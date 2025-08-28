package com.levelupjourney.microserviceiam.IAM.application.internal.commandservices;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.oauth.GoogleOAuth2Service;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.oauth.GitHubOAuth2Service;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.AuthIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserSession;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import com.levelupjourney.microserviceiam.IAM.domain.services.RoleService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.AuthIdentityRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.UserRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OAuth2CommandServiceImpl {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final GitHubOAuth2Service gitHubOAuth2Service;
    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final UserSessionRepository userSessionRepository;
    private final TokenService tokenService;
    private final RoleService roleService;

    public OAuth2CommandServiceImpl(GoogleOAuth2Service googleOAuth2Service,
                               GitHubOAuth2Service gitHubOAuth2Service,
                               UserRepository userRepository,
                               AuthIdentityRepository authIdentityRepository,
                               UserSessionRepository userSessionRepository,
                               TokenService tokenService,
                               RoleService roleService) {
        this.googleOAuth2Service = googleOAuth2Service;
        this.gitHubOAuth2Service = gitHubOAuth2Service;
        this.userRepository = userRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.userSessionRepository = userSessionRepository;
        this.tokenService = tokenService;
        this.roleService = roleService;
    }

    @Transactional
    public String processGoogleCallback(String code, String state, String storedState) {
        if (!state.equals(storedState)) {
            // Create failed session for CSRF attack attempt
            var failedSession = new UserSession(null, AuthProvider.GOOGLE, "LOGIN", false, "Invalid state parameter - possible CSRF attack");
            userSessionRepository.save(failedSession);
            throw new SecurityException("Invalid state parameter - possible CSRF attack");
        }

        try {
            GoogleOAuth2Service.GoogleTokenResponse tokenResponse = 
                googleOAuth2Service.exchangeCodeForTokens(code, state);

            GoogleOAuth2Service.GoogleUserInfo userInfo = 
                googleOAuth2Service.getUserInfo(tokenResponse.getAccessToken());

            if (!Boolean.TRUE.equals(userInfo.getVerifiedEmail())) {
                // Create failed session for unverified email
                var failedSession = new UserSession(null, AuthProvider.GOOGLE, "LOGIN", false, "Email not verified by Google");
                userSessionRepository.save(failedSession);
                throw new SecurityException("Email not verified by Google");
            }

            User user = findOrCreateUser(userInfo);
            boolean isNewUser = !authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, userInfo.getId()).isPresent();
            
            createOrUpdateAuthIdentity(user, userInfo, tokenResponse);

            // Create successful session (LOGIN for existing user, SIGNUP for new user)
            String sessionType = isNewUser ? "SIGNUP" : "LOGIN";
            var session = new UserSession(user, AuthProvider.GOOGLE, sessionType, true);
            userSessionRepository.save(session);
            user.addSession(session);
            
            // Save the user with the new session
            userRepository.save(user);

            return tokenService.generateToken(user.getUsername());
            
        } catch (Exception e) {
            // Create failed session for any other errors during OAuth process
            var failedSession = new UserSession(null, AuthProvider.GOOGLE, "LOGIN", false, "OAuth2 authentication failed: " + e.getMessage());
            userSessionRepository.save(failedSession);
            throw e;
        }
    }

    @Transactional
    public String processGitHubCallback(String code, String state, String storedState) {
        if (!state.equals(storedState)) {
            // Create failed session for CSRF attack attempt
            var failedSession = new UserSession(null, AuthProvider.GITHUB, "LOGIN", false, "Invalid state parameter - possible CSRF attack");
            userSessionRepository.save(failedSession);
            throw new SecurityException("Invalid state parameter - possible CSRF attack");
        }

        try {
            GitHubOAuth2Service.GitHubTokenResponse tokenResponse = 
                gitHubOAuth2Service.exchangeCodeForTokens(code, state);

            GitHubOAuth2Service.GitHubUserInfo userInfo = 
                gitHubOAuth2Service.getUserInfo(tokenResponse.getAccessToken());

            if (userInfo.getEmail() == null) {
                // Create failed session for missing email
                var failedSession = new UserSession(null, AuthProvider.GITHUB, "LOGIN", false, "No email address found in GitHub account");
                userSessionRepository.save(failedSession);
                throw new SecurityException("No email address found in GitHub account");
            }

            User user = findOrCreateUserGitHub(userInfo);
            boolean isNewUser = !authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.GITHUB, userInfo.getId()).isPresent();
            
            createOrUpdateAuthIdentityGitHub(user, userInfo, tokenResponse);

            // Create successful session (LOGIN for existing user, SIGNUP for new user)
            String sessionType = isNewUser ? "SIGNUP" : "LOGIN";
            var session = new UserSession(user, AuthProvider.GITHUB, sessionType, true);
            userSessionRepository.save(session);
            user.addSession(session);
            
            // Save the user with the new session
            userRepository.save(user);

            return tokenService.generateToken(user.getUsername());
            
        } catch (Exception e) {
            // Create failed session for any other errors during OAuth process
            var failedSession = new UserSession(null, AuthProvider.GITHUB, "LOGIN", false, "OAuth2 authentication failed: " + e.getMessage());
            userSessionRepository.save(failedSession);
            throw e;
        }
    }

    private User findOrCreateUser(GoogleOAuth2Service.GoogleUserInfo userInfo) {
        Optional<AuthIdentity> existingIdentity = 
            authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, userInfo.getId());

        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getUser();
        }

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        if (existingUser.isPresent() && Boolean.TRUE.equals(existingUser.get().getEmailVerified())) {
            return existingUser.get();
        }

        // Generate normalized username from email
        String baseUsername = userInfo.getEmail().split("@")[0];
        String normalizedUsername = generateUniqueUsername(baseUsername);
        
        // Create user with normalized username
        User newUser = new User(normalizedUsername);
        newUser.setName(userInfo.getName());
        newUser.setAvatarUrl(userInfo.getPicture());
        
        // Add Google email
        newUser.addEmail(userInfo.getEmail(), true, true, AuthProvider.GOOGLE);
        
        // Add default role using RoleService to avoid duplicates
        newUser.addRole(roleService.getOrCreateDefaultRole());

        return userRepository.save(newUser);
    }

    private void createOrUpdateAuthIdentity(User user, 
                                               GoogleOAuth2Service.GoogleUserInfo userInfo,
                                               GoogleOAuth2Service.GoogleTokenResponse tokenResponse) {
        Optional<AuthIdentity> existingIdentity = 
            authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, userInfo.getId());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());

        if (existingIdentity.isPresent()) {
            AuthIdentity identity = existingIdentity.get();
            identity.setAccessToken(tokenResponse.getAccessToken());
            identity.setRefreshToken(tokenResponse.getRefreshToken());
            identity.setExpiresAt(expiresAt);
            identity.setScope(tokenResponse.getScope());
            identity.updateLastLogin();
            authIdentityRepository.save(identity);
        } else {
            AuthIdentity newIdentity = new AuthIdentity(
                user,
                AuthProvider.GOOGLE,
                userInfo.getId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                expiresAt,
                tokenResponse.getScope()
            );
            authIdentityRepository.save(newIdentity);
            user.addAuthIdentity(newIdentity);
        }
    }

    private User findOrCreateUserGitHub(GitHubOAuth2Service.GitHubUserInfo userInfo) {
        Optional<AuthIdentity> existingIdentity = 
            authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.GITHUB, userInfo.getId());

        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getUser();
        }

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        if (existingUser.isPresent() && Boolean.TRUE.equals(existingUser.get().getEmailVerified())) {
            return existingUser.get();
        }

        // Generate normalized username from email
        String baseUsername = userInfo.getEmail().split("@")[0];
        String normalizedUsername = generateUniqueUsername(baseUsername);
        String displayName = userInfo.getName() != null ? userInfo.getName() : userInfo.getLogin();
        
        // Create user with normalized username
        User newUser = new User(normalizedUsername);
        newUser.setName(displayName);
        newUser.setAvatarUrl(userInfo.getAvatarUrl());
        
        // Add GitHub email
        newUser.addEmail(userInfo.getEmail(), true, true, AuthProvider.GITHUB);
        
        // Add default role using RoleService to avoid duplicates
        newUser.addRole(roleService.getOrCreateDefaultRole());

        return userRepository.save(newUser);
    }

    private void createOrUpdateAuthIdentityGitHub(User user, 
                                                  GitHubOAuth2Service.GitHubUserInfo userInfo,
                                                  GitHubOAuth2Service.GitHubTokenResponse tokenResponse) {
        Optional<AuthIdentity> existingIdentity = 
            authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.GITHUB, userInfo.getId());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());

        if (existingIdentity.isPresent()) {
            AuthIdentity identity = existingIdentity.get();
            identity.setAccessToken(tokenResponse.getAccessToken());
            identity.setRefreshToken(tokenResponse.getRefreshToken());
            identity.setExpiresAt(expiresAt);
            identity.setScope(tokenResponse.getScope());
            identity.updateLastLogin();
            authIdentityRepository.save(identity);
        } else {
            AuthIdentity newIdentity = new AuthIdentity(
                user,
                AuthProvider.GITHUB,
                userInfo.getId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                expiresAt,
                tokenResponse.getScope()
            );
            authIdentityRepository.save(newIdentity);
            user.addAuthIdentity(newIdentity);
        }
    }

    /**
     * Generate a unique username based on a base username
     */
    private String generateUniqueUsername(String baseUsername) {
        String finalUsername = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter;
            counter++;
        }
        return finalUsername;
    }

}
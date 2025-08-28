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

import java.security.SecureRandom;
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
    private final SecureRandom secureRandom;

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
        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public String processGoogleCallback(String code, String state, String storedState) {
        return processOAuth2Callback(
            code, state, storedState, AuthProvider.GOOGLE,
            () -> {
                GoogleOAuth2Service.GoogleTokenResponse tokenResponse = 
                    googleOAuth2Service.exchangeCodeForTokens(code, state);
                GoogleOAuth2Service.GoogleUserInfo userInfo = 
                    googleOAuth2Service.getUserInfo(tokenResponse.getAccessToken());
                
                if (!Boolean.TRUE.equals(userInfo.getVerifiedEmail())) {
                    throw new SecurityException("Email not verified by Google");
                }
                
                return new OAuth2CallbackData(userInfo.getId(), userInfo.getEmail(), 
                    userInfo.getName(), userInfo.getPicture(), tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(), tokenResponse.getExpiresIn().longValue(), tokenResponse.getScope());
            }
        );
    }

    @Transactional
    public String processGitHubCallback(String code, String state, String storedState) {
        return processOAuth2Callback(
            code, state, storedState, AuthProvider.GITHUB,
            () -> {
                GitHubOAuth2Service.GitHubTokenResponse tokenResponse = 
                    gitHubOAuth2Service.exchangeCodeForTokens(code, state);
                GitHubOAuth2Service.GitHubUserInfo userInfo = 
                    gitHubOAuth2Service.getUserInfo(tokenResponse.getAccessToken());
                
                if (userInfo.getEmail() == null) {
                    throw new SecurityException("No email address found in GitHub account");
                }
                
                String displayName = userInfo.getName() != null ? userInfo.getName() : userInfo.getLogin();
                return new OAuth2CallbackData(userInfo.getId(), userInfo.getEmail(), 
                    displayName, userInfo.getAvatarUrl(), tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(), tokenResponse.getExpiresIn().longValue(), tokenResponse.getScope());
            }
        );
    }

    private User findOrCreateUser(String providerId, String email, String name, 
                                    String avatarUrl, AuthProvider provider) {
        Optional<AuthIdentity> existingIdentity = 
            authIdentityRepository.findByProviderAndProviderUserId(provider, providerId);

        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getUser();
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && Boolean.TRUE.equals(existingUser.get().getEmailVerified())) {
            return existingUser.get();
        }

        String randomUsername = generateRandomUsername();
        
        User newUser = new User(randomUsername);
        newUser.setName(name);
        newUser.setAvatarUrl(avatarUrl);
        newUser.addEmail(email, true, true, provider);
        newUser.addRole(roleService.getOrCreateDefaultRole());

        return userRepository.save(newUser);
    }

    private void createOrUpdateAuthIdentity(User user, String providerId, String accessToken,
                                           String refreshToken, Long expiresIn, String scope, AuthProvider provider) {
        Optional<AuthIdentity> existingIdentity = 
            authIdentityRepository.findByProviderAndProviderUserId(provider, providerId);

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

        if (existingIdentity.isPresent()) {
            AuthIdentity identity = existingIdentity.get();
            identity.setAccessToken(accessToken);
            identity.setRefreshToken(refreshToken);
            identity.setExpiresAt(expiresAt);
            identity.setScope(scope);
            identity.updateLastLogin();
            authIdentityRepository.save(identity);
        } else {
            AuthIdentity newIdentity = new AuthIdentity(
                user, provider, providerId, accessToken, refreshToken, expiresAt, scope
            );
            authIdentityRepository.save(newIdentity);
            user.addAuthIdentity(newIdentity);
        }
    }



    private String processOAuth2Callback(String code, String state, String storedState, 
                                        AuthProvider provider, OAuth2DataExtractor extractor) {
        validateState(state, storedState, provider);
        
        try {
            OAuth2CallbackData callbackData = extractor.extractData();
            
            User user = findOrCreateUser(callbackData.providerId, callbackData.email, 
                callbackData.name, callbackData.avatarUrl, provider);
            
            boolean isNewUser = !authIdentityRepository
                .findByProviderAndProviderUserId(provider, callbackData.providerId).isPresent();
            
            createOrUpdateAuthIdentity(user, callbackData.providerId, callbackData.accessToken,
                callbackData.refreshToken, callbackData.expiresIn, callbackData.scope, provider);

            createSuccessfulSession(user, provider, isNewUser);
            userRepository.save(user);

            return tokenService.generateToken(user.getUsername());
            
        } catch (Exception e) {
            createFailedSession(provider, "OAuth2 authentication failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void validateState(String state, String storedState, AuthProvider provider) {
        if (!state.equals(storedState)) {
            createFailedSession(provider, "Invalid state parameter - possible CSRF attack");
            throw new SecurityException("Invalid state parameter - possible CSRF attack");
        }
    }

    private void createSuccessfulSession(User user, AuthProvider provider, boolean isNewUser) {
        String sessionType = isNewUser ? "SIGNUP" : "LOGIN";
        var session = new UserSession(user, provider, sessionType, true);
        userSessionRepository.save(session);
        user.addSession(session);
    }

    private void createFailedSession(AuthProvider provider, String errorMessage) {
        var failedSession = new UserSession(null, provider, "LOGIN", false, errorMessage);
        userSessionRepository.save(failedSession);
    }


    @FunctionalInterface
    private interface OAuth2DataExtractor {
        OAuth2CallbackData extractData() throws Exception;
    }

    private static class OAuth2CallbackData {
        final String providerId;
        final String email;
        final String name;
        final String avatarUrl;
        final String accessToken;
        final String refreshToken;
        final Long expiresIn;
        final String scope;

        OAuth2CallbackData(String providerId, String email, String name, String avatarUrl,
                          String accessToken, String refreshToken, Long expiresIn, String scope) {
            this.providerId = providerId;
            this.email = email;
            this.name = name;
            this.avatarUrl = avatarUrl;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.scope = scope;
        }
    }

    private String generateRandomUsername() {
        String username;
        do {
            // Generate random 9-digit number
            int randomNumber = secureRandom.nextInt(900000000) + 100000000; // ensures 9 digits
            username = "user" + randomNumber;
        } while (userRepository.existsByUsername(username));
        return username;
    }

}
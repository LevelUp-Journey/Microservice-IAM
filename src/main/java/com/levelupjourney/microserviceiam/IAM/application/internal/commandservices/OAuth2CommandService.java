package com.levelupjourney.microserviceiam.IAM.application.internal.commandservices;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.oauth.GoogleOAuth2Service;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.ExternalIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Roles;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.ExternalIdentityRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OAuth2CommandService {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final UserRepository userRepository;
    private final ExternalIdentityRepository externalIdentityRepository;
    private final TokenService tokenService;
    private final BCryptHashingService hashingService;

    public OAuth2CommandService(GoogleOAuth2Service googleOAuth2Service,
                               UserRepository userRepository,
                               ExternalIdentityRepository externalIdentityRepository,
                               TokenService tokenService,
                               BCryptHashingService hashingService) {
        this.googleOAuth2Service = googleOAuth2Service;
        this.userRepository = userRepository;
        this.externalIdentityRepository = externalIdentityRepository;
        this.tokenService = tokenService;
        this.hashingService = hashingService;
    }

    @Transactional
    public String processGoogleCallback(String code, String state, String storedState) {
        if (!state.equals(storedState)) {
            throw new SecurityException("Invalid state parameter - possible CSRF attack");
        }

        GoogleOAuth2Service.GoogleTokenResponse tokenResponse = 
            googleOAuth2Service.exchangeCodeForTokens(code, state);

        GoogleOAuth2Service.GoogleUserInfo userInfo = 
            googleOAuth2Service.getUserInfo(tokenResponse.getAccessToken());

        if (!Boolean.TRUE.equals(userInfo.getVerifiedEmail())) {
            throw new SecurityException("Email not verified by Google");
        }

        User user = findOrCreateUser(userInfo);
        createOrUpdateExternalIdentity(user, userInfo, tokenResponse);

        return tokenService.generateToken(user.getUsername());
    }

    private User findOrCreateUser(GoogleOAuth2Service.GoogleUserInfo userInfo) {
        Optional<ExternalIdentity> existingIdentity = 
            externalIdentityRepository.findByProviderAndProviderUserId("google", userInfo.getId());

        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getUser();
        }

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        if (existingUser.isPresent() && Boolean.TRUE.equals(existingUser.get().getEmailVerified())) {
            return existingUser.get();
        }

        User newUser = new User(
            userInfo.getEmail(),
            userInfo.getName(),
            userInfo.getPicture(),
            true
        );

        newUser.setUsername(generateUsernameFromEmail(userInfo.getEmail()));
        // OAuth users get a hashed placeholder password
        String oauthPassword = "OAUTH_USER_" + userInfo.getId();
        newUser.setPassword(hashingService.encode(oauthPassword));
        
        Role userRole = new Role(Roles.ROLE_USER);
        newUser.addRoles(List.of(userRole));

        return userRepository.save(newUser);
    }

    private void createOrUpdateExternalIdentity(User user, 
                                               GoogleOAuth2Service.GoogleUserInfo userInfo,
                                               GoogleOAuth2Service.GoogleTokenResponse tokenResponse) {
        Optional<ExternalIdentity> existingIdentity = 
            externalIdentityRepository.findByProviderAndProviderUserId("google", userInfo.getId());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());

        if (existingIdentity.isPresent()) {
            ExternalIdentity identity = existingIdentity.get();
            identity.setAccessToken(tokenResponse.getAccessToken());
            identity.setRefreshToken(tokenResponse.getRefreshToken());
            identity.setExpiresAt(expiresAt);
            identity.setScope(tokenResponse.getScope());
            externalIdentityRepository.save(identity);
        } else {
            ExternalIdentity newIdentity = new ExternalIdentity(
                user,
                "google",
                userInfo.getId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                expiresAt,
                tokenResponse.getScope()
            );
            externalIdentityRepository.save(newIdentity);
            user.addExternalIdentity(newIdentity);
        }
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}
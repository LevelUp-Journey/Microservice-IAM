package com.levelupjourney.microserviceiam.IAM.application.internal.commandservices;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.oauth.GoogleOAuth2Service;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.AuthIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.AuthIdentityRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OAuth2CommandServiceImpl {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final TokenService tokenService;

    public OAuth2CommandServiceImpl(GoogleOAuth2Service googleOAuth2Service,
                               UserRepository userRepository,
                               AuthIdentityRepository authIdentityRepository,
                               TokenService tokenService) {
        this.googleOAuth2Service = googleOAuth2Service;
        this.userRepository = userRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.tokenService = tokenService;
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
        createOrUpdateAuthIdentity(user, userInfo, tokenResponse);

        return tokenService.generateToken(user.getUsername());
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

        User newUser = new User(
            userInfo.getEmail(),
            userInfo.getName(),
            userInfo.getPicture(),
            true
        );

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

}
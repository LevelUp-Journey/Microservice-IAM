package com.levelupjourney.microserviceiam.iam.infrastructure.oauth2;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.iam.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.iam.domain.model.events.UserRegisteredEvent;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.OAuth2UserInfo;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.Roles;
import com.levelupjourney.microserviceiam.iam.domain.services.RoleQueryService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserQueryService;
import com.levelupjourney.microserviceiam.iam.infrastructure.eventpublishers.IamEventPublisher;
import com.levelupjourney.microserviceiam.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final RoleQueryService roleQueryService;
    private final BearerTokenService tokenService;
    private final OAuth2UserInfoExtractor userInfoExtractor;
    private final IamEventPublisher eventPublisher;

    @Value("${app.oauth2.authorized-redirect-uris}")
    private String authorizedRedirectUris;

    public OAuth2AuthenticationSuccessHandler(UserCommandService userCommandService,
                                            UserQueryService userQueryService,
                                            RoleQueryService roleQueryService,
                                            BearerTokenService tokenService,
                                            OAuth2UserInfoExtractor userInfoExtractor,
                                            IamEventPublisher eventPublisher) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.roleQueryService = roleQueryService;
        this.tokenService = tokenService;
        this.userInfoExtractor = userInfoExtractor;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        System.out.println("OAuth2 Authentication Success Handler called!");
        System.out.println("Authentication: " + authentication.getClass().getName());
        System.out.println("Principal: " + authentication.getPrincipal().getClass().getName());
        
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            try {
                System.out.println("Processing OAuth2User...");
                System.out.println("Attributes: " + oauth2User.getAttributes());
                String token = handleOAuth2User(oauth2User);
                String redirectUrl = getAuthorizedRedirectUri() + "?token=" + token;
                System.out.println("Redirecting to: " + redirectUrl);
                response.sendRedirect(redirectUrl);
            } catch (Exception e) {
                System.err.println("Error in handleOAuth2User: " + e.getMessage());
                e.printStackTrace();
                String redirectUrl = getAuthorizedRedirectUri() + "?error=authentication_failed";
                response.sendRedirect(redirectUrl);
            }
        } else {
            System.err.println("Principal is not OAuth2User, redirecting with invalid_user error");
            String redirectUrl = getAuthorizedRedirectUri() + "?error=invalid_user";
            response.sendRedirect(redirectUrl);
        }
    }

    private String handleOAuth2User(OAuth2User oauth2User) {
        // Extract provider and user info
        String provider = userInfoExtractor.extractProvider(oauth2User);
        OAuth2UserInfo userInfo = userInfoExtractor.extractUserInfo(oauth2User, provider);

        String email = userInfo.email();

        if (email == null) {
            throw new RuntimeException("email not found in OAuth2 user attributes");
        }

        Optional<User> existingUser = userQueryService.handle(new com.levelupjourney.microserviceiam.iam.domain.model.queries.GetUserByEmailQuery(email));

        User user;
        boolean isNewUser = false;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Create user in IAM
            Role userRole = roleQueryService.handle(new com.levelupjourney.microserviceiam.iam.domain.model.queries.GetRoleByNameQuery(Roles.ROLE_STUDENT))
                    .orElseThrow(() -> new RuntimeException("Student role not found"));

            SignUpCommand signUpCommand = new SignUpCommand(email, "oauth2_user_" + System.currentTimeMillis(), List.of(userRole));
            Optional<User> newUser = userCommandService.handle(signUpCommand);

            if (newUser.isEmpty()) {
                throw new RuntimeException("Failed to create user from OAuth2 data");
            }
            user = newUser.get();
            isNewUser = true;
        }

        // Publish event only for new users
        if (isNewUser) {
            UserRegisteredEvent event = new UserRegisteredEvent(
                    user.getId(),
                    email,
                    userInfo.firstName(),
                    userInfo.lastName(),
                    userInfo.profileUrl(),
                    provider,
                    LocalDateTime.now()
            );

            eventPublisher.publishUserRegistered(event);
        }

        return tokenService.generateToken(user);
    }

    private String getAuthorizedRedirectUri() {
        String[] uris = authorizedRedirectUris.split(",");
        return uris[0].trim();
    }
}
package com.levelupjourney.microserviceiam.iam.infrastructure.oauth2;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.iam.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.Roles;
import com.levelupjourney.microserviceiam.iam.domain.services.RoleQueryService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserQueryService;
import com.levelupjourney.microserviceiam.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final RoleQueryService roleQueryService;
    private final BearerTokenService tokenService;

    @Value("${app.oauth2.authorized-redirect-uris}")
    private String authorizedRedirectUris;

    public OAuth2AuthenticationSuccessHandler(UserCommandService userCommandService,
                                            UserQueryService userQueryService,
                                            RoleQueryService roleQueryService,
                                            BearerTokenService tokenService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.roleQueryService = roleQueryService;
        this.tokenService = tokenService;
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
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = extractEmail(attributes);
        String username = extractUsername(attributes);

        if (email == null || username == null) {
            throw new RuntimeException("Email or username not found in OAuth2 user attributes");
        }

        Optional<User> existingUser = userQueryService.handle(new com.levelupjourney.microserviceiam.iam.domain.model.queries.GetUserByUsernameQuery(username));
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            Role userRole = roleQueryService.handle(new com.levelupjourney.microserviceiam.iam.domain.model.queries.GetRoleByNameQuery(Roles.ROLE_STUDENT))
                    .orElseThrow(() -> new RuntimeException("User role not found"));
            
            SignUpCommand signUpCommand = new SignUpCommand(username, "oauth2_user_" + System.currentTimeMillis(), List.of(userRole));
            Optional<User> newUser = userCommandService.handle(signUpCommand);
            
            if (newUser.isEmpty()) {
                throw new RuntimeException("Failed to create user from OAuth2 data");
            }
            user = newUser.get();
        }

        return tokenService.generateToken(user.getUsername());
    }

    private String extractEmail(Map<String, Object> attributes) {
        return (String) attributes.get("email");
    }

    private String extractUsername(Map<String, Object> attributes) {
        if (attributes.containsKey("login")) {
            return (String) attributes.get("login");
        }
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }
        return null;
    }

    private String getAuthorizedRedirectUri() {
        String[] uris = authorizedRedirectUris.split(",");
        return uris[0].trim();
    }
}
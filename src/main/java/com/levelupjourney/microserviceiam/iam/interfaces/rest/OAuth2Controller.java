package com.levelupjourney.microserviceiam.iam.interfaces.rest;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.iam.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.Roles;
import com.levelupjourney.microserviceiam.iam.domain.services.RoleQueryService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserQueryService;
import com.levelupjourney.microserviceiam.iam.infrastructure.configuration.FrontendConfigurationProperties;
import com.levelupjourney.microserviceiam.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "OAuth2 Authentication", description = "OAuth2 Authentication Endpoints")
public class OAuth2Controller {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final RoleQueryService roleQueryService;
    private final BearerTokenService tokenService;
    private final FrontendConfigurationProperties frontendProperties;

    public OAuth2Controller(UserCommandService userCommandService,
                          UserQueryService userQueryService,
                          RoleQueryService roleQueryService,
                          BearerTokenService tokenService,
                          FrontendConfigurationProperties frontendProperties) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.roleQueryService = roleQueryService;
        this.tokenService = tokenService;
        this.frontendProperties = frontendProperties;
    }

    @GetMapping("/oauth2/callback")
    @Operation(summary = "OAuth2 Success Callback", description = "Handles successful OAuth2 authentication")
    public void oauth2Success(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String token = handleOAuth2User(oauth2User);

            String redirectUrl = frontendProperties.getPrimaryRedirectUri() + "?token=" + token;
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect(frontendProperties.getPrimaryRedirectUri() + "?error=authentication_failed");
        }
    }

    @GetMapping("/oauth2/error")
    @Operation(summary = "OAuth2 Error Callback", description = "Handles OAuth2 authentication errors")
    public void oauth2Error(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) String error) throws IOException {
        String redirectUrl = frontendProperties.getPrimaryRedirectUri() + "?error=" + (error != null ? error : "oauth2_error");
        response.sendRedirect(redirectUrl);
    }

    private String handleOAuth2User(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = extractEmail(attributes);
        String username = extractUsername(attributes);

        if (email == null || username == null) {
            throw new RuntimeException("email or username not found in OAuth2 user attributes");
        }

        Optional<User> existingUser = userQueryService.handle(new com.levelupjourney.microserviceiam.iam.domain.model.queries.GetUserByEmailQuery(username));
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            Role userRole = roleQueryService.handle(new com.levelupjourney.microserviceiam.iam.domain.model.queries.GetRoleByNameQuery(Roles.ROLE_STUDENT))
                    .orElseThrow(() -> new RuntimeException("User role not found"));
            
            SignUpCommand signUpCommand = new SignUpCommand(username, "oauth2_password", List.of(userRole));
            Optional<User> newUser = userCommandService.handle(signUpCommand);
            
            if (newUser.isEmpty()) {
                throw new RuntimeException("Failed to create user from OAuth2 data");
            }
            user = newUser.get();
        }

        return tokenService.generateToken(user);
    }

    private String extractEmail(Map<String, Object> attributes) {
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }
        return null;
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
}

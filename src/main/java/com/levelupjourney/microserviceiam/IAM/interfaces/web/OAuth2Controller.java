package com.levelupjourney.microserviceiam.IAM.interfaces.web;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.OAuth2SignInCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Controller
@Tag(name = "OAuth2 Authentication", description = "OAuth2 authentication endpoints for Google and GitHub")
public class OAuth2Controller {
    
    private final AccountCommandService accountCommandService;
    private final TokenService tokenService;
    
    public OAuth2Controller(AccountCommandService accountCommandService, TokenService tokenService) {
        this.accountCommandService = accountCommandService;
        this.tokenService = tokenService;
    }
    
    @GetMapping("/oauth2/google")
    @Operation(summary = "Redirect to Google OAuth2 authorization")
    public void googleOAuth2(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }
    
    @GetMapping("/oauth2/github")
    @Operation(summary = "Redirect to GitHub OAuth2 authorization")
    public void githubOAuth2(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/github");
    }
    
    @GetMapping("/oauth2/callback")
    @Operation(summary = "Handle OAuth2 callback from providers")
    public ResponseEntity<?> oauth2Callback(@AuthenticationPrincipal OAuth2User oauth2User,
                                           @RequestParam(required = false) String state,
                                           HttpServletResponse response) throws IOException {
        try {
            if (oauth2User == null) {
                response.sendRedirect("/oauth2/error?error=authentication_failed");
                return null;
            }

            // Determine provider from OAuth2User attributes
            String provider = determineProvider(oauth2User);
            String providerUserId = oauth2User.getAttribute("id").toString();
            String email = extractEmail(oauth2User, provider);
            String name = extractName(oauth2User, provider);
            
            // Create OAuth2SignInCommand
            AuthProvider authProvider = switch (provider.toLowerCase()) {
                case "google" -> AuthProvider.google();
                case "github" -> AuthProvider.github();
                default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
            };
            
            OAuth2SignInCommand command = new OAuth2SignInCommand(
                authProvider,
                providerUserId,
                new EmailAddress(email),
                name,
                oauth2User.getAttributes(),
                Set.of(Role.getDefaultRole())
            );
            
            // Handle OAuth2 sign in
            var accountOpt = accountCommandService.handle(command);
            
            if (accountOpt.isEmpty()) {
                response.sendRedirect("/oauth2/error?error=sign_in_failed");
                return null;
            }
            
            Account account = accountOpt.get();
            String accessToken = tokenService.generateToken(account);
            String refreshToken = tokenService.generateRefreshToken(account);
            
            // Redirect to success page with tokens
            String redirectUrl = String.format("/oauth2/success?access_token=%s&refresh_token=%s", 
                                              accessToken, refreshToken);
            response.sendRedirect(redirectUrl);
            
            return null;
        } catch (Exception e) {
            response.sendRedirect("/oauth2/error?error=" + e.getMessage());
            return null;
        }
    }
    
    @GetMapping("/oauth2/success")
    @Operation(summary = "OAuth2 success page")
    public ResponseEntity<Map<String, Object>> oauth2Success(@RequestParam String access_token,
                                                            @RequestParam String refresh_token) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "OAuth2 authentication successful",
            "access_token", access_token,
            "refresh_token", refresh_token,
            "token_type", "Bearer"
        ));
    }
    
    @GetMapping("/oauth2/status")
    @Operation(summary = "Get current OAuth2 authentication status")
    public ResponseEntity<Map<String, Object>> oauth2Status(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User != null) {
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "provider", determineProvider(oauth2User),
                "name", extractName(oauth2User, determineProvider(oauth2User)),
                "email", extractEmail(oauth2User, determineProvider(oauth2User))
            ));
        } else {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }
    
    private String determineProvider(OAuth2User oauth2User) {
        // Check for Google-specific attributes
        if (oauth2User.getAttributes().containsKey("sub")) {
            return "google";
        }
        // Check for GitHub-specific attributes
        if (oauth2User.getAttributes().containsKey("login")) {
            return "github";
        }
        // Default fallback
        return "unknown";
    }
    
    private String extractEmail(OAuth2User oauth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oauth2User.getAttribute("email");
            case "github" -> oauth2User.getAttribute("email");
            default -> oauth2User.getAttribute("email");
        };
    }
    
    private String extractName(OAuth2User oauth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oauth2User.getAttribute("name");
            case "github" -> {
                String name = oauth2User.getAttribute("name");
                yield name != null ? name : oauth2User.getAttribute("login");
            }
            default -> oauth2User.getAttribute("name");
        };
    }
}
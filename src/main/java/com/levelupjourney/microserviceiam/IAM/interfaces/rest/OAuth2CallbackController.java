package com.levelupjourney.microserviceiam.IAM.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.OAuth2SignInCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountCommandService;

import java.util.Map;
import java.util.Set;

@Controller
@Tag(name = "OAuth2 Callback", description = "OAuth2 callback handler")
public class OAuth2CallbackController {
    
    private final AccountCommandService accountCommandService;
    private final TokenService tokenService;
    
    public OAuth2CallbackController(AccountCommandService accountCommandService, TokenService tokenService) {
        this.accountCommandService = accountCommandService;
        this.tokenService = tokenService;
    }
    
    @GetMapping("/oauth2/callback")
    @Operation(summary = "Handle OAuth2 callback from providers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 authentication successful"),
        @ApiResponse(responseCode = "400", description = "Authentication failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> oauth2Callback(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) {
        
        try {
            if (oauth2User == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "AUTHENTICATION_FAILED",
                    "message", "OAuth2 authentication failed - no user info available"
                ));
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
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "SIGN_IN_FAILED",
                    "message", "OAuth2 sign in failed"
                ));
            }
            
            Account account = accountOpt.get();
            String accessToken = tokenService.generateToken(account);
            String refreshToken = tokenService.generateRefreshToken(account);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OAuth2 authentication successful",
                "access_token", accessToken,
                "refresh_token", refreshToken,
                "token_type", "Bearer",
                "provider", provider,
                "user", Map.of(
                    "email", email,
                    "name", name,
                    "provider_user_id", providerUserId
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/oauth2/success")
    @Operation(summary = "Handle successful OAuth2 authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 authentication successful"),
        @ApiResponse(responseCode = "400", description = "Authentication failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> oauth2Success(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RequestParam(required = false) String access_token,
            @RequestParam(required = false) String refresh_token,
            @RequestParam(required = false) String provider) {
        
        // If tokens are already provided, return them
        if (access_token != null && refresh_token != null && provider != null) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OAuth2 authentication successful",
                "access_token", access_token,
                "refresh_token", refresh_token,
                "token_type", "Bearer",
                "provider", provider
            ));
        }
        
        // Otherwise, process OAuth2 authentication
        try {
            if (oauth2User == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "AUTHENTICATION_FAILED",
                    "message", "OAuth2 authentication failed"
                ));
            }

            // Determine provider from OAuth2User attributes
            String detectedProvider = determineProvider(oauth2User);
            String providerUserId = oauth2User.getAttribute("id").toString();
            String email = extractEmail(oauth2User, detectedProvider);
            String name = extractName(oauth2User, detectedProvider);
            
            // Create OAuth2SignInCommand
            AuthProvider authProvider = switch (detectedProvider.toLowerCase()) {
                case "google" -> AuthProvider.google();
                case "github" -> AuthProvider.github();
                default -> throw new IllegalArgumentException("Unsupported provider: " + detectedProvider);
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
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "SIGN_IN_FAILED",
                    "message", "OAuth2 sign in failed"
                ));
            }
            
            Account account = accountOpt.get();
            String accessTokenGenerated = tokenService.generateToken(account);
            String refreshTokenGenerated = tokenService.generateRefreshToken(account);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OAuth2 authentication successful",
                "access_token", accessTokenGenerated,
                "refresh_token", refreshTokenGenerated,
                "token_type", "Bearer",
                "provider", detectedProvider
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/oauth2/error")
    @Operation(summary = "OAuth2 error page")
    public ResponseEntity<Map<String, Object>> oauth2Error(@RequestParam String message) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "OAUTH2_ERROR",
            "message", message,
            "success", false
        ));
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
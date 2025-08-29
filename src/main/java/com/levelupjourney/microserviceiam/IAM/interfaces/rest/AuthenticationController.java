package com.levelupjourney.microserviceiam.IAM.interfaces.rest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.http.HttpServletResponse;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.ChangePasswordCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.OAuth2SignInCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SignInCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.UpdateUsernameCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.IAM.application.internal.services.AuthenticationService;
import com.levelupjourney.microserviceiam.IAM.application.internal.services.AccountContextualCommandService;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.*;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.levelupjourney.microserviceiam.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authentication")
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthenticationController {
    
    private final AccountContextualCommandService accountContextualCommandService;
    private final TokenService tokenService;
    private final AuthenticationService authenticationService;
    
    public AuthenticationController(AccountContextualCommandService accountContextualCommandService,
                                   TokenService tokenService,
                                   AuthenticationService authenticationService) {
        this.accountContextualCommandService = accountContextualCommandService;
        this.tokenService = tokenService;
        this.authenticationService = authenticationService;
    }
    
    @PostMapping("/sign-up")
    @Operation(summary = "Sign up a new user", description = "Creates a new user account with email, username and password")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
        @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpResource resource,
                                   HttpServletRequest request) {
        try {
            var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
            var accountOpt = accountContextualCommandService.handleSignUp(command, extractUserAgent(request));
            
            if (accountOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new MessageResource("Failed to create user account"));
            }
            
            Account account = accountOpt.get();
            String accessToken = tokenService.generateToken(account);
            String refreshToken = tokenService.generateRefreshToken(account);
            
            var userResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(account, accessToken, refreshToken);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(userResource);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "VALIDATION_ERROR",
                    "message", e.getMessage(),
                    "details", Map.of("field", "unknown")
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new MessageResource("Sign up failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/sign-in")
    @Operation(summary = "Sign in user", description = "Authenticates a user with email/username and password")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource resource,
                                   HttpServletRequest request) {
        try {
            var command = new SignInCommand(resource.emailOrUsername(), resource.password());
            var accountOpt = accountContextualCommandService.handleSignIn(command, extractUserAgent(request));
            
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResource("Invalid credentials"));
            }
            
            Account account = accountOpt.get();
            String accessToken = tokenService.generateToken(account);
            String refreshToken = tokenService.generateRefreshToken(account);
            
            var userResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(account, accessToken, refreshToken);
            
            return ResponseEntity.ok(userResource);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResource("Authentication failed: " + e.getMessage()));
        }
    }
    
    @PutMapping("/accounts/{accountId}/password")
    @PreAuthorize("hasRole('USER') and @authenticationService.canAccessAccount(authentication, #accountId)")
    @Operation(summary = "Change user password", description = "Changes the password for the specified account - User can only change own password")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid current password or validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Can only change own password"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<?> changePassword(@PathVariable UUID accountId,
                                          @Valid @RequestBody ChangePasswordResource resource,
                                          Authentication authentication,
                                          HttpServletRequest request) {
        try {
            // Verify user can only change their own password
            UUID authenticatedAccountId = authenticationService.getAccountIdFromAuthentication(authentication);
            if (!authenticatedAccountId.equals(accountId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResource("You can only change your own password"));
            }
            
            var command = new ChangePasswordCommand(
                new AccountId(accountId),
                resource.currentPassword(),
                resource.newPassword()
            );
            
            var result = accountContextualCommandService.handleChangePassword(command, extractUserAgent(request));
            
            if (result.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new MessageResource("Failed to change password"));
            }
            
            return ResponseEntity.ok(new MessageResource("Password changed successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "VALIDATION_ERROR",
                    "message", e.getMessage(),
                    "details", Map.of("field", "password")
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new MessageResource("Failed to change password: " + e.getMessage()));
        }
    }
    
    @PutMapping("/accounts/{accountId}/username")
    @PreAuthorize("hasRole('USER') and @authenticationService.canAccessAccount(authentication, #accountId)")
    @Operation(summary = "Update username", description = "Updates the username for the specified account - User can only change own username")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Username updated successfully"),
        @ApiResponse(responseCode = "400", description = "Username already exists or validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Can only change own username"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<?> updateUsername(@PathVariable UUID accountId,
                                          @Valid @RequestBody UpdateUsernameResource resource,
                                          Authentication authentication,
                                          HttpServletRequest request) {
        try {
            // Verify user can only change their own username
            UUID authenticatedAccountId = authenticationService.getAccountIdFromAuthentication(authentication);
            if (!authenticatedAccountId.equals(accountId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResource("You can only change your own username"));
            }
            
            var command = new UpdateUsernameCommand(
                new AccountId(accountId),
                new Username(resource.username())
            );
            
            var result = accountContextualCommandService.handleUpdateUsername(command, extractUserAgent(request));
            
            if (result.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new MessageResource("Failed to update username"));
            }
            
            return ResponseEntity.ok(new MessageResource("Username updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "VALIDATION_ERROR",
                    "message", e.getMessage(),
                    "details", Map.of("field", "username")
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new MessageResource("Failed to update username: " + e.getMessage()));
        }
    }
    
    @GetMapping("/oauth2/google")
    @Operation(summary = "Redirect to Google OAuth2 authorization", description = "Redirects directly to Google OAuth2 authorization")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Redirect to Google OAuth2"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void googleOAuth2(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }
    
    @GetMapping("/oauth2/github")
    @Operation(summary = "Redirect to GitHub OAuth2 authorization", description = "Redirects directly to GitHub OAuth2 authorization")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Redirect to GitHub OAuth2"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void githubOAuth2(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/github");
    }

    @GetMapping("/oauth2/callback")
    @Operation(summary = "Handle OAuth2 callback from providers", description = "Processes OAuth2 callback and returns authentication tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 authentication successful"),
        @ApiResponse(responseCode = "400", description = "Authentication failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> oauth2Callback(@AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            if (oauth2User == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "AUTHENTICATION_FAILED",
                    "message", "OAuth2 authentication failed"
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
            
            // Handle OAuth2 sign in - note: oauth2 callback doesn't have direct request access for user agent
            var accountOpt = accountContextualCommandService.handleOAuth2SignIn(command, null);
            
            if (accountOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "SIGN_IN_FAILED",
                    "message", "OAuth2 sign in failed"
                ));
            }
            
            Account account = accountOpt.get();
            String accessToken = tokenService.generateToken(account);
            String refreshToken = tokenService.generateRefreshToken(account);
            
            var userResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(account, accessToken, refreshToken);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OAuth2 authentication successful",
                "user", userResource,
                "provider", provider
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", e.getMessage()
            ));
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
    
    private String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
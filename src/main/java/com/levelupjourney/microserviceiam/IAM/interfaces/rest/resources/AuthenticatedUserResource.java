package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Authenticated user response resource")
public record AuthenticatedUserResource(
    @Schema(description = "Account ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID accountId,
    
    @Schema(description = "Username", example = "john_doe")
    String username,
    
    @Schema(description = "Email", example = "user@example.com")
    String email,
    
    @Schema(description = "User roles", example = "[\"STUDENT\"]")
    Set<String> roles,
    
    @Schema(description = "JWT access token")
    String accessToken,
    
    @Schema(description = "JWT refresh token")
    String refreshToken,
    
    @Schema(description = "Token type", example = "Bearer")
    String tokenType
) {
    public AuthenticatedUserResource(UUID accountId, String username, String email, Set<String> roles, String accessToken, String refreshToken) {
        this(accountId, username, email, roles, accessToken, refreshToken, "Bearer");
    }
}
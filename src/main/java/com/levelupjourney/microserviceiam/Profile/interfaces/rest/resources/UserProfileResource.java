package com.levelupjourney.microserviceiam.Profile.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "User profile information")
public record UserProfileResource(
        @Schema(description = "Profile unique identifier", example = "e057bb73-19fe-4eb7-99ab-7d962cad3ed5")
        @JsonProperty("id")
        UUID id,
        
        @Schema(description = "Account identifier from IAM", example = "550e8400-e29b-41d4-a716-446655440000")
        @JsonProperty("account_id")
        UUID accountId,
        
        @Schema(description = "Public username", example = "user691412544")
        @JsonProperty("username")
        String username,
        
        @Schema(description = "Display name", example = "John Doe")
        @JsonProperty("name")
        String name,
        
        @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
        @JsonProperty("avatar_url")
        String avatarUrl,
        
        @Schema(description = "User roles", example = "[\"STUDENT\", \"USER\"]")
        @JsonProperty("roles")
        Set<String> roles,
        
        @Schema(description = "Creation timestamp")
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        
        @Schema(description = "Last update timestamp")
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {}
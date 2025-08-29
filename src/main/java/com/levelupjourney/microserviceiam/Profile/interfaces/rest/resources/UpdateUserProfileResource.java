package com.levelupjourney.microserviceiam.Profile.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Update user profile request - Username cannot be changed here, use authentication endpoints")
public record UpdateUserProfileResource(        
        @Schema(description = "Display name", example = "John Doe")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        @JsonProperty("name")
        String name,
        
        @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
        @Pattern(regexp = "^(https?://.+)?$", message = "Avatar URL must be a valid HTTP/HTTPS URL or empty")
        @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
        @JsonProperty("avatar_url")
        String avatarUrl
) {}
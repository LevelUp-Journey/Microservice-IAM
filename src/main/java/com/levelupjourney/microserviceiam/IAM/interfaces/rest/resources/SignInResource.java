package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Sign in request resource")
public record SignInResource(
    @NotBlank(message = "Email or username is required")
    @Schema(description = "Email or username for authentication", example = "user@example.com")
    String emailOrUsername,
    
    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "SecurePassword123!")
    String password
) {}
package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Sign up request resource")
public record SignUpResource(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User email address", example = "user@example.com")
    String email,
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 32, message = "Username must be between 3 and 32 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.\\-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    @Schema(description = "Unique username", example = "john_doe")
    String username,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(description = "User password", example = "SecurePassword123!")
    String password
) {}
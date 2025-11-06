package com.levelupjourney.microserviceiam.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInResource(
        @NotBlank(message = "Email is required")
        @Email(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$", 
               message = "Email must be in valid RFC 5322 format")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        @Schema(description = "User email", example = "user@example.com")
        String email,
        
        @NotBlank(message = "Password is required")
        @Size(min = 1, max = 128, message = "Password cannot exceed 128 characters")
        @Schema(description = "User password", example = "MySecure123!")
        String password) {
}

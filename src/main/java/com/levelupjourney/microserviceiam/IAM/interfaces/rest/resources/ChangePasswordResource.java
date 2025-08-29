package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Change password request resource")
public record ChangePasswordResource(
    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password", example = "OldPassword123!")
    String currentPassword,
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    @Schema(description = "New password", example = "NewPassword123!")
    String newPassword
) {}
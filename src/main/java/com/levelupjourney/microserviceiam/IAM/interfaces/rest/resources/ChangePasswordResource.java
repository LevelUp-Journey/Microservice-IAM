package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Change password request resource")
public record ChangePasswordResource(
    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password", example = "OldPassword123!")
    String currentPassword,
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 40, message = "New password must be between 8 and 40 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
             message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)")
    @Schema(description = "New password", example = "NewPassword123!")
    String newPassword
) {}
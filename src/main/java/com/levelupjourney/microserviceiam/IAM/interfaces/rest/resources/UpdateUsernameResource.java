package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Update username request resource")
public record UpdateUsernameResource(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.\\-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    @Schema(description = "New username", example = "new_username")
    String username
) {}
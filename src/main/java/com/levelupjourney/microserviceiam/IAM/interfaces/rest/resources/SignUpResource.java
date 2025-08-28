package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpResource(@NotBlank @Email String email, @NotBlank String username, @NotBlank String password) {
}

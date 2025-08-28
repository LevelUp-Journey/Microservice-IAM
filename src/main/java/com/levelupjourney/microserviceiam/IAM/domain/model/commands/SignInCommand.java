package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Sign in command
 * <p>
 *     This class represents the command to sign in a user.
 * </p>
 * @param email the email of the user
 * @param password the password of the user
 *
 * @see com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User
 */
public record SignInCommand(@NotBlank @Email String email, @NotBlank String password) {
}

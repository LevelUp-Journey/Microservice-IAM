package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Sign up command
 * <p>
 *     This class represents the command to sign up a user.
 *     Users are assigned STUDENT role by default.
 * </p>
 * @param email the email of the user
 * @param username the username of the user
 * @param password the password of the user
 *
 * @see com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User
 */
public record SignUpCommand(@NotBlank @Email String email, @NotBlank String username, @NotBlank String password) {
}

package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

/**
 * Sign up command
 * <p>
 *     This class represents the command to sign up a user.
 *     Users are assigned STUDENT role by default.
 * </p>
 * @param username the username of the user
 * @param password the password of the user
 *
 * @see com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User
 */
public record SignUpCommand(String username, String password) {
}

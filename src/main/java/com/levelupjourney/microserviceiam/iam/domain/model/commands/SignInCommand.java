package com.levelupjourney.microserviceiam.iam.domain.model.commands;

/**
 * Sign in command
 * <p>
 *     This class represents the command to sign in a user.
 * </p>
 * @param email_address the email_address of the user
 * @param password the password of the user
 *
 * @see com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User
 */
public record SignInCommand(String email_address, String password) {

    /**
     * Get username (for compatibility - returns email_address)
     * @return the email_address as username
     */
    public String username() {
        return this.email_address;
    }
}

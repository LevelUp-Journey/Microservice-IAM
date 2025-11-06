package com.levelupjourney.microserviceiam.iam.domain.model.commands;

import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;

import java.util.List;

/**
 * Sign up command
 * <p>
 *     This class represents the command to sign up a user.
 * </p>
 * @param email the email of the user
 * @param password the password of the user
 * @param roles the roles of the user
 *
 * @see com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User
 */
public record SignUpCommand(String email, String password, List<Role> roles) {

    /**
     * Get username (for compatibility - returns email)
     * @return the email as username
     */
    public String username() {
        return this.email;
    }
}

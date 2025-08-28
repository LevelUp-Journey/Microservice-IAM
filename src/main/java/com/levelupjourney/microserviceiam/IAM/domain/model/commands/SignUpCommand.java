package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.EmailAddress;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;

import java.util.Set;

public record SignUpCommand(
    EmailAddress email,
    Username username,
    String password,
    Set<Role> roles
) {
    public SignUpCommand {
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
    }

    public SignUpCommand(EmailAddress email, Username username, String password) {
        this(email, username, password, Set.of(Role.getDefaultRole()));
    }
}
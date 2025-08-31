package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

public record SignInCommand(
    String emailOrUsername,
    String password
) {
    public SignInCommand {
        if (emailOrUsername == null || emailOrUsername.isBlank()) {
            throw new IllegalArgumentException("Email or username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
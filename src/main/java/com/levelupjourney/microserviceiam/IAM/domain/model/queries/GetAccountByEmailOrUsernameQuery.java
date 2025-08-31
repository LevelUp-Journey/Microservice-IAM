package com.levelupjourney.microserviceiam.IAM.domain.model.queries;

public record GetAccountByEmailOrUsernameQuery(String emailOrUsername) {
    public GetAccountByEmailOrUsernameQuery {
        if (emailOrUsername == null || emailOrUsername.isBlank()) {
            throw new IllegalArgumentException("Email or username is required");
        }
    }
}
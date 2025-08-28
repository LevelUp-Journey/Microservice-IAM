package com.levelupjourney.microserviceiam.IAM.domain.model.queries;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;

public record GetAccountByUsernameQuery(Username username) {
    public GetAccountByUsernameQuery {
        if (username == null) {
            throw new IllegalArgumentException("Username is required");
        }
    }
}
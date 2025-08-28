package com.levelupjourney.microserviceiam.IAM.domain.model.queries;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.EmailAddress;

public record GetAccountByEmailQuery(EmailAddress email) {
    public GetAccountByEmailQuery {
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
    }
}
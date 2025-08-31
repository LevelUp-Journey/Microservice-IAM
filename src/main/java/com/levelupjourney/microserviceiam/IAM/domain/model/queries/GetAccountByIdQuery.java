package com.levelupjourney.microserviceiam.IAM.domain.model.queries;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;

public record GetAccountByIdQuery(AccountId accountId) {
    public GetAccountByIdQuery {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
    }
}
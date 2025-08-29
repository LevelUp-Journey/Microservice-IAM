package com.levelupjourney.microserviceiam.Profile.domain.model.queries;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;

public record GetUserProfileByAccountIdQuery(AccountId accountId) {
    public GetUserProfileByAccountIdQuery {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
    }
}
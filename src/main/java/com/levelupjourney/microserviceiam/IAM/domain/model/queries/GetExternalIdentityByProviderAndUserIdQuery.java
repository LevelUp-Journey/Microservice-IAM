package com.levelupjourney.microserviceiam.IAM.domain.model.queries;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;

public record GetExternalIdentityByProviderAndUserIdQuery(
    AuthProvider provider,
    String providerUserId
) {
    public GetExternalIdentityByProviderAndUserIdQuery {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is required");
        }
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("Provider user ID is required");
        }
    }
}
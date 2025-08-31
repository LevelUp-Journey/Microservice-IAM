package com.levelupjourney.microserviceiam.Profile.domain.model.queries;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.PublicUsername;

public record GetUserProfileByUsernameQuery(PublicUsername username) {
    public GetUserProfileByUsernameQuery {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
    }
}

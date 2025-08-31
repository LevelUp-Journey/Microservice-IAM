package com.levelupjourney.microserviceiam.Profile.domain.model.queries;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.ProfileId;

public record GetUserProfileByIdQuery(ProfileId profileId) {
    public GetUserProfileByIdQuery {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID is required");
        }
    }
}
package com.levelupjourney.microserviceiam.Profile.domain.services;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.domain.model.queries.GetAllUserProfilesQuery;
import com.levelupjourney.microserviceiam.Profile.domain.model.queries.GetUserProfileByAccountIdQuery;
import com.levelupjourney.microserviceiam.Profile.domain.model.queries.GetUserProfileByIdQuery;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserProfileQueryService {
    Optional<UserProfile> handle(GetUserProfileByIdQuery query);
    Optional<UserProfile> handle(GetUserProfileByAccountIdQuery query);
    Page<UserProfile> handle(GetAllUserProfilesQuery query);
}
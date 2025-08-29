package com.levelupjourney.microserviceiam.Profile.domain.services;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.domain.model.queries.*;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserProfileQueryService {
    Optional<UserProfile> handle(GetUserProfileByIdQuery query);
    Optional<UserProfile> handle(GetUserProfileByAccountIdQuery query);
    Optional<UserProfile> handle(GetUserProfileByUsernameQuery query);
    Page<UserProfile> handle(GetAllUserProfilesQuery query);
    Page<UserProfile> handle(GetUserProfilesByUsernameQuery query);
    Page<UserProfile> handle(GetUserProfilesByRoleQuery query);
}
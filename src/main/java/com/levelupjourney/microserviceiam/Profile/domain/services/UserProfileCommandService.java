package com.levelupjourney.microserviceiam.Profile.domain.services;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.domain.model.commands.*;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.ProfileId;

import java.util.Optional;

public interface UserProfileCommandService {
    Optional<UserProfile> handle(CreateUserProfileCommand command);
    Optional<UserProfile> handle(CreateUserProfileFromAccountCommand command);
    Optional<ProfileId> handle(UpdateUserProfileCommand command);
}
package com.levelupjourney.microserviceiam.Profile.domain.services;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.domain.model.commands.CreateUserProfileCommand;
import com.levelupjourney.microserviceiam.Profile.domain.model.commands.UpdateUserProfileCommand;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.ProfileId;

import java.util.Optional;

public interface UserProfileCommandService {
    Optional<UserProfile> handle(CreateUserProfileCommand command);
    Optional<ProfileId> handle(UpdateUserProfileCommand command);
}
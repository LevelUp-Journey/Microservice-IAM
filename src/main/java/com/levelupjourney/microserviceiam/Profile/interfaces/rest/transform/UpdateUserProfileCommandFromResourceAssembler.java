package com.levelupjourney.microserviceiam.Profile.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.Profile.domain.model.commands.UpdateUserProfileCommand;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.Profile.interfaces.rest.resources.UpdateUserProfileResource;

import java.util.UUID;

public class UpdateUserProfileCommandFromResourceAssembler {
    
    public static UpdateUserProfileCommand toCommandFromResource(UpdateUserProfileResource resource, UUID accountId) {
        return new UpdateUserProfileCommand(
                new AccountId(accountId),
                null, // Username cannot be changed via profile endpoint
                resource.name() != null ? new DisplayName(resource.name()) : null,
                resource.avatarUrl() != null ? new AvatarUrl(resource.avatarUrl()) : null
        );
    }
}
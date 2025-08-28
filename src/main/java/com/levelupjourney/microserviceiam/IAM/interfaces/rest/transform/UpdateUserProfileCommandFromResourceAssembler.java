package com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.IAM.domain.model.commands.UpdateUserProfileCommand;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.UpdateUserProfileResource;

import java.util.UUID;

public class UpdateUserProfileCommandFromResourceAssembler {
    public static UpdateUserProfileCommand toCommandFromResource(UUID userId, UpdateUserProfileResource resource) {
        return new UpdateUserProfileCommand(
            userId,
            resource.username(),
            resource.name(),
            resource.avatarUrl(),
            resource.password()
        );
    }
}
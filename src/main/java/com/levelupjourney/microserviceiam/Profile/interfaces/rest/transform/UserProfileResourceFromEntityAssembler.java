package com.levelupjourney.microserviceiam.Profile.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.interfaces.rest.resources.UserProfileResource;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class UserProfileResourceFromEntityAssembler {
    
    public static UserProfileResource toResourceFromEntity(UserProfile userProfile) {
        return new UserProfileResource(
                userProfile.getId(),
                userProfile.getAccountId().value(),
                userProfile.getUsername().value(),
                userProfile.getName().value(),
                userProfile.getAvatarUrl().url(),
                userProfile.getRoles(),
                userProfile.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                userProfile.getUpdatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
    }
}
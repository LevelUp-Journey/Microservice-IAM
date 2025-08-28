package com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(user.getId(), user.getUsername(), token);
    }
}

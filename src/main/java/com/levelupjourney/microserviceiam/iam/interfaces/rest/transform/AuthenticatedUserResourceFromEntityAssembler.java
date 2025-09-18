package com.levelupjourney.microserviceiam.iam.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token, String refreshToken) {
        return new AuthenticatedUserResource(user.getId(), user.getEmail_address(), token, refreshToken);
    }
}

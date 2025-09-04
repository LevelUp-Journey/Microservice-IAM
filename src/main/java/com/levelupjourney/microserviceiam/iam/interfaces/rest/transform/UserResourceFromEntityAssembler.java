package com.levelupjourney.microserviceiam.iam.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user) {
        return new UserResource(user.getId(), user.getEmail_address());
    }
}

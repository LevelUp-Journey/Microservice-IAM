package com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user) {
        var roles = user.getRoles().stream().map(Role::getStringName).toList();
        return new UserResource(user.getId(), user.getUsername(), user.getName(), user.getAvatarUrl(), roles);
    }
}

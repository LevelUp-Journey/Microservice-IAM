package com.levelupjourney.microserviceiam.iam.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.resources.RoleResource;

public class RoleResourceFromEntityAssembler {
    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}

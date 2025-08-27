package com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.SignUpUserResource;

public class SignUpUserResourceFromEntityAssembler {
    public static SignUpUserResource toResourceFromEntity(User user) {
        return new SignUpUserResource(user.getId(), user.getUsername());
    }
}
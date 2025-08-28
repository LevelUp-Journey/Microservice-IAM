package com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.SignUpResource;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(resource.email(), resource.username(), resource.password());
    }
}

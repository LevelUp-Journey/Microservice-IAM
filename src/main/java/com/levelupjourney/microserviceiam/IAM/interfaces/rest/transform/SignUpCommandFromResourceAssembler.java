package com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.EmailAddress;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.SignUpResource;

import java.util.Set;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(
            new EmailAddress(resource.email()),
            new Username(resource.username()),
            resource.password(),
            Set.of(Role.getDefaultRole())
        );
    }
}
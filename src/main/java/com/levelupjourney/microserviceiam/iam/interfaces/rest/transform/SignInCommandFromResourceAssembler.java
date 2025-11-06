package com.levelupjourney.microserviceiam.iam.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.iam.domain.model.commands.SignInCommand;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource signInResource) {
        return new SignInCommand(signInResource.email(), signInResource.password());
    }
}

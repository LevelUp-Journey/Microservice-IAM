package com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.AuthenticatedUserResource;

import java.util.stream.Collectors;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(Account account, String accessToken, String refreshToken) {
        return new AuthenticatedUserResource(
            account.getAccountId().value(),
            account.getUsername().value(),
            account.getEmail().email(),
            account.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
            accessToken,
            refreshToken
        );
    }
}
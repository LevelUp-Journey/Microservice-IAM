package com.levelupjourney.microserviceiam.IAM.interfaces.rest.transform;

import java.util.stream.Collectors;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;
import com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources.AuthenticatedUserResource;

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
    
    public static AuthenticatedUserResource toResourceFromEntityWithoutTokens(Account account) {
        return new AuthenticatedUserResource(
            account.getAccountId().value(),
            account.getUsername().value(),
            account.getEmail().email(),
            account.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
            null,
            null
        );
    }
}
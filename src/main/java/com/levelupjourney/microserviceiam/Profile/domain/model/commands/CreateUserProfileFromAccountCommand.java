package com.levelupjourney.microserviceiam.Profile.domain.model.commands;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.*;

import java.util.Set;

public record CreateUserProfileFromAccountCommand(
        AccountId accountId,
        PublicUsername username,
        DisplayName name,
        AvatarUrl avatarUrl,
        Set<String> roles
) {
    public CreateUserProfileFromAccountCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Roles cannot be null or empty");
        }
    }
}
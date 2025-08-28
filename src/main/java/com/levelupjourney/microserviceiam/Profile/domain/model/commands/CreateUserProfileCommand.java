package com.levelupjourney.microserviceiam.Profile.domain.model.commands;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AvatarUrl;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.DisplayName;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.PublicUsername;

import java.util.Set;

public record CreateUserProfileCommand(
    AccountId accountId,
    PublicUsername username,
    DisplayName name,
    AvatarUrl avatarUrl,
    Set<String> roles
) {
    public CreateUserProfileCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username is required");
        }
        if (name == null) {
            name = DisplayName.empty();
        }
        if (avatarUrl == null) {
            avatarUrl = AvatarUrl.empty();
        }
        if (roles == null) {
            roles = Set.of();
        }
    }
}
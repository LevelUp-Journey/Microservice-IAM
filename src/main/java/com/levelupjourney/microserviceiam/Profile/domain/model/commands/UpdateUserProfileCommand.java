package com.levelupjourney.microserviceiam.Profile.domain.model.commands;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AvatarUrl;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.DisplayName;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.PublicUsername;

public record UpdateUserProfileCommand(
    AccountId accountId,
    PublicUsername username,
    DisplayName name,
    AvatarUrl avatarUrl
) {
    public UpdateUserProfileCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
    }
}
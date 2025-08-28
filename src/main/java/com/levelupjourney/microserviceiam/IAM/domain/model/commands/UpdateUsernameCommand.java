package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;

public record UpdateUsernameCommand(
    AccountId accountId,
    Username newUsername
) {
    public UpdateUsernameCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (newUsername == null) {
            throw new IllegalArgumentException("New username is required");
        }
    }
}
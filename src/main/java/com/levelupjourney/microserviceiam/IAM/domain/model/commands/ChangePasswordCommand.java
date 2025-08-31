package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;

public record ChangePasswordCommand(
    AccountId accountId,
    String currentPassword,
    String newPassword
) {
    public ChangePasswordCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
    }
}
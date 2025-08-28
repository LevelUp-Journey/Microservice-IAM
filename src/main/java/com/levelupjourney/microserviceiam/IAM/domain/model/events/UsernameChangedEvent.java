package com.levelupjourney.microserviceiam.IAM.domain.model.events;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UsernameChangedEvent {
    private final AccountId accountId;
    private final Username oldUsername;
    private final Username newUsername;
    private final LocalDateTime occurredAt;

    public UsernameChangedEvent(AccountId accountId, Username oldUsername, Username newUsername) {
        this.accountId = accountId;
        this.oldUsername = oldUsername;
        this.newUsername = newUsername;
        this.occurredAt = LocalDateTime.now();
    }
}
package com.levelupjourney.microserviceiam.IAM.domain.model.events;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountDeactivatedEvent {
    private final AccountId accountId;
    private final LocalDateTime occurredAt;

    public AccountDeactivatedEvent(AccountId accountId) {
        this.accountId = accountId;
        this.occurredAt = LocalDateTime.now();
    }
}
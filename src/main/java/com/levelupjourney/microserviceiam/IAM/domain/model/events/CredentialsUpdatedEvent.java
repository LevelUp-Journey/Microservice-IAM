package com.levelupjourney.microserviceiam.IAM.domain.model.events;

import lombok.Getter;

import java.time.LocalDateTime;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;

@Getter
public class CredentialsUpdatedEvent {
    private final AccountId accountId;
    private final LocalDateTime changedAt;
    private final LocalDateTime occurredAt;

    public CredentialsUpdatedEvent(AccountId accountId, LocalDateTime changedAt) {
        this.accountId = accountId;
        this.changedAt = changedAt;
        this.occurredAt = LocalDateTime.now();
    }
}
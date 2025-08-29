package com.levelupjourney.microserviceiam.IAM.domain.model.events;

import lombok.Getter;

import java.time.LocalDateTime;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;

@Getter
public class AccountRegisteredEvent {
    private final AccountId accountId;
    private final String method;
    private final Username username;
    private final LocalDateTime occurredAt;

    public AccountRegisteredEvent(AccountId accountId, String method, Username username) {
        this.accountId = accountId;
        this.method = method;
        this.username = username;
        this.occurredAt = LocalDateTime.now();
    }
}
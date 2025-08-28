package com.levelupjourney.microserviceiam.Profile.domain.model.events;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.ProfileId;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.PublicUsername;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserProfileCreatedEvent {
    private final ProfileId profileId;
    private final AccountId accountId;
    private final PublicUsername username;
    private final LocalDateTime occurredAt;

    public UserProfileCreatedEvent(ProfileId profileId, AccountId accountId, PublicUsername username) {
        this.profileId = profileId;
        this.accountId = accountId;
        this.username = username;
        this.occurredAt = LocalDateTime.now();
    }
}
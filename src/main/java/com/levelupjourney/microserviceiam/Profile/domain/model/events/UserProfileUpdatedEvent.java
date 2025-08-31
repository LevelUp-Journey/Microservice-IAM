package com.levelupjourney.microserviceiam.Profile.domain.model.events;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.ProfileId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class UserProfileUpdatedEvent {
    private final ProfileId profileId;
    private final AccountId accountId;
    private final AccountId actorId;
    private final Map<String, String> changedFields;
    private final LocalDateTime occurredAt;

    public UserProfileUpdatedEvent(ProfileId profileId, AccountId accountId, AccountId actorId, Map<String, String> changedFields) {
        this.profileId = profileId;
        this.accountId = accountId;
        this.actorId = actorId;
        this.changedFields = Map.copyOf(changedFields);
        this.occurredAt = LocalDateTime.now();
    }
}
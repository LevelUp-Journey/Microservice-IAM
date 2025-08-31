package com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ProfileId(UUID value) {
    public ProfileId {
        if (value == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
    }

    public static ProfileId generate() {
        return new ProfileId(UUID.randomUUID());
    }

    public static ProfileId from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Profile ID string cannot be null or empty");
        }
        try {
            return new ProfileId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + value);
        }
    }
}
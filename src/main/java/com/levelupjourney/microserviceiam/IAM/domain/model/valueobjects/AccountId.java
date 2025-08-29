package com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record AccountId(UUID value) {
    public AccountId {
        if (value == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account ID string cannot be null or empty");
        }
        try {
            return new AccountId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + value);
        }
    }
}
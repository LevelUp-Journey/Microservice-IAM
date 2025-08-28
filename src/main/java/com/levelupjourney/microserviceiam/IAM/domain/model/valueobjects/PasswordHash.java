package com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record PasswordHash(String hash) {
    public PasswordHash {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        if (hash.length() < 60) {
            throw new IllegalArgumentException("Password hash appears to be invalid");
        }
    }

    public static PasswordHash of(String plainPassword, String hashedPassword) {
        return new PasswordHash(hashedPassword);
    }
}
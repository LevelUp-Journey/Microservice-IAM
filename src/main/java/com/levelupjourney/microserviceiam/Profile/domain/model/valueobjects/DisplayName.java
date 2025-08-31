package com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record DisplayName(String value) {
    public DisplayName {
        if (value != null && value.length() > 100) {
            throw new IllegalArgumentException("Display name cannot exceed 100 characters");
        }
    }

    public static DisplayName empty() {
        return new DisplayName("");
    }

    public boolean isEmpty() {
        return value == null || value.isBlank();
    }
}
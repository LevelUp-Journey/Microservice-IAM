package com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record Username(String username) {

    // Username must ALWAYS be USER + exactly 9 digits (13 characters total)
    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^USER\\d{9}$");
    private static final int EXPECTED_LENGTH = 13; // USER + 9 digits
    private static final String INVALID_USERNAME_MESSAGE = "Username must start with 'USER' followed by exactly 9 digits (total 13 characters)";

    public Username() {
        this(null);
    }

    public Username {
        validateUsername(username);
    }

    /**
     * Validate username format - ALWAYS must be USER + 9 digits
     */
    private static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        String trimmedUsername = username.trim();
        
        if (trimmedUsername.length() != EXPECTED_LENGTH) {
            throw new IllegalArgumentException("Username must be exactly " + EXPECTED_LENGTH + " characters long");
        }

        if (!VALID_USERNAME_PATTERN.matcher(trimmedUsername).matches()) {
            throw new IllegalArgumentException(INVALID_USERNAME_MESSAGE);
        }
    }

    @Override
    public String toString() {
        return username;
    }
}

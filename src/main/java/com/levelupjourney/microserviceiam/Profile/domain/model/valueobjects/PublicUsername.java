package com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.Random;
import java.util.regex.Pattern;

@Embeddable
public record PublicUsername(String value) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.\\-]{3,32}$");
    private static final String USER_PREFIX = "user";
    private static final Random RANDOM = new Random();

    public PublicUsername {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Username must be 3-32 characters and contain only letters, numbers, dots, underscores, and hyphens");
        }
    }

    public static PublicUsername generateRandomUsername() {
        String randomNumbers = String.valueOf(100000000 + RANDOM.nextInt(900000000));
        return new PublicUsername(USER_PREFIX + randomNumbers);
    }

    public boolean isGenerated() {
        return value.startsWith(USER_PREFIX) && value.length() == 12;
    }
}
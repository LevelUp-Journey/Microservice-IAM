package com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public record PersonName(String firstName, String lastName) {

    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[A-Za-zÁáÉéÍíÓóÚúÑñÜü\\s\\-]{1,50}$");
    private static final String INVALID_NAME_MESSAGE = "Name must contain only letters, accents, spaces, and hyphens, with maximum 50 characters";

    public PersonName() {
        this(null, null);
    }

    public PersonName {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
    }

    private static void validateName(String name, String fieldName) {
        // Allow null values for OAuth2 registration
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        
        if (!VALID_NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new IllegalArgumentException(fieldName + ": " + INVALID_NAME_MESSAGE);
        }
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}

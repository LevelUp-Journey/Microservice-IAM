package com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import org.apache.logging.log4j.util.Strings;

@Embeddable
public record PersonName(String firstName, String lastName) {

    public PersonName() {
        this(Strings.EMPTY, Strings.EMPTY);
    }

    public PersonName {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

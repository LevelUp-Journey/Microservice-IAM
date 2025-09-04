package com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import org.apache.logging.log4j.util.Strings;

@Embeddable
public record Username(String username) {

    public Username() {
        this(Strings.EMPTY);
    }
    public Username {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return username;
    }
}

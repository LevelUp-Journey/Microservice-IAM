package com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import org.apache.logging.log4j.util.Strings;

@Embeddable
public record EmailAddress(@Email String address) {

    public EmailAddress() {
        this(Strings.EMPTY);
    }
    public EmailAddress {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return address;
    }
}

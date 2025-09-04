package com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record StreetAddress(
        String street,
        String number,
        String city,
        String state,
        String postalCode,
        String country) {

    public StreetAddress {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or blank");
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Number cannot be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be null or blank");
        }

        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("State cannot be null or blank");
        }

        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code cannot be null or blank");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be null or blank");
        }
    }

    public String getStreetAddress() {
        return "%s %s, %s, %s, %s, %s".formatted(street, number, city, state, postalCode, country);
    }

    @Override
    public String toString() {
        return getStreetAddress();
    }
}

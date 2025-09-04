package com.levelupjourney.microserviceiam.profiles.interfaces.rest.resources;

/**
 * Resource for creating a profile.
 */
public record CreateProfileResource(
        String firstName,
        String lastName,
        String username,
        String street,
        String number,
        String city,
        String state,
        String postalCode,
        String country) {
    /**
     * Validates the resource.
     *
     * @throws IllegalArgumentException if the resource is invalid.
     */
    public CreateProfileResource {
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("First name is required");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Last name is required");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street is required");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City is required");
        if (state == null || state.isBlank()) throw new IllegalArgumentException("State is required");
    }
}

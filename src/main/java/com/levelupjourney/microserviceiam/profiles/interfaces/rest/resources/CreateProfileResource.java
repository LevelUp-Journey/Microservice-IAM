package com.levelupjourney.microserviceiam.profiles.interfaces.rest.resources;

/**
 * Resource for creating a profile.
 */
public record CreateProfileResource(
        String firstName,
        String lastName,
        String username,
        String profileUrl) {
    /**
     * Validates the resource.
     *
     * @throws IllegalArgumentException if the resource is invalid.
     */
    public CreateProfileResource {
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("First name is required");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Last name is required");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
    }
}

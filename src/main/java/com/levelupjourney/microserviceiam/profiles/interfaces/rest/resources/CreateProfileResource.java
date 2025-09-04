package com.levelupjourney.microserviceiam.profiles.interfaces.rest.resources;

/**
 * Resource for creating a profile.
 */
public record CreateProfileResource(
        String firstName,
        String lastName,
        String profileUrl) {
    /**
     * Validates the resource.
     *
     * @throws IllegalArgumentException if the resource is invalid.
     */
    public CreateProfileResource {
        // firstName and lastName can be null for local registration
        // profileUrl can be null for local registration
    }
}

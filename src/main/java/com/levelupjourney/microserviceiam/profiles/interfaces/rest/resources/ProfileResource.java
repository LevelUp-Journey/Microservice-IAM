package com.levelupjourney.microserviceiam.profiles.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource for a profile.
 */
public record ProfileResource(
        UUID id,
        String fullName,
        String username,
        String streetAddress) {
}

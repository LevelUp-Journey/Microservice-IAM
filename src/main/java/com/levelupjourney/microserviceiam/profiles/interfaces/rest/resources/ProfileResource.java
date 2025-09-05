package com.levelupjourney.microserviceiam.profiles.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource for a profile.
 */
public record ProfileResource(
        UUID id,
        String username,
        String profileUrl,
        String firstName,
        String lastName) {
}

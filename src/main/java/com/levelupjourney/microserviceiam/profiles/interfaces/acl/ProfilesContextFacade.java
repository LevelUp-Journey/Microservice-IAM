package com.levelupjourney.microserviceiam.profiles.interfaces.acl;

import java.util.UUID;

/**
 * ProfilesContextFacade
 */
public interface ProfilesContextFacade {
    /**
     * Create a new profile
     * @param firstName The first name (can be null for local registration)
     * @param lastName The last name (can be null for local registration)
     * @param profileUrl The profile URL (can be null for local registration)
     * @return The profile ID
     */
    UUID createProfile(String firstName, String lastName, String profileUrl);

    /**
     * Fetch a profile ID by username
     * @param username The username
     * @return The profile ID
     */
    UUID fetchProfileIdByUsername(String username);
}

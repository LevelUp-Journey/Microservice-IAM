package com.levelupjourney.microserviceiam.profiles.interfaces.acl;

import java.util.UUID;

/**
 * ProfilesContextFacade
 */
public interface ProfilesContextFacade {
    /**
     * Create a new profile
     * @param firstName The first name
     * @param lastName The last name
     * @param username The username
     * @param profileUrl The profile URL
     * @return The profile ID
     */
    UUID createProfile(String firstName, String lastName, String username, String profileUrl);

    /**
     * Fetch a profile ID by username
     * @param username The username
     * @return The profile ID
     */
    UUID fetchProfileIdByUsername(String username);
}

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
     * @param street The street address
     * @param number The street number
     * @param city The city
     * @param state The  state(optional)
     * @param postalCode The postal code
     * @param country The country
     * @return The profile ID
     */
    UUID createProfile(String firstName, String lastName, String username, String street, String number, String city, String state, String postalCode, String country);

    /**
     * Fetch a profile ID by username
     * @param username The username
     * @return The profile ID
     */
    UUID fetchProfileIdByUsername(String username);
}

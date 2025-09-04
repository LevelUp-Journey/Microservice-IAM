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
     * @param email The email address
     * @param street The street address
     * @param number The street number
     * @param city The city
     * @param state The  state(optional)
     * @param postalCode The postal code
     * @param country The country
     * @return The profile ID
     */
    UUID createProfile(String firstName, String lastName, String email, String street, String number, String city, String state, String postalCode, String country);

    /**
     * Fetch a profile ID by email
     * @param email The email address
     * @return The profile ID
     */
    UUID fetchProfileIdByEmail(String email);
}

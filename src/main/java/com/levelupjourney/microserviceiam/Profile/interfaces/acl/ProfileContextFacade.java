package com.levelupjourney.microserviceiam.Profile.interfaces.acl;

import java.util.UUID;

public interface ProfileContextFacade {
    
    /**
     * Updates the username in the Profile context
     * @param accountId The account ID
     * @param newUsername The new username
     * @return true if successful, false otherwise
     */
    boolean updateProfileUsername(UUID accountId, String newUsername);
    
    /**
     * Updates the display name in the Profile context
     * @param accountId The account ID
     * @param newDisplayName The new display name
     * @return true if successful, false otherwise
     */
    boolean updateProfileDisplayName(UUID accountId, String newDisplayName);
    
    /**
     * Gets profile information by account ID
     * @param accountId The account ID
     * @return Profile information or null if not found
     */
    ProfileInfo getProfileByAccountId(UUID accountId);
    
    /**
     * Simple profile information DTO for ACL communication
     */
    record ProfileInfo(
        UUID profileId,
        UUID accountId,
        String username,
        String displayName,
        String avatarUrl
    ) {}
}
package com.levelupjourney.microserviceiam.IAM.interfaces.acl;

import java.util.Set;
import java.util.UUID;

public interface IamContextFacade {
    
    /**
     * Updates the username in the IAM context
     * @param accountId The account ID
     * @param newUsername The new username
     * @return The account ID if successful, null otherwise
     */
    UUID updateAccountUsername(UUID accountId, String newUsername);
    
    /**
     * Changes the password for an account
     * @param accountId The account ID
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return The account ID if successful, null otherwise
     */
    UUID changeAccountPassword(UUID accountId, String currentPassword, String newPassword);
    
    /**
     * Gets account information by account ID
     * @param accountId The account ID
     * @return Account information or null if not found
     */
    AccountInfo getAccountById(UUID accountId);
    
    /**
     * Gets roles for an account
     * @param accountId The account ID
     * @return Set of role names
     */
    Set<String> getAccountRoles(UUID accountId);
    
    /**
     * Simple account information DTO for ACL communication
     */
    record AccountInfo(
        UUID accountId,
        String username,
        String email,
        Set<String> roles,
        boolean isActive
    ) {}
}
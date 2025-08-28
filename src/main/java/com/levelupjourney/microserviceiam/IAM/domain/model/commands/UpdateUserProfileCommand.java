package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

import java.util.UUID;

/**
 * Update user profile command
 * <p>
 *     This class represents the command to update a user profile.
 *     Only non-null fields will be updated.
 * </p>
 * @param userId the ID of the user to update
 * @param username the new username (optional)
 * @param name the new name (optional)
 * @param avatarUrl the new avatar URL (optional)
 * @param password the new password (optional)
 */
public record UpdateUserProfileCommand(
    UUID userId,
    String username,
    String name,
    String avatarUrl,
    String password
) {
}
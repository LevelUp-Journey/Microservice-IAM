package com.levelupjourney.microserviceiam.iam.domain.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event representing a user registration in the IAM system
 * This event is published when a user successfully registers via OAuth2
 *
 * @author LevelUp Journey Team
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    /**
     * Unique identifier of the user in IAM
     */
    private UUID userId;

    /**
     * Email address of the registered user
     */
    private String email;

    /**
     * First name extracted from OAuth2 provider
     */
    private String firstName;

    /**
     * Last name extracted from OAuth2 provider
     */
    private String lastName;

    /**
     * Profile URL/avatar from OAuth2 provider
     */
    private String profileUrl;

    /**
     * OAuth2 provider name (e.g., "google", "github")
     */
    private String provider;

    /**
     * Timestamp when the registration occurred
     */
    private LocalDateTime registeredAt;
}

package com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects;

/**
 * Authentication providers enum
 * <p>
 *     This enum represents the authentication providers supported by the system.
 * </p>
 */
public enum AuthProvider {
    LOCAL,      // Username/password authentication
    GOOGLE,     // Google OAuth2
    GITHUB      // GitHub OAuth2 (future)
}
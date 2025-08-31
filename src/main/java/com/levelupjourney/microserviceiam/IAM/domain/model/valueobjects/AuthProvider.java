package com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record AuthProvider(String provider) {
    public static final String LOCAL = "local";
    public static final String GOOGLE = "google";
    public static final String GITHUB = "github";

    public AuthProvider {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Auth provider cannot be null or empty");
        }
        if (!isValidProvider(provider)) {
            throw new IllegalArgumentException("Invalid auth provider: " + provider);
        }
    }

    private boolean isValidProvider(String provider) {
        return LOCAL.equals(provider) || GOOGLE.equals(provider) || GITHUB.equals(provider);
    }

    public boolean isLocal() {
        return LOCAL.equals(provider);
    }

    public boolean isGoogle() {
        return GOOGLE.equals(provider);
    }

    public boolean isGitHub() {
        return GITHUB.equals(provider);
    }

    public static AuthProvider local() {
        return new AuthProvider(LOCAL);
    }

    public static AuthProvider google() {
        return new AuthProvider(GOOGLE);
    }

    public static AuthProvider github() {
        return new AuthProvider(GITHUB);
    }
}
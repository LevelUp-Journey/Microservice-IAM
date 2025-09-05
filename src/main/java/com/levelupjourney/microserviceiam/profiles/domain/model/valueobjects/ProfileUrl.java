package com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.net.URI;
import java.net.URISyntaxException;

@Embeddable
public record ProfileUrl(String url) {

    private static final int MAX_LENGTH = 255;

    public ProfileUrl {
        validateUrl(url);
    }

    private static void validateUrl(String url) {
        // Allow null values for local registration
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        String trimmedUrl = url.trim();
        
        if (trimmedUrl.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Profile URL cannot exceed " + MAX_LENGTH + " characters");
        }

        try {
            URI validatedUri = new URI(trimmedUrl);
            String scheme = validatedUri.getScheme();
            if (scheme == null || (!"http".equals(scheme) && !"https".equals(scheme))) {
                throw new IllegalArgumentException("Profile URL must use http or https protocol");
            }
            if (validatedUri.getHost() == null) {
                throw new IllegalArgumentException("Profile URL must have a valid host");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + e.getMessage());
        }
    }

    public String value() {
        return url;
    }
}
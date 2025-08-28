package com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.net.URI;
import java.net.URISyntaxException;

@Embeddable
public record AvatarUrl(String url) {
    public AvatarUrl {
        if (url != null && !url.isBlank()) {
            try {
                new URI(url).toURL();
            } catch (URISyntaxException | java.net.MalformedURLException e) {
                throw new IllegalArgumentException("Invalid URL format: " + url);
            }
            
            if (url.length() > 500) {
                throw new IllegalArgumentException("Avatar URL cannot exceed 500 characters");
            }
        }
    }

    public static AvatarUrl empty() {
        return new AvatarUrl("");
    }

    public boolean isEmpty() {
        return url == null || url.isBlank();
    }
}
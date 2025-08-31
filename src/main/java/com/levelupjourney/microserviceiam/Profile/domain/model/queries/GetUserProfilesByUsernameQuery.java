package com.levelupjourney.microserviceiam.Profile.domain.model.queries;

public record GetUserProfilesByUsernameQuery(String searchTerm, Integer page, Integer pageSize) {
    public GetUserProfilesByUsernameQuery {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        
        if (page == null || page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        
        if (pageSize == null || pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
    }
}
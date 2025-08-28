package com.levelupjourney.microserviceiam.Profile.domain.model.queries;

public record GetUserProfilesByRoleQuery(String role, Integer page, Integer pageSize) {
    public GetUserProfilesByRoleQuery {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        
        if (page == null || page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        
        if (pageSize == null || pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
    }
}
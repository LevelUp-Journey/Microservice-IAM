package com.levelupjourney.microserviceiam.Profile.domain.model.queries;

import java.util.Optional;

public record GetAllUserProfilesQuery(
    Optional<Integer> page,
    Optional<Integer> pageSize,
    Optional<String> searchQuery
) {
    public GetAllUserProfilesQuery {
        if (page.isPresent() && page.get() < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (pageSize.isPresent() && pageSize.get() <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (pageSize.isPresent() && pageSize.get() > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }
    
    public GetAllUserProfilesQuery() {
        this(Optional.of(0), Optional.of(10), Optional.empty());
    }
    
    public GetAllUserProfilesQuery(int page, int pageSize) {
        this(Optional.of(page), Optional.of(pageSize), Optional.empty());
    }
    
    public GetAllUserProfilesQuery(int page, int pageSize, String searchQuery) {
        this(Optional.of(page), Optional.of(pageSize), Optional.ofNullable(searchQuery));
    }
}
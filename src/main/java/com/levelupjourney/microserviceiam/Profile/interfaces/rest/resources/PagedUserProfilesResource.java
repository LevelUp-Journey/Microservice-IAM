package com.levelupjourney.microserviceiam.Profile.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated user profiles response")
public record PagedUserProfilesResource(
        @Schema(description = "List of user profiles")
        @JsonProperty("items")
        List<UserProfileResource> items,
        
        @Schema(description = "Current page number", example = "0")
        @JsonProperty("page")
        Integer page,
        
        @Schema(description = "Page size", example = "10")
        @JsonProperty("page_size")
        Integer pageSize,
        
        @Schema(description = "Total number of items", example = "150")
        @JsonProperty("total")
        Long total,
        
        @Schema(description = "Total number of pages", example = "15")
        @JsonProperty("total_pages")
        Integer totalPages
) {}
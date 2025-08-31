package com.levelupjourney.microserviceiam.Profile.interfaces.rest.transform;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.interfaces.rest.resources.PagedUserProfilesResource;
import com.levelupjourney.microserviceiam.Profile.interfaces.rest.resources.UserProfileResource;
import org.springframework.data.domain.Page;

import java.util.List;

public class PagedUserProfilesResourceFromPageAssembler {
    
    public static PagedUserProfilesResource toResourceFromPage(Page<UserProfile> page) {
        List<UserProfileResource> items = page.getContent()
                .stream()
                .map(UserProfileResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        
        return new PagedUserProfilesResource(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
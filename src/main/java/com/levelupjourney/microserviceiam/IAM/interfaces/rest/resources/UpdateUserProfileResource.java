package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileResource(
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    String name,
    
    @Pattern(regexp = "^(https?://|ftp://).*", message = "Avatar URL must be a valid URL starting with http://, https://, or ftp://")
    String avatarUrl,
    
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {
}
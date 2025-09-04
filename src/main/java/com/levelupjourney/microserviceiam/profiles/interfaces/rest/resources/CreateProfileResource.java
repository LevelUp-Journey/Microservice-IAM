package com.levelupjourney.microserviceiam.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * Resource for creating a profile.
 */
public record CreateProfileResource(
        @Pattern(regexp = "^[A-Za-zÁáÉéÍíÓóÚúÑñÜü\\s\\-]*$", 
                 message = "First name must contain only letters, accents, spaces, and hyphens")
        @Size(max = 50, message = "First name cannot exceed 50 characters")
        @Schema(description = "User's first name (supports international characters)", example = "María")
        String firstName,
        
        @Pattern(regexp = "^[A-Za-zÁáÉéÍíÓóÚúÑñÜü\\s\\-]*$", 
                 message = "Last name must contain only letters, accents, spaces, and hyphens")
        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        @Schema(description = "User's last name (supports international characters)", example = "González-López")
        String lastName,
        
        @URL(message = "Profile URL must be a valid HTTP or HTTPS URL")
        @Size(max = 255, message = "Profile URL cannot exceed 255 characters")
        @Schema(description = "User's profile URL (optional)", example = "https://github.com/username")
        String profileUrl) {
}

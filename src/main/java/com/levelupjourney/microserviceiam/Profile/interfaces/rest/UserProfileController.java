package com.levelupjourney.microserviceiam.Profile.interfaces.rest;

import com.levelupjourney.microserviceiam.Profile.domain.model.queries.*;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.Profile.domain.services.*;
import com.levelupjourney.microserviceiam.Profile.interfaces.rest.resources.*;
import com.levelupjourney.microserviceiam.Profile.interfaces.rest.transform.*;
import com.levelupjourney.microserviceiam.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Profiles", description = "User profile management endpoints")
public class UserProfileController {
    
    private final UserProfileQueryService userProfileQueryService;
    private final UserProfileCommandService userProfileCommandService;
    
    public UserProfileController(UserProfileQueryService userProfileQueryService, 
                               UserProfileCommandService userProfileCommandService) {
        this.userProfileQueryService = userProfileQueryService;
        this.userProfileCommandService = userProfileCommandService;
    }
    
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update current user profile", description = "Updates the profile of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or username already taken"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UpdateUserProfileResource resource,
                                           Authentication authentication) {
        try {
            UUID accountId = extractAccountIdFromAuthentication(authentication);
            
            var command = UpdateUserProfileCommandFromResourceAssembler
                    .toCommandFromResource(resource, accountId);
            
            var result = userProfileCommandService.handle(command);
            
            if (result.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResource("Failed to update profile"));
            }
            
            return ResponseEntity.ok(new MessageResource("Profile updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "VALIDATION_ERROR",
                            "message", e.getMessage(),
                            "details", Map.of("field", "unknown")
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResource("Failed to update profile: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user by ID", description = "Returns a specific user profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        try {
            var query = new GetUserProfileByIdQuery(new ProfileId(userId));
            var userProfile = userProfileQueryService.handle(query);
            
            if (userProfile.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            var resource = UserProfileResourceFromEntityAssembler
                    .toResourceFromEntity(userProfile.get());
            
            return ResponseEntity.ok(resource);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new MessageResource("Failed to retrieve user: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search users by username", description = "Returns a user profile by exact username match - Username is unique across the application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found or not found"),
        @ApiResponse(responseCode = "400", description = "Invalid username parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchUserByUsername(
            @Parameter(description = "Exact username to search for", example = "john_doe", required = true)
            @RequestParam String q) {

        try {
            if (q == null || q.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new MessageResource("Username parameter 'q' is required and cannot be empty"));
            }

            var query = new GetUserProfileByUsernameQuery(new PublicUsername(q.trim()));
            var userProfile = userProfileQueryService.handle(query);

            if (userProfile.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "found", false,
                    "message", "No user found with username: " + q,
                    "user", null
                ));
            }

            var resource = UserProfileResourceFromEntityAssembler
                    .toResourceFromEntity(userProfile.get());

            return ResponseEntity.ok(Map.of(
                "found", true,
                "message", "User found",
                "user", resource
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new MessageResource("Invalid username format: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new MessageResource("Failed to search user: " + e.getMessage()));
        }
    }
    
    private UUID extractAccountIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String accountId = jwt.getSubject();
            return UUID.fromString(accountId);
        }
        throw new IllegalArgumentException("Invalid authentication principal - JWT required");
    }
}
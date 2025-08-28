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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
            // TODO: Extract accountId from JWT token instead of authentication principal
            // For now, we'll use a placeholder
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
    
    @GetMapping
    @Operation(summary = "List users", description = "Returns a paginated list of user profiles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PagedUserProfilesResource> getUsers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            
            @Parameter(description = "Search query for username or name", example = "john")
            @RequestParam(required = false) String q) {
        
        try {
            var query = new GetAllUserProfilesQuery(
                    java.util.Optional.ofNullable(page),
                    java.util.Optional.ofNullable(pageSize),
                    java.util.Optional.ofNullable(q)
            );
            
            var userProfiles = userProfileQueryService.handle(query);
            var resource = PagedUserProfilesResourceFromPageAssembler.toResourceFromPage(userProfiles);
            
            return ResponseEntity.ok(resource);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Returns a specific user profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
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
    @Operation(summary = "Search users by username", description = "Returns users matching the username search term")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PagedUserProfilesResource> searchUsersByUsername(
            @Parameter(description = "Username search term", example = "john")
            @RequestParam String q,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            var query = new GetUserProfilesByUsernameQuery(q, page, pageSize);
            var userProfiles = userProfileQueryService.handle(query);
            var resource = PagedUserProfilesResourceFromPageAssembler.toResourceFromPage(userProfiles);
            
            return ResponseEntity.ok(resource);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/by-role/{role}")
    @Operation(summary = "Get users by role", description = "Returns users with the specified role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role or pagination parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PagedUserProfilesResource> getUsersByRole(
            @Parameter(description = "Role name", example = "STUDENT")
            @PathVariable String role,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            var query = new GetUserProfilesByRoleQuery(role, page, pageSize);
            var userProfiles = userProfileQueryService.handle(query);
            var resource = PagedUserProfilesResourceFromPageAssembler.toResourceFromPage(userProfiles);
            
            return ResponseEntity.ok(resource);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private UUID extractAccountIdFromAuthentication(Authentication authentication) {
        // TODO: Implement proper JWT token parsing to extract account ID
        // For now, return a placeholder UUID
        // In a real implementation, you would:
        // 1. Extract the JWT token from the Authentication object
        // 2. Parse the token to get the account ID claim
        // 3. Return the account ID as UUID
        
        throw new UnsupportedOperationException("JWT token parsing not yet implemented");
    }
}
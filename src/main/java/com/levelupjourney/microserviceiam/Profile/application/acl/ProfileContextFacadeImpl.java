package com.levelupjourney.microserviceiam.Profile.application.acl;

import com.levelupjourney.microserviceiam.Profile.domain.model.commands.CreateUserProfileFromAccountCommand;
import com.levelupjourney.microserviceiam.Profile.domain.model.commands.UpdateUserProfileCommand;
import com.levelupjourney.microserviceiam.Profile.domain.model.queries.GetUserProfileByAccountIdQuery;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.PublicUsername;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.DisplayName;
import com.levelupjourney.microserviceiam.Profile.domain.services.UserProfileCommandService;
import com.levelupjourney.microserviceiam.Profile.domain.services.UserProfileQueryService;
import com.levelupjourney.microserviceiam.Profile.interfaces.acl.ProfileContextFacade;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.UUID;

@Service
public class ProfileContextFacadeImpl implements ProfileContextFacade {
    
    private final UserProfileCommandService userProfileCommandService;
    private final UserProfileQueryService userProfileQueryService;
    
    public ProfileContextFacadeImpl(UserProfileCommandService userProfileCommandService,
                                   UserProfileQueryService userProfileQueryService) {
        this.userProfileCommandService = userProfileCommandService;
        this.userProfileQueryService = userProfileQueryService;
    }
    
    @Override
    public boolean updateProfileUsername(UUID accountId, String newUsername) {
        try {
            System.out.println("ProfileACL: Starting username update for account: " + accountId + " with new username: " + newUsername);
            
            // Check if profile exists first
            var query = new GetUserProfileByAccountIdQuery(new AccountId(accountId));
            var existingProfile = userProfileQueryService.handle(query);
            
            if (existingProfile.isEmpty()) {
                System.out.println("ProfileACL: No existing profile found for account " + accountId + ", creating new profile");
                // Profile doesn't exist, create it with the new username
                var createCommand = new CreateUserProfileFromAccountCommand(
                    new AccountId(accountId),
                    new PublicUsername(newUsername),
                    null, // no display name initially
                    null, // no avatar initially  
                    new HashSet<>() // empty roles, will be synced later if needed
                );
                var createResult = userProfileCommandService.handle(createCommand);
                boolean success = createResult.isPresent();
                System.out.println("ProfileACL: Profile creation result: " + success);
                return success;
            } else {
                System.out.println("ProfileACL: Found existing profile for account " + accountId + ", updating username");
                // Profile exists, update it
                var updateCommand = new UpdateUserProfileCommand(
                    new AccountId(accountId),
                    new PublicUsername(newUsername),
                    null,
                    null
                );
                var updateResult = userProfileCommandService.handle(updateCommand);
                boolean success = updateResult.isPresent();
                System.out.println("ProfileACL: Profile update result: " + success);
                return success;
            }
        } catch (Exception e) {
            System.err.println("ProfileACL: Error updating profile username for account " + accountId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateProfileDisplayName(UUID accountId, String newDisplayName) {
        try {
            var command = new UpdateUserProfileCommand(
                new AccountId(accountId),
                null,
                new DisplayName(newDisplayName),
                null
            );
            var result = userProfileCommandService.handle(command);
            return result.isPresent();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public ProfileInfo getProfileByAccountId(UUID accountId) {
        try {
            var query = new GetUserProfileByAccountIdQuery(new AccountId(accountId));
            var profile = userProfileQueryService.handle(query);
            
            return profile.map(p -> new ProfileInfo(
                p.getProfileId().value(),
                p.getAccountId().value(),
                p.getUsername().value(),
                p.getName() != null ? p.getName().value() : null,
                p.getAvatarUrl() != null ? p.getAvatarUrl().url() : null
            )).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
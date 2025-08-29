package com.levelupjourney.microserviceiam.Profile.application.internal.commandservices;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.domain.model.commands.*;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.ProfileId;
import com.levelupjourney.microserviceiam.Profile.domain.services.UserProfileCommandService;
import com.levelupjourney.microserviceiam.Profile.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.levelupjourney.microserviceiam.IAM.interfaces.acl.IamContextFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserProfileCommandServiceImpl implements UserProfileCommandService {
    
    private final UserProfileRepository userProfileRepository;
    private final IamContextFacade iamContextFacade;
    
    public UserProfileCommandServiceImpl(UserProfileRepository userProfileRepository,
                                        @Lazy IamContextFacade iamContextFacade) {
        this.userProfileRepository = userProfileRepository;
        this.iamContextFacade = iamContextFacade;
    }
    
    @Override
    @Transactional
    public Optional<UserProfile> handle(CreateUserProfileCommand command) {
        if (userProfileRepository.existsByAccountId(command.accountId())) {
            throw new IllegalArgumentException("User profile already exists for this account");
        }
        
        if (userProfileRepository.existsByUsername(command.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        UserProfile userProfile = new UserProfile(
            command.accountId(),
            command.username(),
            command.name(),
            command.avatarUrl(),
            command.roles()
        );
        
        UserProfile savedProfile = userProfileRepository.save(userProfile);
        return Optional.of(savedProfile);
    }
    
    @Override
    @Transactional
    public Optional<UserProfile> handle(CreateUserProfileFromAccountCommand command) {
        if (userProfileRepository.existsByAccountId(command.accountId())) {
            throw new IllegalArgumentException("User profile already exists for this account");
        }
        
        if (userProfileRepository.existsByUsername(command.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        UserProfile userProfile = new UserProfile(
            command.accountId(),
            command.username(),
            command.name(),
            command.avatarUrl(),
            command.roles()
        );
        
        UserProfile savedProfile = userProfileRepository.save(userProfile);
        return Optional.of(savedProfile);
    }
    
    @Override
    @Transactional
    public Optional<ProfileId> handle(UpdateUserProfileCommand command) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByAccountId(command.accountId());
        
        if (profileOpt.isEmpty()) {
            throw new IllegalArgumentException("User profile not found");
        }
        
        UserProfile userProfile = profileOpt.get();
        boolean usernameChanged = false;
        
        // Check username uniqueness if it's being changed
        if (command.username() != null && !userProfile.getUsername().equals(command.username())) {
            if (userProfileRepository.existsByUsername(command.username())) {
                throw new IllegalArgumentException("Username already exists");
            }
            usernameChanged = true;
        }
        
        userProfile.updateProfile(command.username(), command.name(), command.avatarUrl(), command.accountId());
        
        userProfileRepository.save(userProfile);
        
        // Sync display name and avatar changes with IAM context for OAuth2 users
        // (but NOT username to avoid circular dependencies)
        boolean nameOrAvatarChanged = (command.name() != null && !userProfile.getName().equals(command.name())) ||
                                     (command.avatarUrl() != null && !userProfile.getAvatarUrl().equals(command.avatarUrl()));
                                     
        if (nameOrAvatarChanged) {
            try {
                // Only sync name/avatar, never username
                boolean synced = iamContextFacade.updateExternalIdentityProfile(
                    command.accountId().value(),
                    command.name() != null ? command.name().value() : null,
                    command.avatarUrl() != null ? command.avatarUrl().url() : null
                );
                
                if (!synced) {
                    System.err.println("Warning: Failed to sync profile changes with IAM ExternalIdentity for account: " + command.accountId().value());
                }
            } catch (Exception e) {
                System.err.println("Warning: Exception syncing profile changes with IAM for account " + command.accountId().value() + ": " + e.getMessage());
            }
        }
        
        return Optional.of(userProfile.getProfileId());
    }
}
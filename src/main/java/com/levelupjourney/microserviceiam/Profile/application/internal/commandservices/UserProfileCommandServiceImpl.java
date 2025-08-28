package com.levelupjourney.microserviceiam.Profile.application.internal.commandservices;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.domain.model.commands.CreateUserProfileCommand;
import com.levelupjourney.microserviceiam.Profile.domain.model.commands.UpdateUserProfileCommand;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.ProfileId;
import com.levelupjourney.microserviceiam.Profile.domain.services.UserProfileCommandService;
import com.levelupjourney.microserviceiam.Profile.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserProfileCommandServiceImpl implements UserProfileCommandService {
    
    private final UserProfileRepository userProfileRepository;
    
    public UserProfileCommandServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
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
    public Optional<ProfileId> handle(UpdateUserProfileCommand command) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByAccountId(command.accountId());
        
        if (profileOpt.isEmpty()) {
            throw new IllegalArgumentException("User profile not found");
        }
        
        UserProfile userProfile = profileOpt.get();
        
        // Check username uniqueness if it's being changed
        if (command.username() != null && !userProfile.getUsername().equals(command.username())) {
            if (userProfileRepository.existsByUsername(command.username())) {
                throw new IllegalArgumentException("Username already exists");
            }
        }
        
        userProfile.updateProfile(command.username(), command.name(), command.avatarUrl(), command.accountId());
        
        userProfileRepository.save(userProfile);
        return Optional.of(userProfile.getProfileId());
    }
}
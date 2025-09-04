package com.levelupjourney.microserviceiam.profiles.application.internal.commandservices;

import com.levelupjourney.microserviceiam.profiles.domain.model.aggregates.Profile;
import com.levelupjourney.microserviceiam.profiles.domain.model.commands.CreateProfileCommand;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.profiles.domain.services.ProfileCommandService;
import com.levelupjourney.microserviceiam.profiles.infrastructure.persistence.jpa.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Profile Command Service Implementation
 */
@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {
    private final ProfileRepository profileRepository;

    /**
     * Constructor
     *
     * @param profileRepository The {@link ProfileRepository} instance
     */
    public ProfileCommandServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // inherited javadoc
    @Override
    public Optional<Profile> handle(CreateProfileCommand command) {
        var username = new Username(command.username());
        if (profileRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Profile with username already exists");
        }
        var profile = new Profile(command);
        profileRepository.save(profile);
        return Optional.of(profile);
    }
}

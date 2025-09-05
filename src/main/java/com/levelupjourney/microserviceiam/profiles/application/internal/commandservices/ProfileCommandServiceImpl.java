package com.levelupjourney.microserviceiam.profiles.application.internal.commandservices;

import com.levelupjourney.microserviceiam.profiles.domain.model.aggregates.Profile;
import com.levelupjourney.microserviceiam.profiles.domain.model.commands.CreateProfileCommand;
import com.levelupjourney.microserviceiam.profiles.domain.services.ProfileCommandService;
import com.levelupjourney.microserviceiam.profiles.domain.services.UsernameGeneratorService;
import com.levelupjourney.microserviceiam.profiles.infrastructure.persistence.jpa.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Profile Command Service Implementation
 */
@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {
    private final ProfileRepository profileRepository;
    private final UsernameGeneratorService usernameGeneratorService;

    /**
     * Constructor
     *
     * @param profileRepository The {@link ProfileRepository} instance
     * @param usernameGeneratorService The {@link UsernameGeneratorService} instance
     */
    public ProfileCommandServiceImpl(ProfileRepository profileRepository,
                                   UsernameGeneratorService usernameGeneratorService) {
        this.profileRepository = profileRepository;
        this.usernameGeneratorService = usernameGeneratorService;
    }

    // inherited javadoc
    @Override
    public Optional<Profile> handle(CreateProfileCommand command) {
        var username = usernameGeneratorService.generateUniqueUsername();
        var profile = new Profile(command, username);
        profileRepository.save(profile);
        return Optional.of(profile);
    }
}

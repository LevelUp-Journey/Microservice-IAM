package com.levelupjourney.microserviceiam.profiles.application.internal.queryservices;

import com.levelupjourney.microserviceiam.profiles.domain.model.aggregates.Profile;
import com.levelupjourney.microserviceiam.profiles.domain.model.queries.GetAllProfilesQuery;
import com.levelupjourney.microserviceiam.profiles.domain.model.queries.GetProfileByemail_addressQuery;
import com.levelupjourney.microserviceiam.profiles.domain.model.queries.GetProfileByIdQuery;
import com.levelupjourney.microserviceiam.profiles.domain.services.ProfileQueryService;
import com.levelupjourney.microserviceiam.profiles.infrastructure.persistence.jpa.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Profile Query Service Implementation
 */
@Service
public class ProfileQueryServiceImpl implements ProfileQueryService {
    private final ProfileRepository profileRepository;

    /**
     * Constructor
     *
     * @param profileRepository The {@link ProfileRepository} instance
     */
    public ProfileQueryServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // inherited javadoc
    @Override
    public Optional<Profile> handle(GetProfileByIdQuery query) {
        return profileRepository.findById(query.profileId());
    }

    // inherited javadoc
    @Override
    public Optional<Profile> handle(GetProfileByemail_addressQuery query) {
        return profileRepository.findByUsername(query.username());
    }

    // inherited javadoc
    @Override
    public List<Profile> handle(GetAllProfilesQuery query) {
        return profileRepository.findAll();
    }
}

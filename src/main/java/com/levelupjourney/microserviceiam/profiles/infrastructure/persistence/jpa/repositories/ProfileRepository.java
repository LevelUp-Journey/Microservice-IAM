package com.levelupjourney.microserviceiam.profiles.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.profiles.domain.model.aggregates.Profile;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.Username;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Profile Repository
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    /**
     * Find a Profile by Username
     *
     * @param username The Username
     * @return A {@link Profile} instance if the username is valid, otherwise empty
     */
    Optional<Profile> findByUsername(Username username);

    /**
     * Check if a Profile exists by Username
     *
     * @param username The Username
     * @return True if the username exists, otherwise false
     */
    boolean existsByUsername(Username username);
}

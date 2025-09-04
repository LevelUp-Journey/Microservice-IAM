package com.levelupjourney.microserviceiam.profiles.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.profiles.domain.model.aggregates.Profile;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.EmailAddress;
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
     * Find a Profile by Email Address
     *
     * @param emailAddress The Email Address
     * @return A {@link Profile} instance if the email address is valid, otherwise empty
     */
    Optional<Profile> findByEmailAddress(EmailAddress emailAddress);

    /**
     * Check if a Profile exists by Email Address
     *
     * @param emailAddress The Email Address
     * @return True if the email address exists, otherwise false
     */
    boolean existsByEmailAddress(EmailAddress emailAddress);
}

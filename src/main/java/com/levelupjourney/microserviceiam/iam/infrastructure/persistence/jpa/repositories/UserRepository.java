package com.levelupjourney.microserviceiam.iam.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * This interface is responsible for providing the User entity related operations.
 * It extends the JpaRepository interface.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>
{
    /**
     * This method is responsible for finding the user by email_address.
     * @param email_address The email_address.
     * @return The user object.
     */
    Optional<User> findByEmail_address(String email_address);

    /**
     * This method is responsible for checking if the user exists by email_address.
     * @param email_address The email_address.
     * @return True if the user exists, false otherwise.
     */
    boolean existsByEmail_address(String email_address);

}

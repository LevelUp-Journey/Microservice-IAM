package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * This interface is responsible for providing the User entity related operations.
 * It extends the JpaRepository interface.
 */
@Repository
public interface UserRepository extends JpaRepository<User, java.util.UUID>
{
    /**
     * This method is responsible for finding the user by username.
     * @param username The username.
     * @return The user object.
     */
    Optional<User> findByUsername(String username);

    /**
     * This method is responsible for checking if the user exists by username.
     * @param username The username.
     * @return True if the user exists, false otherwise.
     */
    boolean existsByUsername(String username);

    /**
     * This method is responsible for finding the user by email.
     * @param email The email.
     * @return The user object.
     */
    @Query("SELECT u FROM User u JOIN u.emails e WHERE e.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * This method is responsible for checking if the user exists by email.
     * @param email The email.
     * @return True if the user exists, false otherwise.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.emails e WHERE e.email = :email")
    boolean existsByEmail(@Param("email") String email);

}

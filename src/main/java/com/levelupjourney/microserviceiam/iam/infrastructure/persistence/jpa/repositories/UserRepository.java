package com.levelupjourney.microserviceiam.iam.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * This method is responsible for finding the user by email.
     * @param email The email.
     * @return The user object.
     */
    @Query("SELECT u FROM User u WHERE u.emailAddress.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * This method is responsible for checking if the user exists by email.
     * @param email The email.
     * @return True if the user exists, false otherwise.
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.emailAddress.email = :email")
    boolean existsByEmail(@Param("email") String email);

}

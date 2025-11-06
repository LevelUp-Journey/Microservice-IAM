package com.levelupjourney.microserviceiam.iam.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * This interface is responsible for providing the Role entity related operations.
 * It extends the JpaRepository interface.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * This method is responsible for finding the role by name.
     * @param name The role name.
     * @return The role object.
     */
    Optional<Role> findByName(Roles name);

    /**
     * This method is responsible for checking if the role exists by name.
     * @param name The role name.
     * @return True if the role exists, false otherwise.
     */
    boolean existsByName(Roles name);

}

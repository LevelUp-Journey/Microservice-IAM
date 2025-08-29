package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Get role UUID by name
     */
    @Query("SELECT r.id FROM Role r WHERE r.name = :name")
    Optional<UUID> findRoleIdByName(String name);
    
    /**
     * Get default role (STUDENT)
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'STUDENT'")
    Optional<Role> findDefaultRole();
    
    /**
     * Get default role UUID (STUDENT)
     */
    @Query("SELECT r.id FROM Role r WHERE r.name = 'STUDENT'")
    Optional<UUID> findDefaultRoleId();
}
package com.levelupjourney.microserviceiam.IAM.application.internal.services;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing role entities and conversions
 */
@Service
public class RoleManagementService {
    
    private final RoleRepository roleRepository;
    
    public RoleManagementService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    /**
     * Get role entity by role enum
     */
    public Optional<Role> getRoleEntity(com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role roleEnum) {
        return roleRepository.findByName(roleEnum.getName());
    }
    
    /**
     * Get role entities from role enums
     */
    public Set<Role> getRoleEntities(Set<com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role> roleEnums) {
        return roleEnums.stream()
            .map(this::getRoleEntity)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get role enum from role entity
     */
    public Optional<com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role> getRoleEnum(Role roleEntity) {
        try {
            return Optional.of(com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role.fromString(roleEntity.getName()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Get role enums from role entities
     */
    public Set<com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role> getRoleEnums(Set<Role> roleEntities) {
        return roleEntities.stream()
            .map(this::getRoleEnum)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get default role entity (STUDENT)
     */
    public Role getDefaultRoleEntity() {
        return roleRepository.findDefaultRole()
            .orElseThrow(() -> new RuntimeException("Default role (STUDENT) not found in database"));
    }
    
    /**
     * Get role entity by UUID
     */
    public Optional<Role> getRoleById(UUID roleId) {
        return roleRepository.findById(roleId);
    }
    
    /**
     * Get role entity by name
     */
    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByName(roleName);
    }
}
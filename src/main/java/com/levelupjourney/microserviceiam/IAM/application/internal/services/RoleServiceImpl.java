package com.levelupjourney.microserviceiam.IAM.application.internal.services;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Roles;
import com.levelupjourney.microserviceiam.IAM.domain.services.RoleService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role service implementation
 * <p>
 *     This service manages Role entities and ensures uniqueness.
 * </p>
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    @Override
    public Role getOrCreateDefaultRole() {
        return getOrCreateRole(Roles.STUDENT);
    }
    
    @Override
    public Role getOrCreateRole(Roles roleName) {
        // Try to find existing role
        var existingRole = roleRepository.findByName(roleName);
        
        if (existingRole.isPresent()) {
            return existingRole.get();
        }
        
        // Create new role if it doesn't exist
        Role newRole = new Role(roleName);
        return roleRepository.save(newRole);
    }
}
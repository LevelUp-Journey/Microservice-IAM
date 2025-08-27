package com.levelupjourney.microserviceiam.IAM.domain.services;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Roles;

/**
 * Role service
 * <p>
 *     This service manages Role entities and ensures uniqueness.
 * </p>
 */
public interface RoleService {
    
    /**
     * Get or create the default role (STUDENT)
     * @return the default role
     */
    Role getOrCreateDefaultRole();
    
    /**
     * Get or create a role by name
     * @param roleName the role name
     * @return the role
     */
    Role getOrCreateRole(Roles roleName);
}
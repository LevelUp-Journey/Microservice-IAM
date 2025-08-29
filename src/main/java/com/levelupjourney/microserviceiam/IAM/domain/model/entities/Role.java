package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

/**
 * Role entity representing the 3 fixed application roles
 * Only 3 roles exist: STUDENT, ADMIN, INSTRUCTOR
 * UUIDs are auto-generated when database is created but remain fixed thereafter
 */
@Getter
@Entity
@Table(name = "roles")
public class Role extends AuditableModel {
    
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    public Role() {}
    
    public Role(String name) {
        this.name = name;
        this.isActive = true;
    }
    
    /**
     * Get the role ID from the inherited id field
     */
    public UUID getRoleId() {
        return getId();
    }
    
    /**
     * Creates the STUDENT role
     */
    public static Role createStudentRole() {
        return new Role("STUDENT");
    }
    
    /**
     * Creates the ADMIN role
     */
    public static Role createAdminRole() {
        return new Role("ADMIN");
    }
    
    /**
     * Creates the INSTRUCTOR role
     */
    public static Role createInstructorRole() {
        return new Role("INSTRUCTOR");
    }
}
package com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects;

/**
 * Role enum representing the 3 fixed application roles
 * The actual UUIDs are stored in the database roles table
 */
public enum Role {
    STUDENT("STUDENT"),
    ADMIN("ADMIN"),
    INSTRUCTOR("INSTRUCTOR");

    private final String name;

    Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Role fromString(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        
        for (Role role : Role.values()) {
            if (role.name.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        
        throw new IllegalArgumentException("Unknown role: " + roleName);
    }

    public static Role getDefaultRole() {
        return STUDENT;
    }
}
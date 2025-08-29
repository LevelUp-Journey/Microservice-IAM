package com.levelupjourney.microserviceiam.IAM.application.internal.services;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

/**
 * Service responsible for seeding the 3 fixed roles in the database
 * This runs at application startup to ensure the roles exist
 */
@Service
public class RoleSeederService {
    
    private static final Logger logger = Logger.getLogger(RoleSeederService.class.getName());
    
    private final RoleRepository roleRepository;
    
    public RoleSeederService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    @PostConstruct
    @Transactional
    public void seedRoles() {
        logger.info("Starting role seeding process...");
        
        // Seed STUDENT role
        if (!roleRepository.existsByName("STUDENT")) {
            Role studentRole = Role.createStudentRole();
            roleRepository.save(studentRole);
            logger.info("Created STUDENT role with ID: " + studentRole.getRoleId());
        } else {
            logger.info("STUDENT role already exists");
        }
        
        // Seed ADMIN role
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = Role.createAdminRole();
            roleRepository.save(adminRole);
            logger.info("Created ADMIN role with ID: " + adminRole.getRoleId());
        } else {
            logger.info("ADMIN role already exists");
        }
        
        // Seed INSTRUCTOR role
        if (!roleRepository.existsByName("INSTRUCTOR")) {
            Role instructorRole = Role.createInstructorRole();
            roleRepository.save(instructorRole);
            logger.info("Created INSTRUCTOR role with ID: " + instructorRole.getRoleId());
        } else {
            logger.info("INSTRUCTOR role already exists");
        }
        
        logger.info("Role seeding process completed. Total roles in database: " + roleRepository.count());
        
        // Log all role UUIDs for reference
        roleRepository.findAll().forEach(role -> 
            logger.info(String.format("Role: %s - UUID: %s", role.getName(), role.getRoleId()))
        );
    }
}
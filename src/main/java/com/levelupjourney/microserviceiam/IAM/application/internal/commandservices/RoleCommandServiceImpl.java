package com.levelupjourney.microserviceiam.IAM.application.internal.commandservices;


import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SeedRolesCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Roles;
import com.levelupjourney.microserviceiam.IAM.domain.services.RoleCommandService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Implementation of {@link RoleCommandService} to handle {@link SeedRolesCommand}
 */
@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * This method will handle the {@link SeedRolesCommand} and will create the roles if not exists
     * @param command {@link SeedRolesCommand}
     * @see SeedRolesCommand
     */
    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values()).forEach(role -> {
            if(!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(Roles.valueOf(role.name())));
            }
        } );
    }
}

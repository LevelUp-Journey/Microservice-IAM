package com.levelupjourney.microserviceiam.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Configuration
 * This class configures JPA for PostgreSQL-based bounded contexts.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories"
    }
)
@EntityScan(
    basePackages = {
        "com.levelupjourney.microserviceiam.IAM.domain.model"
    }
)
public class JpaConfiguration {
    
}
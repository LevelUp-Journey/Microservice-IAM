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
        "com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories",
        "com.levelupjourney.microserviceiam.Profile.infrastructure.persistence.jpa.repositories"
    }
)
@EntityScan(
    basePackages = {
        "com.levelupjourney.microserviceiam.IAM.domain.model",
        "com.levelupjourney.microserviceiam.Profile.domain.model",
        "com.levelupjourney.microserviceiam.shared.domain.model"
    }
)
public class JpaConfiguration {
    
}
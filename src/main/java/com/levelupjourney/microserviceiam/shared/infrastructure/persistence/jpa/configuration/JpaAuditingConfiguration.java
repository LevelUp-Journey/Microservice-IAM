package com.levelupjourney.microserviceiam.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JpaAuditingConfiguration
 * This class enables JPA auditing for automatic population of audit fields.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}

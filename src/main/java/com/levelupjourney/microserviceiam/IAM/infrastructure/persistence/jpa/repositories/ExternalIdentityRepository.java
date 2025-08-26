package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.ExternalIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity, UUID> {
    
    Optional<ExternalIdentity> findByProviderAndProviderUserId(String provider, String providerUserId);
    
    Optional<ExternalIdentity> findByUserIdAndProvider(UUID userId, String provider);
    
    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}
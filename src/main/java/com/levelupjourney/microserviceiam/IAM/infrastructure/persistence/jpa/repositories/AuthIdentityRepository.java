package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.AuthIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, UUID> {
    
    Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
    
    Optional<AuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider);
    
    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
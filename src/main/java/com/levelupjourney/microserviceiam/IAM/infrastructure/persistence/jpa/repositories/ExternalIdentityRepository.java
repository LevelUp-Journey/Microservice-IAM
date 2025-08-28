package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.ExternalIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity, UUID> {
    
    Optional<ExternalIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
    
    List<ExternalIdentity> findByAccountId(AccountId accountId);
    
    List<ExternalIdentity> findByAccountIdAndProvider(AccountId accountId, AuthProvider provider);
    
    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
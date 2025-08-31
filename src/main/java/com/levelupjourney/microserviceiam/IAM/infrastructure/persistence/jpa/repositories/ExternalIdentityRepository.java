package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.ExternalIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity, UUID> {
    
    Optional<ExternalIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
    
    @Query("SELECT e FROM ExternalIdentity e WHERE e.account.id = :accountId")
    List<ExternalIdentity> findByAccountId(@Param("accountId") UUID accountId);
    
    @Query("SELECT e FROM ExternalIdentity e WHERE e.account.id = :accountId AND e.provider = :provider")
    List<ExternalIdentity> findByAccountIdAndProvider(@Param("accountId") UUID accountId, @Param("provider") AuthProvider provider);
    
    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
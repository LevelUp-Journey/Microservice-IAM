package com.levelupjourney.microserviceiam.Profile.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.Profile.domain.model.entities.ProfileAudit;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileAuditRepository extends JpaRepository<ProfileAudit, UUID> {
    
    List<ProfileAudit> findByAccountIdOrderByChangedAtDesc(AccountId accountId);
    
    Page<ProfileAudit> findByAccountId(AccountId accountId, Pageable pageable);
    
    @Query("SELECT pa FROM ProfileAudit pa WHERE pa.accountId = :accountId AND pa.changedAt BETWEEN :startDate AND :endDate")
    List<ProfileAudit> findByAccountIdAndDateRange(
        @Param("accountId") AccountId accountId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT pa FROM ProfileAudit pa WHERE pa.field = :field AND pa.accountId = :accountId ORDER BY pa.changedAt DESC")
    List<ProfileAudit> findByFieldAndAccountId(@Param("field") String field, @Param("accountId") AccountId accountId);
}
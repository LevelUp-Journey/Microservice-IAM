package com.levelupjourney.microserviceiam.Profile.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.Profile.domain.model.entities.ProfileAudit;
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
    
    List<ProfileAudit> findByProfileIdOrderByChangedAtDesc(UUID profileId);
    
    Page<ProfileAudit> findByProfileId(UUID profileId, Pageable pageable);
    
    @Query("SELECT pa FROM ProfileAudit pa WHERE pa.profileId = :profileId AND pa.changedAt BETWEEN :startDate AND :endDate")
    List<ProfileAudit> findByProfileIdAndDateRange(
        @Param("profileId") UUID profileId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT pa FROM ProfileAudit pa WHERE pa.field = :field AND pa.profileId = :profileId ORDER BY pa.changedAt DESC")
    List<ProfileAudit> findByFieldAndProfileId(@Param("field") String field, @Param("profileId") UUID profileId);
}
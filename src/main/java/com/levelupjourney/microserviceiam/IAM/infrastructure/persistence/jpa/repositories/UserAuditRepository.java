package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserAuditRepository extends JpaRepository<UserAudit, UUID> {

    List<UserAudit> findByUserIdOrderByChangeTimestampDesc(UUID userId);

    List<UserAudit> findByUserIdAndFieldNameOrderByChangeTimestampDesc(UUID userId, String fieldName);

    List<UserAudit> findByChangedByUserIdOrderByChangeTimestampDesc(UUID changedByUserId);

    @Query("SELECT ua FROM UserAudit ua WHERE ua.user.id = :userId AND ua.changeTimestamp BETWEEN :startDate AND :endDate ORDER BY ua.changeTimestamp DESC")
    List<UserAudit> findByUserIdAndChangeTimestampBetween(@Param("userId") UUID userId, 
                                                         @Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);

    long countByUserIdAndChangeTimestampAfter(UUID userId, LocalDateTime after);
}
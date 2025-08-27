package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserSession;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<UserSession> findByUserIdAndSuccessOrderByCreatedAtDesc(UUID userId, Boolean success);

    List<UserSession> findByUserIdAndAuthProviderOrderByCreatedAtDesc(UUID userId, AuthProvider authProvider);

    List<UserSession> findByUserIdAndSessionTypeOrderByCreatedAtDesc(UUID userId, String sessionType);

    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<UserSession> findByUserIdAndDateRange(@Param("userId") UUID userId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM UserSession s WHERE s.ipAddress = :ipAddress AND s.createdAt > :since ORDER BY s.createdAt DESC")
    List<UserSession> findRecentSessionsByIpAddress(@Param("ipAddress") String ipAddress, 
                                                     @Param("since") LocalDateTime since);

    long countByUserIdAndSuccessAndCreatedAtAfter(UUID userId, Boolean success, LocalDateTime after);
}
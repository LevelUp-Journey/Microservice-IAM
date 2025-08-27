package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserEmail;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserEmailRepository extends JpaRepository<UserEmail, UUID> {

    Optional<UserEmail> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEmail> findByUserIdOrderByIsPrimaryDescCreatedAtAsc(UUID userId);

    Optional<UserEmail> findByUserIdAndIsPrimary(UUID userId, Boolean isPrimary);

    List<UserEmail> findByUserIdAndIsVerified(UUID userId, Boolean isVerified);

    List<UserEmail> findByVerifiedByProvider(AuthProvider provider);

    Optional<UserEmail> findByVerificationToken(String verificationToken);

    @Query("SELECT ue FROM UserEmail ue WHERE ue.user.id = :userId AND ue.email = :email")
    Optional<UserEmail> findByUserIdAndEmail(@Param("userId") UUID userId, @Param("email") String email);

    @Query("SELECT ue FROM UserEmail ue WHERE ue.isVerified = false AND ue.verificationSentAt < :cutoffDate")
    List<UserEmail> findUnverifiedEmailsOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    long countByUserIdAndIsVerified(UUID userId, Boolean isVerified);
}
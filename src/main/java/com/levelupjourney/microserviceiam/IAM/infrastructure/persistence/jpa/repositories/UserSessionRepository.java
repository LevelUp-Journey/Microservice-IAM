package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserSession;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<UserSession> findByUserIdAndSuccessOrderByCreatedAtDesc(UUID userId, Boolean success);

    List<UserSession> findByUserIdAndAuthProviderOrderByCreatedAtDesc(UUID userId, AuthProvider authProvider);

    List<UserSession> findByUserIdAndSessionTypeOrderByCreatedAtDesc(UUID userId, String sessionType);

}
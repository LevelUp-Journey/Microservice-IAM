package com.levelupjourney.microserviceiam.Profile.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.PublicUsername;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
    Optional<UserProfile> findByAccountId(AccountId accountId);
    
    Optional<UserProfile> findByUsername(PublicUsername username);
    
    boolean existsByUsername(PublicUsername username);
    
    boolean existsByAccountId(AccountId accountId);
    
    @Query("SELECT up FROM UserProfile up WHERE " +
           "(:searchQuery IS NULL OR :searchQuery = '' OR " +
           "LOWER(up.username.value) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(up.name.value) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<UserProfile> findAllWithSearch(@Param("searchQuery") String searchQuery, Pageable pageable);
}
package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.EmailAddress;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    Optional<Account> findByEmail(EmailAddress email);
    
    Optional<Account> findByUsername(Username username);
    
    @Query("SELECT a FROM Account a WHERE a.email.email = :emailOrUsername OR a.username.value = :emailOrUsername")
    Optional<Account> findByEmailOrUsername(@Param("emailOrUsername") String emailOrUsername);
    
    boolean existsByEmail(EmailAddress email);
    
    boolean existsByUsername(Username username);
}
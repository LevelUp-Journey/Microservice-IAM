package com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.IamAudit;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IamAuditRepository extends JpaRepository<IamAudit, UUID> {
    
    List<IamAudit> findByAccountIdOrderByAuditCreatedAtDesc(AccountId accountId);
    
    Page<IamAudit> findByAccountId(AccountId accountId, Pageable pageable);
    
    List<IamAudit> findByAction(String action);
    
    List<IamAudit> findByAccountIdAndAction(AccountId accountId, String action);
    
    List<IamAudit> findByAuditCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    List<IamAudit> findByActorIdOrderByAuditCreatedAtDesc(AccountId actorId);
}
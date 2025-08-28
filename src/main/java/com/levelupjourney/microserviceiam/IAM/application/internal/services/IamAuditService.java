package com.levelupjourney.microserviceiam.IAM.application.internal.services;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.IamAudit;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.IamAuditRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IamAuditService {
    
    private final IamAuditRepository auditRepository;
    
    public IamAuditService(IamAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    
    public void auditSignUp(AccountId accountId, String userAgent) {
        IamAudit audit = new IamAudit(
            accountId, 
            IamAudit.Actions.SIGNED_UP,
            userAgent
        );
        auditRepository.save(audit);
    }
    
    public void auditSignIn(AccountId accountId, String userAgent) {
        IamAudit audit = new IamAudit(
            accountId, 
            IamAudit.Actions.SIGNED_IN,
            userAgent
        );
        auditRepository.save(audit);
    }
    
    public void auditOAuth2Link(AccountId accountId, String provider, String userAgent) {
        IamAudit audit = new IamAudit(
            accountId,
            accountId, 
            IamAudit.Actions.OAUTH_LINKED,
            userAgent,
            Map.of("provider", provider)
        );
        auditRepository.save(audit);
    }
    
    public void auditPasswordChange(AccountId actorId, AccountId accountId, String userAgent) {
        IamAudit audit = new IamAudit(
            actorId,
            accountId,
            IamAudit.Actions.PASSWORD_CHANGED,
            userAgent,
            Map.of()
        );
        auditRepository.save(audit);
    }
    
    public void auditUsernameChange(AccountId actorId, AccountId accountId, String oldUsername, String newUsername, String userAgent) {
        IamAudit audit = new IamAudit(
            actorId,
            accountId,
            IamAudit.Actions.USERNAME_CHANGED,
            userAgent,
            Map.of("oldUsername", oldUsername, "newUsername", newUsername)
        );
        auditRepository.save(audit);
    }
    
    public void auditRoleAssignment(AccountId actorId, AccountId accountId, String role, String userAgent) {
        IamAudit audit = new IamAudit(
            actorId,
            accountId,
            IamAudit.Actions.ROLE_ASSIGNED,
            userAgent,
            Map.of("role", role)
        );
        auditRepository.save(audit);
    }
    
    public void auditAccountDeactivation(AccountId actorId, AccountId accountId, String userAgent) {
        IamAudit audit = new IamAudit(
            actorId,
            accountId,
            IamAudit.Actions.ACCOUNT_DEACTIVATED,
            userAgent,
            Map.of()
        );
        auditRepository.save(audit);
    }
}
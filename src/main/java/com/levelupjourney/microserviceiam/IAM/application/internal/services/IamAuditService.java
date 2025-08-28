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
    
    public void auditSignUp(AccountId accountId, String ipAddress, String userAgent) {
        IamAudit audit = new IamAudit(
            accountId, 
            IamAudit.Actions.SIGNED_UP,
            ipAddress,
            userAgent
        );
        auditRepository.save(audit);
    }
    
    public void auditSignIn(AccountId accountId, String ipAddress, String userAgent) {
        IamAudit audit = new IamAudit(
            accountId, 
            IamAudit.Actions.SIGNED_IN,
            ipAddress,
            userAgent
        );
        auditRepository.save(audit);
    }
    
    public void auditOAuth2Link(AccountId accountId, String provider, String ipAddress, String userAgent) {
        IamAudit audit = new IamAudit(
            accountId,
            accountId, 
            IamAudit.Actions.OAUTH_LINKED,
            ipAddress,
            userAgent,
            Map.of("provider", provider)
        );
        auditRepository.save(audit);
    }
    
    public void auditPasswordChange(AccountId actorId, AccountId accountId, String ipAddress, String userAgent) {
        IamAudit audit = new IamAudit(
            actorId,
            accountId,
            IamAudit.Actions.PASSWORD_CHANGED,
            ipAddress,
            userAgent,
            Map.of()
        );
        auditRepository.save(audit);
    }
    
    public void auditUsernameChange(AccountId actorId, AccountId accountId, String oldUsername, String newUsername, String ipAddress, String userAgent) {
        IamAudit audit = new IamAudit(
            actorId,
            accountId,
            IamAudit.Actions.USERNAME_CHANGED,
            ipAddress,
            userAgent,
            Map.of("oldUsername", oldUsername, "newUsername", newUsername)
        );
        auditRepository.save(audit);
    }
    
    public void auditRoleAssignment(AccountId actorId, AccountId accountId, String role, String ipAddress, String userAgent) {
        IamAudit audit = new IamAudit(
            actorId,
            accountId,
            IamAudit.Actions.ROLE_ASSIGNED,
            ipAddress,
            userAgent,
            Map.of("role", role)
        );
        auditRepository.save(audit);
    }
    
    public void auditAccountDeactivation(AccountId actorId, AccountId accountId, String ipAddress, String userAgent) {
        IamAudit audit = new IamAudit(
            actorId,
            accountId,
            IamAudit.Actions.ACCOUNT_DEACTIVATED,
            ipAddress,
            userAgent,
            Map.of()
        );
        auditRepository.save(audit);
    }
}
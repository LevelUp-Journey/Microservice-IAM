package com.levelupjourney.microserviceiam.IAM.application.acl;

import org.springframework.stereotype.Service;

import com.levelupjourney.microserviceiam.IAM.application.internal.services.IamAuditService;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.ChangePasswordCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.UpdateUsernameCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.queries.GetAccountByIdQuery;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountCommandService;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountQueryService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.ExternalIdentityRepository;
import com.levelupjourney.microserviceiam.IAM.interfaces.acl.IamContextFacade;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IamContextFacadeImpl implements IamContextFacade {
    
    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;
    private final ExternalIdentityRepository externalIdentityRepository;
    private final IamAuditService iamAuditService;
    
    public IamContextFacadeImpl(AccountCommandService accountCommandService,
                               AccountQueryService accountQueryService,
                               ExternalIdentityRepository externalIdentityRepository,
                               IamAuditService iamAuditService) {
        this.accountCommandService = accountCommandService;
        this.accountQueryService = accountQueryService;
        this.externalIdentityRepository = externalIdentityRepository;
        this.iamAuditService = iamAuditService;
    }
    
    @Override
    public UUID updateAccountUsername(UUID accountId, String newUsername) {
        try {
            var command = new UpdateUsernameCommand(
                new AccountId(accountId),
                new Username(newUsername)
            );
            var result = accountCommandService.handle(command);
            return result.map(AccountId::value).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public UUID changeAccountPassword(UUID accountId, String currentPassword, String newPassword) {
        try {
            var command = new ChangePasswordCommand(
                new AccountId(accountId),
                currentPassword,
                newPassword
            );
            var result = accountCommandService.handle(command);
            return result.map(AccountId::value).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public AccountInfo getAccountById(UUID accountId) {
        try {
            var query = new GetAccountByIdQuery(new AccountId(accountId));
            var account = accountQueryService.handle(query);
            
            return account.map(acc -> new AccountInfo(
                acc.getAccountId().value(),
                acc.getUsername().value(),
                acc.getEmail().email(),
                acc.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                acc.isActive()
            )).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public Set<String> getAccountRoles(UUID accountId) {
        try {
            var query = new GetAccountByIdQuery(new AccountId(accountId));
            var account = accountQueryService.handle(query);
            
            return account.map(acc -> 
                acc.getRoles().stream()
                   .map(Role::getName)
                   .collect(Collectors.toSet())
            ).orElse(Set.of());
        } catch (Exception e) {
            return Set.of();
        }
    }
    
    @Override
    public boolean updateExternalIdentityProfile(UUID accountId, String displayName, String avatarUrl) {
        try {
            // Find external identities for this account
            var externalIdentities = externalIdentityRepository.findByAccountId(accountId);
            
            if (externalIdentities.isEmpty()) {
                // User has no external identities (local account), nothing to sync
                return true; // Consider it successful - no action needed
            }
            
            // Update all external identities for this account
            boolean updated = false;
            AccountId actorId = new AccountId(accountId); // Profile context acting on behalf of user
            AccountId targetAccountId = new AccountId(accountId);
            
            for (var externalIdentity : externalIdentities) {
                String provider = externalIdentity.getProvider().provider();
                
                if (displayName != null) {
                    String oldName = externalIdentity.getName();
                    externalIdentity.updateName(displayName);
                    updated = true;
                    
                    // Audit the name change
                    iamAuditService.auditExternalIdentityUpdate(
                        actorId, 
                        targetAccountId, 
                        provider, 
                        "name", 
                        oldName, 
                        displayName
                    );
                }
                if (avatarUrl != null) {
                    String oldAvatarUrl = externalIdentity.getAvatarUrl();
                    externalIdentity.updateAvatarUrl(avatarUrl);
                    updated = true;
                    
                    // Audit the avatar URL change
                    iamAuditService.auditExternalIdentityUpdate(
                        actorId, 
                        targetAccountId, 
                        provider, 
                        "avatarUrl", 
                        oldAvatarUrl, 
                        avatarUrl
                    );
                }
            }
            
            if (updated) {
                externalIdentityRepository.saveAll(externalIdentities);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating external identity profile for account " + accountId + ": " + e.getMessage());
            return false;
        }
    }
}
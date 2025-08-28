package com.levelupjourney.microserviceiam.IAM.application.acl;

import com.levelupjourney.microserviceiam.IAM.domain.model.commands.ChangePasswordCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.UpdateUsernameCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.queries.GetAccountByIdQuery;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountCommandService;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountQueryService;
import com.levelupjourney.microserviceiam.IAM.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IamContextFacadeImpl implements IamContextFacade {
    
    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;
    
    public IamContextFacadeImpl(AccountCommandService accountCommandService,
                               AccountQueryService accountQueryService) {
        this.accountCommandService = accountCommandService;
        this.accountQueryService = accountQueryService;
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
}
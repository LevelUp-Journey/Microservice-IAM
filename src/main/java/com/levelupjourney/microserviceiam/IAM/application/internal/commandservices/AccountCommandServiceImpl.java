package com.levelupjourney.microserviceiam.IAM.application.internal.commandservices;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.hashing.HashingService;
import com.levelupjourney.microserviceiam.IAM.application.internal.services.IamAuditService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.*;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountCommandService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.AccountRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.ExternalIdentityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountCommandServiceImpl implements AccountCommandService {
    
    private final AccountRepository accountRepository;
    private final ExternalIdentityRepository externalIdentityRepository;
    private final HashingService hashingService;
    private final IamAuditService auditService;
    
    public AccountCommandServiceImpl(AccountRepository accountRepository,
                                   ExternalIdentityRepository externalIdentityRepository,
                                   HashingService hashingService,
                                   IamAuditService auditService) {
        this.accountRepository = accountRepository;
        this.externalIdentityRepository = externalIdentityRepository;
        this.hashingService = hashingService;
        this.auditService = auditService;
    }
    
    @Override
    @Transactional
    public Optional<Account> handle(SignUpCommand command) {
        if (accountRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        if (accountRepository.existsByUsername(command.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        PasswordHash hashedPassword = new PasswordHash(hashingService.encode(command.password()));
        Account account = new Account(command.email(), command.username(), hashedPassword, command.roles());
        
        Account savedAccount = accountRepository.save(account);
        auditService.auditSignUp(savedAccount.getAccountId(), null, null);
        
        return Optional.of(savedAccount);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> handle(SignInCommand command) {
        Optional<Account> accountOpt = accountRepository.findByEmailOrUsername(command.emailOrUsername());
        
        if (accountOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Account account = accountOpt.get();
        
        if (!account.hasLocalCredentials()) {
            throw new IllegalArgumentException("Account does not have local credentials");
        }
        
        if (!account.isActive()) {
            throw new IllegalArgumentException("Account is not active");
        }
        
        if (!hashingService.matches(command.password(), account.getCredential().getPasswordHash().hash())) {
            return Optional.empty();
        }
        
        auditService.auditSignIn(account.getAccountId(), null, null);
        return Optional.of(account);
    }
    
    @Override
    @Transactional
    public Optional<Account> handle(OAuth2SignInCommand command) {
        var existingIdentity = externalIdentityRepository.findByProviderAndProviderUserId(
            command.provider(), command.providerUserId()
        );
        
        if (existingIdentity.isPresent()) {
            Optional<Account> account = accountRepository.findById(existingIdentity.get().getAccountId().value());
            if (account.isPresent()) {
                auditService.auditSignIn(account.get().getAccountId(), null, null);
                return account;
            }
        }
        
        if (accountRepository.existsByEmail(command.email())) {
            Optional<Account> existingAccount = accountRepository.findByEmail(command.email());
            if (existingAccount.isPresent()) {
                existingAccount.get().linkExternalIdentity(command.provider(), command.providerUserId(), command.attributes());
                Account savedAccount = accountRepository.save(existingAccount.get());
                auditService.auditOAuth2Link(savedAccount.getAccountId(), command.provider().provider(), null, null);
                return Optional.of(savedAccount);
            }
        }
        
        Username generatedUsername = Username.generateRandomUsername();
        while (accountRepository.existsByUsername(generatedUsername)) {
            generatedUsername = Username.generateRandomUsername();
        }
        
        Account newAccount = new Account(command.email(), generatedUsername, command.provider(), 
                                       command.providerUserId(), command.name(), command.attributes(), command.roles());
        
        Account savedAccount = accountRepository.save(newAccount);
        auditService.auditSignUp(savedAccount.getAccountId(), null, null);
        auditService.auditOAuth2Link(savedAccount.getAccountId(), command.provider().provider(), null, null);
        
        return Optional.of(savedAccount);
    }
    
    @Override
    @Transactional
    public Optional<AccountId> handle(ChangePasswordCommand command) {
        Optional<Account> accountOpt = accountRepository.findById(command.accountId().value());
        
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }
        
        Account account = accountOpt.get();
        
        if (!account.hasLocalCredentials()) {
            throw new IllegalArgumentException("Account does not have local credentials");
        }
        
        if (!hashingService.matches(command.currentPassword(), account.getCredential().getPasswordHash().hash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        PasswordHash newHashedPassword = new PasswordHash(hashingService.encode(command.newPassword()));
        account.changePassword(newHashedPassword);
        
        accountRepository.save(account);
        auditService.auditPasswordChange(command.accountId(), command.accountId(), null, null);
        
        return Optional.of(account.getAccountId());
    }
    
    @Override
    @Transactional
    public Optional<AccountId> handle(UpdateUsernameCommand command) {
        Optional<Account> accountOpt = accountRepository.findById(command.accountId().value());
        
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }
        
        if (accountRepository.existsByUsername(command.newUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        Account account = accountOpt.get();
        String oldUsername = account.getUsername().value();
        account.updateUsername(command.newUsername());
        
        accountRepository.save(account);
        auditService.auditUsernameChange(command.accountId(), command.accountId(), 
                                       oldUsername, command.newUsername().value(), null, null);
        
        return Optional.of(account.getAccountId());
    }
    
    @Override
    @Transactional
    public void handle(SeedRolesCommand command) {
        // This is handled automatically by the enum, no seeding needed
    }
}
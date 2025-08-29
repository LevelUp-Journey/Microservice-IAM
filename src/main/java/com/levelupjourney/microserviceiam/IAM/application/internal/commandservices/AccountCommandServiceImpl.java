package com.levelupjourney.microserviceiam.IAM.application.internal.commandservices;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.hashing.HashingService;
import com.levelupjourney.microserviceiam.IAM.application.internal.services.IamAuditService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.*;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountCommandService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.AccountRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.ExternalIdentityRepository;
import com.levelupjourney.microserviceiam.Profile.domain.model.commands.CreateUserProfileFromAccountCommand;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.PublicUsername;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.DisplayName;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AvatarUrl;
import com.levelupjourney.microserviceiam.Profile.domain.services.UserProfileCommandService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AccountCommandServiceImpl implements AccountCommandService {
    
    private final AccountRepository accountRepository;
    private final ExternalIdentityRepository externalIdentityRepository;
    private final HashingService hashingService;
    private final IamAuditService auditService;
    private final UserProfileCommandService userProfileCommandService;
    
    public AccountCommandServiceImpl(AccountRepository accountRepository,
                                   ExternalIdentityRepository externalIdentityRepository,
                                   HashingService hashingService,
                                   IamAuditService auditService,
                                   UserProfileCommandService userProfileCommandService) {
        this.accountRepository = accountRepository;
        this.externalIdentityRepository = externalIdentityRepository;
        this.hashingService = hashingService;
        this.auditService = auditService;
        this.userProfileCommandService = userProfileCommandService;
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
        
        // Save first to generate the ID
        Account savedAccount = accountRepository.save(account);
        
        // Now initialize local account entities with the generated account ID
        savedAccount.initializeLocalAccount(hashedPassword, command.roles(), command.username());
        
        // Save again with the initialized entities
        savedAccount = accountRepository.save(savedAccount);
        
        // Create UserProfile for local account
        createUserProfileForAccount(savedAccount, null, null);
        
        auditService.auditSignUp(savedAccount.getAccountId(), null);
        
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
        
        auditService.auditSignIn(account.getAccountId(), null);
        return Optional.of(account);
    }
    
    @Override
    @Transactional
    public Optional<Account> handle(OAuth2SignInCommand command) {
        var existingIdentity = externalIdentityRepository.findByProviderAndProviderUserId(
            command.provider(), command.providerUserId()
        );
        
        if (existingIdentity.isPresent()) {
            Optional<Account> account = accountRepository.findById(existingIdentity.get().getAccount().getAccountId().value());
            if (account.isPresent()) {
                auditService.auditSignIn(account.get().getAccountId(), null);
                return account;
            }
        }
        
        if (accountRepository.existsByEmail(command.email())) {
            Optional<Account> existingAccount = accountRepository.findByEmail(command.email());
            if (existingAccount.isPresent()) {
                String name = extractNameFromAttributes(command.attributes());
                String avatarUrl = extractAvatarFromAttributes(command.attributes());
                existingAccount.get().linkExternalIdentity(command.provider(), command.providerUserId(), name, avatarUrl);
                Account savedAccount = accountRepository.save(existingAccount.get());
                auditService.auditOAuth2Link(savedAccount.getAccountId(), command.provider().provider(), null);
                return Optional.of(savedAccount);
            }
        }
        
        Username generatedUsername = Username.generateRandomUsername();
        while (accountRepository.existsByUsername(generatedUsername)) {
            generatedUsername = Username.generateRandomUsername();
        }
        
        Account newAccount = new Account(command.email(), generatedUsername, command.provider(), 
                                       command.providerUserId(), command.name(), command.roles());
        
        // Save first to generate the ID
        Account savedAccount = accountRepository.save(newAccount);
        
        // Now initialize OAuth2-specific entities with the generated account ID
        String name = extractNameFromAttributes(command.attributes());
        String avatarUrl = extractAvatarFromAttributes(command.attributes());
        savedAccount.initializeOAuth2Account(command.provider(), command.providerUserId(), 
                                           name, avatarUrl, command.roles(), generatedUsername);
        
        // Save again with the initialized entities
        savedAccount = accountRepository.save(savedAccount);
        
        // Create UserProfile for OAuth2 account
        createUserProfileForAccount(savedAccount, name, avatarUrl);
        
        auditService.auditSignUp(savedAccount.getAccountId(), null);
        auditService.auditOAuth2Link(savedAccount.getAccountId(), command.provider().provider(), null);
        
        return Optional.of(savedAccount);
    }
    
    private String extractNameFromAttributes(Map<String, Object> attributes) {
        if (attributes != null) {
            Object name = attributes.get("name");
            if (name != null) return name.toString();
            
            Object login = attributes.get("login");
            if (login != null) return login.toString();
        }
        return null;
    }
    
    private String extractAvatarFromAttributes(Map<String, Object> attributes) {
        if (attributes != null) {
            Object avatarUrl = attributes.get("avatar_url");
            if (avatarUrl != null) return avatarUrl.toString();
            
            Object picture = attributes.get("picture");
            if (picture != null) return picture.toString();
        }
        return null;
    }
    
    private void createUserProfileForAccount(Account account, String name, String avatarUrl) {
        // Extract role names from account
        Set<String> roleNames = account.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());
        
        var command = new CreateUserProfileFromAccountCommand(
                new com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId(account.getAccountId().value()),
                new PublicUsername(account.getUsername().value()),
                name != null ? new DisplayName(name) : null,
                avatarUrl != null ? new AvatarUrl(avatarUrl) : null,
                roleNames
        );
        
        userProfileCommandService.handle(command);
    }
    
    @Override
    @Transactional
    public Optional<com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId> handle(ChangePasswordCommand command) {
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
        auditService.auditPasswordChange(command.accountId(), command.accountId(), null);
        
        return Optional.of(account.getAccountId());
    }
    
    @Override
    @Transactional
    public Optional<com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId> handle(UpdateUsernameCommand command) {
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
                                       oldUsername, command.newUsername().value(), null);
        
        return Optional.of(account.getAccountId());
    }
    
    @Override
    @Transactional
    public void handle(SeedRolesCommand command) {
        // This is handled automatically by the enum, no seeding needed
    }
}
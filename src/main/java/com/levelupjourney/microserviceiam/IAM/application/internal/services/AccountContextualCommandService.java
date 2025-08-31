package com.levelupjourney.microserviceiam.IAM.application.internal.services;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.hashing.HashingService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.*;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.*;
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

/**
 * Contextual command service that handles IAM commands with user agent tracking
 */
@Service
public class AccountContextualCommandService {
    
    private final AccountRepository accountRepository;
    private final ExternalIdentityRepository externalIdentityRepository;
    private final HashingService hashingService;
    private final IamAuditService auditService;
    private final UserProfileCommandService userProfileCommandService;
    private final RoleManagementService roleManagementService;
    
    public AccountContextualCommandService(AccountRepository accountRepository,
                                         ExternalIdentityRepository externalIdentityRepository,
                                         HashingService hashingService,
                                         IamAuditService auditService,
                                         UserProfileCommandService userProfileCommandService,
                                         RoleManagementService roleManagementService) {
        this.accountRepository = accountRepository;
        this.externalIdentityRepository = externalIdentityRepository;
        this.hashingService = hashingService;
        this.auditService = auditService;
        this.userProfileCommandService = userProfileCommandService;
        this.roleManagementService = roleManagementService;
    }
    
    @Transactional
    public Optional<Account> handleSignUp(SignUpCommand command, String userAgent) {
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
        var roleEntities = roleManagementService.getRoleEntities(command.roles());
        savedAccount.initializeLocalAccount(hashedPassword, roleEntities, command.username());
        
        // Save again with the initialized entities
        savedAccount = accountRepository.save(savedAccount);
        
        // Create UserProfile for local account
        createUserProfileForAccount(savedAccount, null, null);
        
        auditService.auditSignUp(savedAccount.getAccountId(), userAgent);
        
        return Optional.of(savedAccount);
    }
    
    @Transactional(readOnly = true)
    public Optional<Account> handleSignIn(SignInCommand command, String userAgent) {
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
        
        auditService.auditSignIn(account.getAccountId(), userAgent);
        return Optional.of(account);
    }
    
    @Transactional
    public Optional<com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId> handleChangePassword(ChangePasswordCommand command, String userAgent) {
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
        auditService.auditPasswordChange(command.accountId(), command.accountId(), userAgent);
        
        return Optional.of(account.getAccountId());
    }
    
    @Transactional
    public Optional<com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId> handleUpdateUsername(UpdateUsernameCommand command, String userAgent) {
        Optional<Account> accountOpt = accountRepository.findById(command.accountId().value());
        
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }
        
        // Check if username exists for other accounts (excluding current account)
        if (accountRepository.existsByUsernameAndAccountIdNot(command.newUsername(), command.accountId().value())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        Account account = accountOpt.get();
        String oldUsername = account.getUsername().value();
        account.updateUsername(command.newUsername());
        
        accountRepository.save(account);
        
        auditService.auditUsernameChange(command.accountId(), command.accountId(), 
                                       oldUsername, command.newUsername().value(), userAgent);
        
        return Optional.of(account.getAccountId());
    }
    
    @Transactional
    public Optional<Account> handleOAuth2SignIn(OAuth2SignInCommand command, String userAgent) {
        var existingIdentity = externalIdentityRepository.findByProviderAndProviderUserId(
            command.provider(), command.providerUserId()
        );
        
        if (existingIdentity.isPresent()) {
            Optional<Account> account = accountRepository.findById(existingIdentity.get().getAccount().getAccountId().value());
            if (account.isPresent()) {
                auditService.auditSignIn(account.get().getAccountId(), userAgent);
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
                auditService.auditOAuth2Link(savedAccount.getAccountId(), command.provider().provider(), userAgent);
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
        var roleEntities = roleManagementService.getRoleEntities(command.roles());
        savedAccount.initializeOAuth2Account(command.provider(), command.providerUserId(), 
                                           name, avatarUrl, roleEntities, generatedUsername);
        
        // Save again with the initialized entities
        savedAccount = accountRepository.save(savedAccount);
        
        // Create UserProfile for OAuth2 account
        createUserProfileForAccount(savedAccount, name, avatarUrl);
        
        auditService.auditSignUp(savedAccount.getAccountId(), userAgent);
        auditService.auditOAuth2Link(savedAccount.getAccountId(), command.provider().provider(), userAgent);
        
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
}
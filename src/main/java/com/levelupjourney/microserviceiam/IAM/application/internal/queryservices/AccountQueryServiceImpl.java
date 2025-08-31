package com.levelupjourney.microserviceiam.IAM.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.ExternalIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.queries.*;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountQueryService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.AccountRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.ExternalIdentityRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AccountQueryServiceImpl implements AccountQueryService {
    
    private final AccountRepository accountRepository;
    private final ExternalIdentityRepository externalIdentityRepository;
    
    public AccountQueryServiceImpl(AccountRepository accountRepository,
                                 ExternalIdentityRepository externalIdentityRepository) {
        this.accountRepository = accountRepository;
        this.externalIdentityRepository = externalIdentityRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> handle(GetAccountByIdQuery query) {
        return accountRepository.findById(query.accountId().value());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> handle(GetAccountByEmailQuery query) {
        return accountRepository.findByEmail(query.email());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> handle(GetAccountByUsernameQuery query) {
        return accountRepository.findByUsername(query.username());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> handle(GetAccountByEmailOrUsernameQuery query) {
        return accountRepository.findByEmailOrUsername(query.emailOrUsername());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExternalIdentity> handle(GetExternalIdentityByProviderAndUserIdQuery query) {
        return externalIdentityRepository.findByProviderAndProviderUserId(
            query.provider(), query.providerUserId()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Role> handle(GetAllRolesQuery query) {
        return Arrays.asList(Role.values());
    }
}
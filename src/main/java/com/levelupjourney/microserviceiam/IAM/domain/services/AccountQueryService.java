package com.levelupjourney.microserviceiam.IAM.domain.services;

import java.util.List;
import java.util.Optional;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.ExternalIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.queries.*;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;

public interface AccountQueryService {
    Optional<Account> handle(GetAccountByIdQuery query);
    Optional<Account> handle(GetAccountByEmailQuery query);
    Optional<Account> handle(GetAccountByUsernameQuery query);
    Optional<Account> handle(GetAccountByEmailOrUsernameQuery query);
    Optional<ExternalIdentity> handle(GetExternalIdentityByProviderAndUserIdQuery query);
    List<Role> handle(GetAllRolesQuery query);
}
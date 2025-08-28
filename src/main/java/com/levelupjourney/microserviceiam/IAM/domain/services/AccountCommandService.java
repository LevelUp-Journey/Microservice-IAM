package com.levelupjourney.microserviceiam.IAM.domain.services;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.*;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;

import java.util.Optional;

public interface AccountCommandService {
    Optional<Account> handle(SignUpCommand command);
    Optional<Account> handle(SignInCommand command);
    Optional<Account> handle(OAuth2SignInCommand command);
    Optional<AccountId> handle(ChangePasswordCommand command);
    Optional<AccountId> handle(UpdateUsernameCommand command);
    void handle(SeedRolesCommand command);
}
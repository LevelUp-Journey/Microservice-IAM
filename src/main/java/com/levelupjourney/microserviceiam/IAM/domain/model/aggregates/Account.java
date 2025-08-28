package com.levelupjourney.microserviceiam.IAM.domain.model.aggregates;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Credential;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.ExternalIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.RoleAssignment;
import com.levelupjourney.microserviceiam.IAM.domain.model.events.AccountRegisteredEvent;
import com.levelupjourney.microserviceiam.IAM.domain.model.events.UsernameChangedEvent;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Entity
@Table(name = "accounts")
public class Account extends AuditableAbstractAggregateRoot<Account> {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "email", column = @Column(name = "email", unique = true, nullable = false))
    })
    private EmailAddress email;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "username", unique = true, nullable = false))
    })
    private Username username;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Credential credential;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Set<ExternalIdentity> externalIdentities = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Set<RoleAssignment> roleAssignments = new HashSet<>();

    public Account() {}

    public Account(EmailAddress email, Username username, PasswordHash passwordHash, Set<Role> roles) {
        this.email = email;
        this.username = username;
        this.status = AccountStatus.ACTIVE;
        this.credential = new Credential(getAccountId(), passwordHash);
        
        roles.forEach(role -> 
            this.roleAssignments.add(new RoleAssignment(getAccountId(), role))
        );

        addDomainEvent(new AccountRegisteredEvent(getAccountId(), "local", username));
    }

    public Account(EmailAddress email, Username username, AuthProvider provider, String providerUserId, String name, Map<String, Object> attributes, Set<Role> roles) {
        this.email = email;
        this.username = username;
        this.status = AccountStatus.ACTIVE;
        
        this.externalIdentities.add(new ExternalIdentity(getAccountId(), provider, providerUserId, attributes));
        
        roles.forEach(role -> 
            this.roleAssignments.add(new RoleAssignment(getAccountId(), role))
        );

        addDomainEvent(new AccountRegisteredEvent(getAccountId(), provider.provider(), username));
    }

    public AccountId getAccountId() {
        return new AccountId(getId());
    }

    public void changePassword(PasswordHash newPasswordHash) {
        if (credential == null) {
            throw new IllegalStateException("Cannot change password for OAuth-only account");
        }
        credential.updatePassword(newPasswordHash);
    }

    public void updateUsername(Username newUsername) {
        Username oldUsername = this.username;
        this.username = newUsername;
        addDomainEvent(new UsernameChangedEvent(getAccountId(), oldUsername, newUsername));
    }

    public void deactivate() {
        this.status = AccountStatus.DEACTIVATED;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public void addRole(Role role) {
        this.roleAssignments.add(new RoleAssignment(getAccountId(), role));
    }

    public void removeRole(Role role) {
        this.roleAssignments.removeIf(ra -> ra.getRole().equals(role));
    }

    public Set<Role> getRoles() {
        return roleAssignments.stream()
            .map(RoleAssignment::getRole)
            .collect(java.util.stream.Collectors.toSet());
    }

    public boolean hasRole(Role role) {
        return getRoles().contains(role);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean hasLocalCredentials() {
        return credential != null;
    }

    public List<ExternalIdentity> getExternalIdentitiesByProvider(AuthProvider provider) {
        return externalIdentities.stream()
            .filter(ei -> ei.getProvider().equals(provider))
            .toList();
    }

    public void linkExternalIdentity(AuthProvider provider, String providerUserId, Map<String, Object> attributes) {
        this.externalIdentities.add(new ExternalIdentity(getAccountId(), provider, providerUserId, attributes));
    }

    public enum AccountStatus {
        ACTIVE, DEACTIVATED, SUSPENDED
    }
}
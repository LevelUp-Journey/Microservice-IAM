package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import com.levelupjourney.microserviceiam.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "external_identities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
})
public class ExternalIdentity extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "provider", column = @Column(name = "provider", nullable = false))
    })
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt;

    @Column(name = "name")
    private String name;
    
    @Column(name = "avatar_url")
    private String avatarUrl;

    public ExternalIdentity() {}

    public ExternalIdentity(Account account, AuthProvider provider, String providerUserId) {
        this.account = account;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.linkedAt = LocalDateTime.now();
    }

    public ExternalIdentity(Account account, AuthProvider provider, String providerUserId, String name, String avatarUrl) {
        this(account, provider, providerUserId);
        this.name = name;
        this.avatarUrl = avatarUrl;
    }
}
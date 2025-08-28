package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import com.levelupjourney.microserviceiam.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Entity
@Table(name = "external_identities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
})
public class ExternalIdentity extends AuditableModel {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "account_id", nullable = false))
    })
    private AccountId accountId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "provider", column = @Column(name = "provider", nullable = false))
    })
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt;

    @ElementCollection
    @CollectionTable(name = "external_identity_attributes", joinColumns = @JoinColumn(name = "external_identity_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    public ExternalIdentity() {}

    public ExternalIdentity(AccountId accountId, AuthProvider provider, String providerUserId) {
        this.accountId = accountId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.linkedAt = LocalDateTime.now();
    }

    public ExternalIdentity(AccountId accountId, AuthProvider provider, String providerUserId, Map<String, Object> attributes) {
        this(accountId, provider, providerUserId);
        setAttributes(attributes);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes.clear();
        if (attributes != null) {
            attributes.forEach((key, value) -> {
                if (value != null) {
                    this.attributes.put(key, value.toString());
                }
            });
        }
    }

    public void updateAttributes(Map<String, Object> newAttributes) {
        if (newAttributes != null) {
            newAttributes.forEach((key, value) -> {
                if (value != null) {
                    this.attributes.put(key, value.toString());
                } else {
                    this.attributes.remove(key);
                }
            });
        }
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }
}
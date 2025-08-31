package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Entity
@Table(name = "iam_audits")
public class IamAudit extends AuditableModel {

    @Column(name = "audit_id", nullable = false, unique = true)
    private UUID auditId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "actor_id"))
    })
    private AccountId actorId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "account_id"))
    })
    private AccountId accountId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "user_agent")
    private String userAgent;

    @ElementCollection
    @CollectionTable(name = "iam_audit_metadata", joinColumns = @JoinColumn(name = "iam_audit_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "audit_created_at")
    private LocalDateTime auditCreatedAt;

    public IamAudit() {}

    public IamAudit(AccountId actorId, AccountId accountId, String action, String userAgent, Map<String, Object> metadata) {
        this.auditId = UUID.randomUUID();
        this.actorId = actorId;
        this.accountId = accountId;
        this.action = action;
        this.userAgent = userAgent;
        this.auditCreatedAt = LocalDateTime.now();
        setMetadata(metadata);
    }

    public IamAudit(AccountId accountId, String action, String userAgent) {
        this(accountId, accountId, action, userAgent, Map.of());
    }

    private void setMetadata(Map<String, Object> metadata) {
        this.metadata.clear();
        if (metadata != null) {
            metadata.forEach((key, value) -> {
                if (value != null) {
                    this.metadata.put(key, value.toString());
                }
            });
        }
    }

    public static class Actions {
        public static final String OAUTH_LINKED = "OAUTH_LINKED";
        public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
        public static final String SIGNED_IN = "SIGNED_IN";
        public static final String SIGNED_UP = "SIGNED_UP";
        public static final String ROLE_ASSIGNED = "ROLE_ASSIGNED";
        public static final String USERNAME_CHANGED = "USERNAME_CHANGED";
        public static final String ACCOUNT_DEACTIVATED = "ACCOUNT_DEACTIVATED";
        public static final String CREDENTIALS_UPDATED = "CREDENTIALS_UPDATED";
        public static final String EXTERNAL_IDENTITY_UPDATED = "EXTERNAL_IDENTITY_UPDATED";
    }
}
package com.levelupjourney.microserviceiam.Profile.domain.model.entities;

import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "profile_audits")
public class ProfileAudit extends AuditableModel {

    @Column(name = "audit_id", nullable = false, unique = true)
    private UUID auditId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "actor_id"))
    })
    private AccountId actorId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "account_id", nullable = false))
    })
    private AccountId accountId;

    @Column(name = "field", nullable = false)
    private String field;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    public ProfileAudit() {}

    public ProfileAudit(AccountId actorId, AccountId accountId, String field, String oldValue, String newValue) {
        this.auditId = UUID.randomUUID();
        this.actorId = actorId;
        this.accountId = accountId;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = LocalDateTime.now();
    }

    public static class Fields {
        public static final String USERNAME = "username";
        public static final String NAME = "name";
        public static final String AVATAR_URL = "avatarUrl";
        public static final String ROLES = "roles";
    }
}
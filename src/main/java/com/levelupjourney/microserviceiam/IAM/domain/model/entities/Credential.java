package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AccountId;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.PasswordHash;
import com.levelupjourney.microserviceiam.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "credentials")
public class Credential extends AuditableModel {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "account_id", nullable = false))
    })
    private AccountId accountId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "hash", column = @Column(name = "password_hash", nullable = false))
    })
    private PasswordHash passwordHash;

    @Column(name = "password_updated_at")
    private LocalDateTime passwordUpdatedAt;

    public Credential() {}

    public Credential(AccountId accountId, PasswordHash passwordHash) {
        this.accountId = accountId;
        this.passwordHash = passwordHash;
        this.passwordUpdatedAt = LocalDateTime.now();
    }

    public void updatePassword(PasswordHash newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordUpdatedAt = LocalDateTime.now();
    }

    public boolean isPasswordExpired(int maxAgeDays) {
        return passwordUpdatedAt.isBefore(LocalDateTime.now().minusDays(maxAgeDays));
    }
}
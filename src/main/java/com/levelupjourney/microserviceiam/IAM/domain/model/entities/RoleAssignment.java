package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "role_assignments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "role_id"})
})
public class RoleAssignment extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "granted_by")
    private String grantedBy;

    public RoleAssignment() {}

    public RoleAssignment(Account account, Role role) {
        this.account = account;
        this.role = role;
        this.grantedAt = LocalDateTime.now();
        this.grantedBy = "SYSTEM";
    }

    public RoleAssignment(Account account, Role role, String grantedBy) {
        this.account = account;
        this.role = role;
        this.grantedAt = LocalDateTime.now();
        this.grantedBy = grantedBy;
    }
    
    /**
     * Get role name from the associated role entity
     */
    public String getRoleName() {
        return role != null ? role.getName() : null;
    }
    
    /**
     * Get role ID from the associated role entity
     */
    public UUID getRoleId() {
        return role != null ? role.getRoleId() : null;
    }
}
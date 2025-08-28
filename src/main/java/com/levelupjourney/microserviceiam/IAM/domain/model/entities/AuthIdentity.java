package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "auth_identities",
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
@EntityListeners(AuditingEntityListener.class)
public class AuthIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private java.util.UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id")
    private String providerUserId;

    // For local auth
    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    // For OAuth providers
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "scope")
    private String scope;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for local auth
    public AuthIdentity(User user, AuthProvider provider, String passwordHash) {
        this.user = user;
        this.provider = provider;
        this.passwordHash = passwordHash;
    }

    // Constructor for OAuth providers
    public AuthIdentity(User user, AuthProvider provider, String providerUserId, String scope) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.scope = scope;
    }

    // Constructor for OAuth providers with tokens
    public AuthIdentity(User user, AuthProvider provider, String providerUserId, String accessToken, 
                           String refreshToken, LocalDateTime expiresAt, String scope) {
        this(user, provider, providerUserId, scope);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
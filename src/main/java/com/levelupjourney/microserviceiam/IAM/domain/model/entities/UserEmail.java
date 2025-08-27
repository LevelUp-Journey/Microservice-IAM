package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_emails", 
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@EntityListeners(AuditingEntityListener.class)
public class UserEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "verified_by_provider")
    private AuthProvider verifiedByProvider;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_sent_at")
    private LocalDateTime verificationSentAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserEmail() {}

    public UserEmail(User user, String email, Boolean isPrimary) {
        this.user = user;
        this.email = email;
        this.isPrimary = isPrimary;
        this.isVerified = false;
    }

    public UserEmail(User user, String email, Boolean isPrimary, Boolean isVerified, AuthProvider verifiedByProvider) {
        this.user = user;
        this.email = email;
        this.isPrimary = isPrimary;
        this.isVerified = isVerified;
        this.verifiedByProvider = verifiedByProvider;
        if (isVerified) {
            this.verifiedAt = LocalDateTime.now();
        }
    }

    public void markAsVerified(AuthProvider provider) {
        this.isVerified = true;
        this.verifiedByProvider = provider;
        this.verifiedAt = LocalDateTime.now();
        this.verificationToken = null;
    }

    public void generateVerificationToken() {
        this.verificationToken = java.util.UUID.randomUUID().toString();
        this.verificationSentAt = LocalDateTime.now();
    }
}
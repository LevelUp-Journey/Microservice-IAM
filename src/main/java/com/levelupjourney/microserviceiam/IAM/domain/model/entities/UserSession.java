package com.levelupjourney.microserviceiam.IAM.domain.model.entities;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_sessions", 
       indexes = {
           @Index(name = "idx_user_sessions_user_id", columnList = "user_id"),
           @Index(name = "idx_user_sessions_auth_provider", columnList = "auth_provider"),
           @Index(name = "idx_user_sessions_created_at", columnList = "created_at")
       })
@EntityListeners(AuditingEntityListener.class)
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column(name = "session_type", nullable = false)
    private String sessionType; // "LOGIN", "SIGNUP"

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public UserSession() {}

    public UserSession(User user, AuthProvider authProvider, String sessionType, Boolean success) {
        this.user = user;
        this.authProvider = authProvider;
        this.sessionType = sessionType;
        this.success = success;
    }

    public UserSession(User user, AuthProvider authProvider, String sessionType, Boolean success, String failureReason) {
        this.user = user;
        this.authProvider = authProvider;
        this.sessionType = sessionType;
        this.success = success;
        this.failureReason = failureReason;
    }

    public void setSessionInfo(String ipAddress, String userAgent) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}
package com.levelupjourney.microserviceiam.IAM.domain.model.aggregates;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.AuthIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserEmail;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserSession;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User aggregate root
 * This class represents the aggregate root for the User entity.
 */
@Getter
@Setter
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private java.util.UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserEmail> emails = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserSession> sessions = new HashSet<>();

    @Column(name = "name")
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;


    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable( name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AuthIdentity> authIdentities = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User() {
        this.roles = new HashSet<>();
        this.authIdentities = new HashSet<>();
        this.emails = new HashSet<>();
        this.sessions = new HashSet<>();
    }

    public User(String username) {
        this.username = username;
        this.roles = new HashSet<>();
        this.authIdentities = new HashSet<>();
        this.emails = new HashSet<>();
        this.sessions = new HashSet<>();
        // Note: Default role should be added by the service layer to avoid duplicates
    }

    public User(String username, List<Role> roles) {
        this.username = username;
        this.roles = new HashSet<>();
        this.authIdentities = new HashSet<>();
        this.emails = new HashSet<>();
        this.sessions = new HashSet<>();
        addRoles(roles);
    }

    public User(String email, String name, String avatarUrl, boolean emailVerified) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.username = name; // Use full name as username for OAuth users
        this.roles = new HashSet<>();
        this.authIdentities = new HashSet<>();
        this.emails = new HashSet<>();
        this.sessions = new HashSet<>();
        // Add primary email (provider will be set later by the calling service)
        this.addEmail(email, true, emailVerified, null);
        // Note: Default role should be added by the service layer to avoid duplicates
    }

    public User(String email, String name, String avatarUrl, boolean emailVerified, com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider verifiedByProvider) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.username = name; // Use full name as username for OAuth users
        this.roles = new HashSet<>();
        this.authIdentities = new HashSet<>();
        this.emails = new HashSet<>();
        this.sessions = new HashSet<>();
        // Add primary email with specific provider
        this.addEmail(email, true, emailVerified, emailVerified ? verifiedByProvider : null);
        // Note: Default role should be added by the service layer to avoid duplicates
    }

    /**
     * Add a role to the user
     * @param role the role to add
     * @return the user with the added role
     */
    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    /**
     * Add a list of roles to the user
     * @param roles the list of roles to add
     * @return the user with the added roles
     */
    public User addRoles(List<Role> roles) {
        if (roles != null && !roles.isEmpty()) {
            this.roles.addAll(roles);
        }
        return this;
    }

    /**
     * Add an auth identity to the user
     * @param authIdentity the auth identity to add
     * @return the user with the added auth identity
     */
    public User addAuthIdentity(AuthIdentity authIdentity) {
        this.authIdentities.add(authIdentity);
        return this;
    }

    /**
     * Get auth identity by provider
     * @param provider the provider name
     * @return the auth identity if found, null otherwise
     */
    public AuthIdentity getAuthIdentityByProvider(String provider) {
        return authIdentities.stream()
                .filter(identity -> identity.getProvider().name().equals(provider))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add an email to the user
     * @param email the email address
     * @param isPrimary whether this is the primary email
     * @param isVerified whether the email is verified
     * @param verifiedByProvider the provider that verified the email
     * @return the user with the added email
     */
    public User addEmail(String email, Boolean isPrimary, Boolean isVerified, com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider verifiedByProvider) {
        UserEmail userEmail = new UserEmail(this, email, isPrimary, isVerified, verifiedByProvider);
        this.emails.add(userEmail);
        return this;
    }

    /**
     * Get primary email address
     * @return the primary email address, null if none found
     */
    public String getPrimaryEmail() {
        return emails.stream()
                .filter(UserEmail::getIsPrimary)
                .map(UserEmail::getEmail)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if email is verified
     * @return true if primary email is verified, false otherwise
     */
    public Boolean getEmailVerified() {
        return emails.stream()
                .filter(UserEmail::getIsPrimary)
                .map(UserEmail::getIsVerified)
                .findFirst()
                .orElse(false);
    }

    /**
     * Add a session to track user activity
     * @param session the session to add
     * @return the user with the added session
     */
    public User addSession(UserSession session) {
        this.sessions.add(session);
        return this;
    }

}

package com.levelupjourney.microserviceiam.IAM.domain.model.aggregates;

import com.levelupjourney.microserviceiam.IAM.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.AuthIdentity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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

    @Email
    @Column(unique = true)
    private String email;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

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
    }

    public User(String username) {
        this.username = username;
        this.roles = new HashSet<>();
        this.authIdentities = new HashSet<>();
        // Assign default STUDENT role
        this.addRole(Role.getDefaultRole());
    }

    public User(String username, List<Role> roles) {
        this.username = username;
        this.roles = new HashSet<>();
        this.authIdentities = new HashSet<>();
        addRoles(roles);
    }

    public User(String email, String name, String avatarUrl, boolean emailVerified) {
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.emailVerified = emailVerified;
        this.username = name; // Use full name as username for OAuth users
        this.roles = new HashSet<>();
        this.authIdentities = new HashSet<>();
        // Assign default STUDENT role
        this.addRole(Role.getDefaultRole());
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
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
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

}

package com.levelupjourney.microserviceiam.iam.domain.model.aggregates;

import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.EmailAddress;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.Password;
import com.levelupjourney.microserviceiam.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User aggregate root
 * This class represents the aggregate root for the User entity.
 *
 * @see AuditableAbstractAggregateRoot
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableAbstractAggregateRoot<User> {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "email", column = @Column(name = "email_address", unique = true))})
    private EmailAddress emailAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "password", column = @Column(name = "password"))})
    private Password userPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(	name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User() {
        this.roles = new HashSet<>();
    }
    
    public User(String email, String password) {
        this.emailAddress = new EmailAddress(email);
        this.userPassword = new Password(password);
        this.roles = new HashSet<>();
    }

    public User(String email, String password, List<Role> roles) {
        this(email, password);
        addRoles(roles);
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
     * Get email (for compatibility)
     * @return the normalized email
     */
    public String getEmail() {
        return this.emailAddress != null ? this.emailAddress.normalized() : null;
    }

    /**
     * Set email (for compatibility)
     * @param email the email
     */
    public void setEmail(String email) {
        this.emailAddress = new EmailAddress(email);
    }

    /**
     * Get password (for authentication)
     * @return the password
     */
    public String getPassword() {
        return this.userPassword != null ? this.userPassword.password() : null;
    }

    /**
     * Set password
     * @param password the new password
     */
    public void setPassword(String password) {
        this.userPassword = new Password(password);
    }

    /**
     * Get email address value object
     * @return EmailAddress value object
     */
    public EmailAddress getEmailAddress() {
        return this.emailAddress;
    }

    /**
     * Get password strength score
     * @return password strength score (1-5)
     */
    public int getPasswordStrength() {
        return this.userPassword != null ? this.userPassword.getStrengthScore() : 0;
    }

}

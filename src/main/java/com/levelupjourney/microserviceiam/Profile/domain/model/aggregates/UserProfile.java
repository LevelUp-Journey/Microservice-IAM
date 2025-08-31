package com.levelupjourney.microserviceiam.Profile.domain.model.aggregates;

import com.levelupjourney.microserviceiam.Profile.domain.model.entities.ProfileAudit;
import com.levelupjourney.microserviceiam.Profile.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@Table(name = "user_profiles")
public class UserProfile extends AuditableAbstractAggregateRoot<UserProfile> {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "account_id", unique = true, nullable = false))
    })
    private AccountId accountId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "username", unique = true, nullable = false))
    })
    private PublicUsername username;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "name"))
    })
    private DisplayName name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "url", column = @Column(name = "avatar_url"))
    })
    private AvatarUrl avatarUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_profile_roles", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private List<ProfileAudit> profileAudits = new ArrayList<>();

    public UserProfile() {}

    public UserProfile(AccountId accountId, PublicUsername username, DisplayName name, AvatarUrl avatarUrl, Set<String> roles) {
        this.accountId = accountId;
        this.username = username;
        this.name = name != null ? name : DisplayName.empty();
        this.avatarUrl = avatarUrl != null ? avatarUrl : AvatarUrl.empty();
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public ProfileId getProfileId() {
        return new ProfileId(getId());
    }

    public void updateProfile(PublicUsername newUsername, DisplayName newName, AvatarUrl newAvatarUrl, AccountId actorId) {
        if (newUsername != null && !this.username.equals(newUsername)) {
            auditChange(actorId, ProfileAudit.Fields.USERNAME, this.username.value(), newUsername.value());
            this.username = newUsername;
        }

        if (newName != null && !this.name.equals(newName)) {
            auditChange(actorId, ProfileAudit.Fields.NAME, this.name.value(), newName.value());
            this.name = newName;
        }

        if (newAvatarUrl != null && !this.avatarUrl.equals(newAvatarUrl)) {
            auditChange(actorId, ProfileAudit.Fields.AVATAR_URL, this.avatarUrl.url(), newAvatarUrl.url());
            this.avatarUrl = newAvatarUrl;
        }
    }

    public void updateRoles(Set<String> newRoles, AccountId actorId) {
        if (newRoles != null && !this.roles.equals(newRoles)) {
            auditChange(actorId, ProfileAudit.Fields.ROLES, 
                       String.join(",", this.roles), 
                       String.join(",", newRoles));
            this.roles.clear();
            this.roles.addAll(newRoles);
        }
    }

    private void auditChange(AccountId actorId, String field, String oldValue, String newValue) {
        ProfileAudit audit = new ProfileAudit(actorId, getId(), field, oldValue, newValue);
        this.profileAudits.add(audit);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public void addRole(String role) {
        roles.add(role);
    }

    public void removeRole(String role) {
        roles.remove(role);
    }
}
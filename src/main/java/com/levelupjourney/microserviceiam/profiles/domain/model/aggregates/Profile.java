package com.levelupjourney.microserviceiam.profiles.domain.model.aggregates;

import com.levelupjourney.microserviceiam.profiles.domain.model.commands.CreateProfileCommand;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.PersonName;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.ProfileUrl;
import com.levelupjourney.microserviceiam.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;

@Entity
public class Profile extends AuditableAbstractAggregateRoot<Profile> {
    @Embedded
    private PersonName name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "username", column = @Column(name = "username"))})
    private Username username;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "url", column = @Column(name = "profile_url"))})
    private ProfileUrl profileUrl;

    public Profile(String firstName, String lastName, String username, String profileUrl) {
        this.name = new PersonName(firstName, lastName);
        this.username = new Username(username);
        this.profileUrl = new ProfileUrl(profileUrl);
    }

    public Profile() {
        // Default constructor for JPA
    }

    public Profile(CreateProfileCommand command, String username) {
        this(
                command.firstName(),
                command.lastName(),
                username,
                command.profileUrl()
        );
    }

    public String getFullName() {
        return name.getFullName();
    }

    public String getUsername() {
        return username.username();
    }

    public String getFirstName() {
        return name.firstName();
    }

    public String getLastName() {
        return name.lastName();
    }

    public String getProfileUrl() {
        return profileUrl != null ? profileUrl.value() : null;
    }

    public void updateName(String firstName, String lastName) {
        this.name = new PersonName(firstName, lastName);
    }

    public void updateUsername(String username) {
        // Username constructor automatically detects if it's:
        // - Generated format (USER + 9 digits): applies strict validation
        // - Edited format (other): applies flexible validation (3-50 chars, alphanumeric + _.- only)
        this.username = new Username(username);
    }

    public void updateProfileUrl(String profileUrl) {
        this.profileUrl = new ProfileUrl(profileUrl);
    }
}
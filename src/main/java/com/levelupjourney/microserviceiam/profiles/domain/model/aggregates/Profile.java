package com.levelupjourney.microserviceiam.profiles.domain.model.aggregates;

import com.levelupjourney.microserviceiam.profiles.domain.model.commands.CreateProfileCommand;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.PersonName;
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

    @Column(name = "profile_url")
    private String profileUrl;

    public Profile(String firstName, String lastName, String username, String profileUrl) {
        this.name = new PersonName(firstName, lastName);
        this.username = new Username(username);
        this.profileUrl = profileUrl;
    }

    public Profile() {
        // Default constructor for JPA
    }

    public Profile(CreateProfileCommand command) {
        this(
                command.firstName(),
                command.lastName(),
                command.username(),
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
        return profileUrl;
    }

    public void updateName(String firstName, String lastName) {
        this.name = new PersonName(firstName, lastName);
    }

    public void updateUsername(String username) {
        this.username = new Username(username);
    }

    public void updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
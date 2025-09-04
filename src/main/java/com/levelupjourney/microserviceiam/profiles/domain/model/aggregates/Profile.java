package com.levelupjourney.microserviceiam.profiles.domain.model.aggregates;

import com.levelupjourney.microserviceiam.profiles.domain.model.commands.CreateProfileCommand;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.PersonName;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.StreetAddress;
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
            @AttributeOverride(name = "street", column = @Column(name = "street_address_street")),
            @AttributeOverride(name = "number", column = @Column(name = "street_address_number")),
            @AttributeOverride(name = "city", column = @Column(name = "street_address_city")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "street_address_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "street_address_country"))})
    private StreetAddress streetAddress;

    public Profile(String firstName, String lastName, String username, String street, String number, String city, String state, String postalCode, String country) {
        this.name = new PersonName(firstName, lastName);
        this.username = new Username(username);
        this.streetAddress = new StreetAddress(street, number, city, state, postalCode, country);
    }

    public Profile() {
        // Default constructor for JPA
    }

    public Profile(CreateProfileCommand command) {
        this(
                command.firstName(),
                command.lastName(),
                command.username(),
                command.street(),
                command.number(),
                command.city(),
                command.state(),
                command.postalCode(),
                command.country()
        );
    }

    public String getFullName() {
        return name.getFullName();
    }

    public String getemail_addressAddress() {
        return username.username();
    }

    public String getUsername() {
        return username.username();
    }

    public String getStreetAddress() {
        return streetAddress.getStreetAddress();
    }

    public void updateName(String firstName, String lastName) {
      this.name = new PersonName(firstName, lastName);
    }

    public void updateemail_addressAddress(String email_address) {
        this.username = new Username(email_address);
    }

    public void updateUsername(String username) {
        this.username = new Username(username);
    }

    public void updateStreetAddress(String street, String number, String city, String state, String postalCode, String country) {
        this.streetAddress = new StreetAddress(street, number, city, state, postalCode, country);
    }
}
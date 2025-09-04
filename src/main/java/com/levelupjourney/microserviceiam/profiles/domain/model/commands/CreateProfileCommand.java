package com.levelupjourney.microserviceiam.profiles.domain.model.commands;

public record CreateProfileCommand(
        String firstName,
        String lastName,
        String email,
        String street,
        String number,
        String city,
        String state,
        String postalCode,
        String country
) {
}

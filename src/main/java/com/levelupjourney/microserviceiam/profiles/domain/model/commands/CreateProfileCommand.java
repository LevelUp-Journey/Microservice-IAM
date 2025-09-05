package com.levelupjourney.microserviceiam.profiles.domain.model.commands;

public record CreateProfileCommand(
        String firstName,
        String lastName,
        String profileUrl
) {
}

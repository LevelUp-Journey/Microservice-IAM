package com.levelupjourney.microserviceiam.profiles.domain.model.queries;

import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.Username;

public record GetProfileByUsernameQuery(Username username) {
}

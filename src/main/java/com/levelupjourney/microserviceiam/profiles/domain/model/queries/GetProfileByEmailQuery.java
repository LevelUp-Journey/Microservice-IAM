package com.levelupjourney.microserviceiam.profiles.domain.model.queries;

import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.EmailAddress;

public record GetProfileByEmailQuery(EmailAddress emailAddress) {
}

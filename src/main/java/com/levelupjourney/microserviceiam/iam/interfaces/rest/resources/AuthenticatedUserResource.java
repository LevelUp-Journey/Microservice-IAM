package com.levelupjourney.microserviceiam.iam.interfaces.rest.resources;

import java.util.UUID;

public record AuthenticatedUserResource(UUID id, String email, String token, String refreshToken) {

}

package com.levelupjourney.microserviceiam.IAM.interfaces.rest.resources;

import java.util.List;

public record UserResource(java.util.UUID id, String username, List<String> roles) {
}

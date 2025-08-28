package com.levelupjourney.microserviceiam.IAM.domain.model.commands;

import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.EmailAddress;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.Role;

import java.util.Map;
import java.util.Set;

public record OAuth2SignInCommand(
    AuthProvider provider,
    String providerUserId,
    EmailAddress email,
    String name,
    Map<String, Object> attributes,
    Set<Role> roles
) {
    public OAuth2SignInCommand {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is required");
        }
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("Provider user ID is required");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
    }

    public OAuth2SignInCommand(AuthProvider provider, String providerUserId, EmailAddress email, String name) {
        this(provider, providerUserId, email, name, Map.of(), Set.of(Role.getDefaultRole()));
    }
}
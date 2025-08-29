package com.levelupjourney.microserviceiam.IAM.application.internal.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("authenticationService")
public class AuthenticationService {
    
    public boolean canAccessAccount(Authentication authentication, UUID accountId) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String tokenAccountId = jwt.getSubject();
            return accountId.toString().equals(tokenAccountId);
        }
        return false;
    }
    
    public UUID getAccountIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String accountId = jwt.getSubject();
            return UUID.fromString(accountId);
        }
        throw new IllegalArgumentException("Invalid authentication principal - JWT required");
    }
    
    public boolean hasRole(Authentication authentication, String role) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsStringList("roles").contains(role);
        }
        return false;
    }
}
package com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;

public interface TokenService {
    String generateToken(Account account);
    String generateRefreshToken(Account account);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
    String getAccountIdFromToken(String token);
    boolean isTokenExpired(String token);
}
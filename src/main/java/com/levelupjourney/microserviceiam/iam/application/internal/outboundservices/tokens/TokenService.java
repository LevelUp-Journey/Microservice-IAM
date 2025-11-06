package com.levelupjourney.microserviceiam.iam.application.internal.outboundservices.tokens;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;

import java.util.List;

import java.util.List;

/**
 * TokenService interface
 * This interface is used to generate and validate tokens
 */
public interface TokenService {

    /**
     * Generate a token for a given username
     * @param email the email
     * @return String the token
     */
    String generateToken(String email);

    /**
     * Generate a refresh token for a given email
     * @param email the email
     * @return String the refresh token
     */
    String generateRefreshToken(String email);

    /**
     * Extract the email from a token
     * @param token the token
     * @return String the email
     */
    String getEmailFromToken(String token);

    /**
     * Validate a token
     * @param token the token
     * @return boolean true if the token is valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Validate a refresh token
     * @param refreshToken the refresh token
     * @return boolean true if the refresh token is valid, false otherwise
     */
    boolean validateRefreshToken(String refreshToken);

    /**
     * Extract the email from a refresh token
     * @param refreshToken the refresh token
     * @return String the email
     */
    String getEmailFromRefreshToken(String refreshToken);

    /**
     * Generate a token for a given user
     * @param user the user
     * @return String the token
     */
    String generateToken(User user);

    /**
     * Extract the userId from a token
     * @param token the token
     * @return Long the userId
     */
    Long getUserIdFromToken(String token);

    /**
     * Extract the roles from a token
     * @param token the token
     * @return List<String> the roles
     */
    List<String> getRolesFromToken(String token);
}

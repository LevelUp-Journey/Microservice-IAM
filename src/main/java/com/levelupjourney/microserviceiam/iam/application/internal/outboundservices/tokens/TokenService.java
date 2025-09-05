package com.levelupjourney.microserviceiam.iam.application.internal.outboundservices.tokens;

/**
 * TokenService interface
 * This interface is used to generate and validate tokens
 */
public interface TokenService {

    /**
     * Generate a token for a given username
     * @param username the username
     * @return String the token
     */
    String generateToken(String email_address);

    /**
     * Generate a refresh token for a given email address
     * @param email_address the email address
     * @return String the refresh token
     */
    String generateRefreshToken(String email_address);

    /**
     * Extract the email_address from a token
     * @param token the token
     * @return String the email_address
     */
    String getEmailAddressFromToken(String token);

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
     * Extract the email_address from a refresh token
     * @param refreshToken the refresh token
     * @return String the email_address
     */
    String getEmailAddressFromRefreshToken(String refreshToken);
}

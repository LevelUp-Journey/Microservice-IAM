package com.levelupjourney.microserviceiam.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record EmailAddress(String email) {

    // RFC 5322 compliant regex (simplified but comprehensive)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final int MAX_LENGTH = 255;
    private static final int MAX_LOCAL_PART = 64; // part before @
    private static final int MAX_DOMAIN_PART = 253; // part after @

    public EmailAddress {
        validateEmail(email);
    }

    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }

        String trimmedEmail = email.trim();
        
        if (trimmedEmail.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Email address cannot exceed " + MAX_LENGTH + " characters");
        }

        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email address format");
        }

        // Additional RFC 5322 validations
        String[] parts = trimmedEmail.split("@");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Email must contain exactly one @ symbol");
        }

        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.length() > MAX_LOCAL_PART) {
            throw new IllegalArgumentException("Email local part cannot exceed " + MAX_LOCAL_PART + " characters");
        }

        if (domainPart.length() > MAX_DOMAIN_PART) {
            throw new IllegalArgumentException("Email domain part cannot exceed " + MAX_DOMAIN_PART + " characters");
        }

        // Check for consecutive dots
        if (localPart.contains("..") || domainPart.contains("..")) {
            throw new IllegalArgumentException("Email cannot contain consecutive dots");
        }

        // Check for starting/ending dots
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            throw new IllegalArgumentException("Email local part cannot start or end with a dot");
        }

        if (domainPart.startsWith(".") || domainPart.endsWith(".")) {
            throw new IllegalArgumentException("Email domain part cannot start or end with a dot");
        }
    }

    /**
     * Get normalized email (lowercase)
     * @return normalized email address
     */
    public String normalized() {
        return email != null ? email.trim().toLowerCase() : null;
    }

    /**
     * Get the local part (before @)
     * @return local part of email
     */
    public String getLocalPart() {
        if (email == null) return null;
        String[] parts = email.split("@");
        return parts.length > 0 ? parts[0] : null;
    }

    /**
     * Get the domain part (after @)
     * @return domain part of email
     */
    public String getDomainPart() {
        if (email == null) return null;
        String[] parts = email.split("@");
        return parts.length > 1 ? parts[1] : null;
    }
}
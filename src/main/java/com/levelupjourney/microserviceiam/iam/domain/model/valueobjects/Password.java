package com.levelupjourney.microserviceiam.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record Password(String password) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    // At least one lowercase letter
    private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
    // At least one uppercase letter  
    private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
    // At least one digit
    private static final Pattern HAS_DIGIT = Pattern.compile(".*[0-9].*");
    // At least one special character
    private static final Pattern HAS_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    // No common weak patterns
    private static final Pattern NO_SEQUENTIAL = Pattern.compile("^(?!.*(?:012|123|234|345|456|567|678|789|890|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)).*$");

    public Password {
        validatePassword(password);
    }

    private static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Password cannot exceed " + MAX_LENGTH + " characters");
        }

        // Check for at least one lowercase letter
        if (!HAS_LOWERCASE.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        // Check for at least one uppercase letter
        if (!HAS_UPPERCASE.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        // Check for at least one digit
        if (!HAS_DIGIT.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }

        // Check for at least one special character
        if (!HAS_SPECIAL.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;':\"\\,.<>?/)");
        }

        // Check for no sequential patterns
        if (!NO_SEQUENTIAL.matcher(password.toLowerCase()).matches()) {
            throw new IllegalArgumentException("Password cannot contain sequential characters (123, abc, etc.)");
        }

        // Check for common weak passwords
        String lowerPassword = password.toLowerCase();
        String[] commonPasswords = {
            "password", "123456789", "qwerty", "admin", "letmein", 
            "welcome", "monkey", "dragon", "master", "shadow",
            "password123", "admin123", "qwerty123"
        };
        
        for (String weak : commonPasswords) {
            if (lowerPassword.contains(weak)) {
                throw new IllegalArgumentException("Password cannot contain common weak patterns");
            }
        }

        // Check for repeated characters (more than 3 consecutive)
        for (int i = 0; i < password.length() - 3; i++) {
            char current = password.charAt(i);
            boolean hasRepeated = true;
            for (int j = i + 1; j < i + 4 && j < password.length(); j++) {
                if (password.charAt(j) != current) {
                    hasRepeated = false;
                    break;
                }
            }
            if (hasRepeated) {
                throw new IllegalArgumentException("Password cannot contain more than 3 consecutive identical characters");
            }
        }
    }

    /**
     * Get password strength score (1-5)
     * @return strength score
     */
    public int getStrengthScore() {
        if (password == null) return 0;
        
        int score = 0;
        
        // Length score
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety
        if (HAS_LOWERCASE.matcher(password).matches()) score++;
        if (HAS_UPPERCASE.matcher(password).matches()) score++;
        if (HAS_DIGIT.matcher(password).matches()) score++;
        if (HAS_SPECIAL.matcher(password).matches()) score++;
        
        return Math.min(score, 5);
    }

    /**
     * Check if password is considered strong
     * @return true if strong (score >= 4)
     */
    public boolean isStrong() {
        return getStrengthScore() >= 4;
    }
}
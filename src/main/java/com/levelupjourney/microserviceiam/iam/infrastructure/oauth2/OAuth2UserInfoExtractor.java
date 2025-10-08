package com.levelupjourney.microserviceiam.iam.infrastructure.oauth2;

import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.OAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OAuth2 User Info Extractor
 * Extracts user information from different OAuth2 providers
 *
 * @author LevelUp Journey Team
 */
@Service
public class OAuth2UserInfoExtractor {

    /**
     * Extracts user information from OAuth2User based on provider
     *
     * @param oauth2User The OAuth2 user object
     * @param provider The OAuth2 provider name (e.g., "google", "github")
     * @return OAuth2UserInfo containing extracted user information
     */
    public OAuth2UserInfo extractUserInfo(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        return switch (provider.toLowerCase()) {
            case "google" -> extractGoogleUserInfo(attributes);
            case "github" -> extractGithubUserInfo(attributes);
            default -> extractDefaultUserInfo(attributes);
        };
    }

    /**
     * Extracts provider name from OAuth2 authentication
     *
     * @param oauth2User The OAuth2 user object
     * @return The provider name (e.g., "google", "github")
     */
    public String extractProvider(OAuth2User oauth2User) {
        // In Spring Security OAuth2, the provider can be inferred from the authorities
        // or we can use a different approach based on the attributes
        Map<String, Object> attributes = oauth2User.getAttributes();

        if (attributes.containsKey("sub")) {
            return "google";
        } else if (attributes.containsKey("login")) {
            return "github";
        }

        return "unknown";
    }

    private OAuth2UserInfo extractGoogleUserInfo(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");
        String picture = (String) attributes.get("picture");

        // Split name if given_name and family_name are not available
        String firstName = givenName != null ? givenName : extractFirstName(name);
        String lastName = familyName != null ? familyName : extractLastName(name);

        return new OAuth2UserInfo(firstName, lastName, picture, email);
    }

    private OAuth2UserInfo extractGithubUserInfo(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String login = (String) attributes.get("login");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("avatar_url");

        // GitHub might not provide email, use login as identifier
        String userEmail = email != null ? email : login + "@github.oauth";

        // Extract first and last name from name field
        String firstName = extractFirstName(name != null ? name : login);
        String lastName = extractLastName(name != null ? name : "");

        return new OAuth2UserInfo(firstName, lastName, avatarUrl, userEmail);
    }

    private OAuth2UserInfo extractDefaultUserInfo(Map<String, Object> attributes) {
        String email = (String) attributes.getOrDefault("email", "unknown@oauth.provider");
        String name = (String) attributes.getOrDefault("name", "Unknown User");

        return new OAuth2UserInfo(
                extractFirstName(name),
                extractLastName(name),
                null,
                email
        );
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Unknown";
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}

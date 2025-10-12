package com.levelupjourney.microserviceiam.iam.interfaces.acl;

import com.levelupjourney.microserviceiam.iam.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.iam.domain.model.queries.GetUserByIdQuery;
import com.levelupjourney.microserviceiam.iam.domain.model.queries.GetUserByEmailQuery;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.OAuth2UserInfo;
import com.levelupjourney.microserviceiam.iam.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserQueryService;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * IamContextFacade
 * <p>
 *     This class is a facade for the IAM context. It provides a simple interface for other bounded contexts to interact with the
 *     IAM context.
 *     This class is a part of the ACL layer.
 * </p>
 *
 */
public class IamContextFacade {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public IamContextFacade(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    /**
     * Creates a user with the given username and password.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return The id of the created user.
     */
    public UUID createUser(String username, String password) {
        var signUpCommand = new SignUpCommand(username, password, List.of(Role.getDefaultRole()));
        var result = userCommandService.handle(signUpCommand);
        if (result.isEmpty()) return null;
        return result.get().getId();
    }

    /**
     * Creates a user with the given username, password and roles.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param roleNames The names of the roles of the user. When a role does not exist, it is ignored.
     * @return The id of the created user.
     */
    public UUID createUser(String username, String password, List<String> roleNames) {
        var roles = roleNames != null ? roleNames.stream().map(Role::toRoleFromName).toList() : new ArrayList<Role>();
        var signUpCommand = new SignUpCommand(username, password, roles);
        var result = userCommandService.handle(signUpCommand);
        if (result.isEmpty()) return null;
        return result.get().getId();
    }

    /**
     * Fetches the id of the user with the given username.
     * @param username The username of the user.
     * @return The id of the user.
     */
    public UUID fetchUserIdByUsername(String username) {
        var getUserByUsernameQuery = new GetUserByEmailQuery(username);
        var result = userQueryService.handle(getUserByUsernameQuery);
        if (result.isEmpty()) return null;
        return result.get().getId();
    }

    /**
     * Fetches the username of the user with the given id.
     * @param userId The id of the user.
     * @return The username of the user.
     */
    public String fetchUsernameByUserId(UUID userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(getUserByIdQuery);
        if (result.isEmpty()) return Strings.EMPTY;
        return result.get().getEmail();
    }

    /**
     * Extracts OAuth2 user information from provider attributes.
     * This method is called during OAuth2 authentication to get user profile data.
     * @param providerName The OAuth2 provider name (google, github)
     * @param attributes The OAuth2 user attributes from provider
     * @return OAuth2UserInfo with extracted user data
     */
    public OAuth2UserInfo extractOAuth2UserInfo(String providerName, java.util.Map<String, Object> attributes) {
        return switch (providerName.toLowerCase()) {
            case "google" -> extractGoogleUserInfo(attributes);
            case "github" -> extractGitHubUserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + providerName);
        };
    }

    private OAuth2UserInfo extractGoogleUserInfo(java.util.Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String profileUrl = (String) attributes.get("picture");
        
        return new OAuth2UserInfo(firstName, lastName, profileUrl, email);
    }

    private OAuth2UserInfo extractGitHubUserInfo(java.util.Map<String, Object> attributes) {
        String login = (String) attributes.get("login");
        String name = (String) attributes.get("name");
        String profileUrl = (String) attributes.get("avatar_url");
        String email = login + "@github.oauth"; // GitHub might not provide public email
        
        // Parse name into firstName and lastName if available
        String firstName = null;
        String lastName = null;
        if (name != null && !name.trim().isEmpty()) {
            String[] nameParts = name.trim().split("\\s+", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : null;
        }
        
        return new OAuth2UserInfo(firstName, lastName, profileUrl, email);
    }

}

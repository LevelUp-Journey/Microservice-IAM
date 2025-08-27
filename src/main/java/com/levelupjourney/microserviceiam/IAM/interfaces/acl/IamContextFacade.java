package com.levelupjourney.microserviceiam.IAM.interfaces.acl;

import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.queries.GetUserByIdQuery;
import com.levelupjourney.microserviceiam.IAM.domain.model.queries.GetUserByUsernameQuery;
import com.levelupjourney.microserviceiam.IAM.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.IAM.domain.services.UserQueryService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * IamContextFacade
 * <p>
 *     This class is a facade for the IAM context. It provides a simple interface for other bounded contexts to interact with the
 *     IAM context.
 *     This class is a part of the ACL layer.
 * </p>
 *
 */
@Service
public class IamContextFacade {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public IamContextFacade(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    /**
     * Creates a user with the given username and password.
     * User is assigned STUDENT role by default.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return The id of the created user.
     */
    public java.util.UUID createUser(String username, String password) {
        var signUpCommand = new SignUpCommand(username, password);
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
     * @deprecated Use createUser(String, String) instead. Users are assigned STUDENT role by default.
     */
    @Deprecated
    public java.util.UUID createUser(String username, String password, List<String> roleNames) {
        // For backward compatibility, create user with default role and then update roles if needed
        var signUpCommand = new SignUpCommand(username, password);
        var result = userCommandService.handle(signUpCommand);
        if (result.isEmpty()) return null;
        return result.get().getId();
    }

    /**
     * Fetches the id of the user with the given username.
     * @param username The username of the user.
     * @return The id of the user.
     */
    public java.util.UUID fetchUserIdByUsername(String username) {
        var getUserByUsernameQuery = new GetUserByUsernameQuery(username);
        var result = userQueryService.handle(getUserByUsernameQuery);
        if (result.isEmpty()) return null;
        return result.get().getId();
    }

    /**
     * Fetches the username of the user with the given id.
     * @param userId The id of the user.
     * @return The username of the user.
     */
    public String fetchUsernameByUserId(java.util.UUID userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(getUserByIdQuery);
        if (result.isEmpty()) return Strings.EMPTY;
        return result.get().getUsername();
    }

}

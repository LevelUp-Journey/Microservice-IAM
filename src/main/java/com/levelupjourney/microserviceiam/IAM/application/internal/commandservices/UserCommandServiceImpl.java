package com.levelupjourney.microserviceiam.IAM.application.internal.commandservices;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.hashing.HashingService;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.application.internal.services.UserAuditService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SignInCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.UpdateUserProfileCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.AuthIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserSession;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
import com.levelupjourney.microserviceiam.IAM.domain.services.RoleService;
import com.levelupjourney.microserviceiam.IAM.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.AuthIdentityRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.UserRepository;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.UserSessionRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * User command service implementation
 * <p>
 *     This class implements the {@link UserCommandService} interface and provides the implementation for the
 *     {@link SignInCommand} and {@link SignUpCommand} commands.
 * </p>
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final UserSessionRepository userSessionRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleService roleService;
    private final UserAuditService userAuditService;

    public UserCommandServiceImpl(UserRepository userRepository, AuthIdentityRepository authIdentityRepository, UserSessionRepository userSessionRepository, HashingService hashingService, TokenService tokenService, RoleService roleService, UserAuditService userAuditService) {
        this.userRepository = userRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.userSessionRepository = userSessionRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleService = roleService;
        this.userAuditService = userAuditService;
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        User user = findUserByEmail(command.email());
        AuthIdentity authIdentity = findLocalAuthIdentity(user);
        validatePassword(command.password(), authIdentity, user);
        
        updateLastLogin(authIdentity);
        createSuccessfulLoginSession(user);
        
        String token = tokenService.generateToken(user.getUsername());
        return Optional.of(ImmutablePair.of(user, token));
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        validateEmailNotExists(command.email());
        validateUsernameNotExists(command.username());
        
        User user = createUserWithDefaults(command.username(), command.email());
        createLocalAuthIdentity(user, command.password());
        createSuccessfulSignUpSession(user);
        
        return Optional.of(user);
    }

    @Override
    public Optional<User> handle(UpdateUserProfileCommand command) {
        var user = userRepository.findById(command.userId());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User existingUser = user.get();
        boolean hasChanges = false;
        
        // For now, we assume the user is updating their own profile
        // In a real application, you would get this from the security context
        User changedByUser = existingUser;
        
        if (command.username() != null && !command.username().equals(existingUser.getUsername())) {
            validateUsernameNotExists(command.username());
            String oldUsername = existingUser.getUsername();
            existingUser.setUsername(command.username());
            userAuditService.logFieldChange(existingUser, changedByUser, "username", oldUsername, command.username());
            hasChanges = true;
        }
        
        if (command.name() != null && !command.name().equals(existingUser.getName())) {
            String oldName = existingUser.getName();
            existingUser.setName(command.name());
            userAuditService.logFieldChange(existingUser, changedByUser, "name", oldName, command.name());
            hasChanges = true;
        }
        
        if (command.avatarUrl() != null && !command.avatarUrl().equals(existingUser.getAvatarUrl())) {
            String oldAvatarUrl = existingUser.getAvatarUrl();
            existingUser.setAvatarUrl(command.avatarUrl());
            userAuditService.logFieldChange(existingUser, changedByUser, "avatar_url", oldAvatarUrl, command.avatarUrl());
            hasChanges = true;
        }
        
        if (command.password() != null) {
            updateUserPassword(existingUser, command.password());
            userAuditService.logFieldChange(existingUser, changedByUser, "password", "[REDACTED]", "[REDACTED]", "Password updated");
            hasChanges = true;
        }
        
        if (hasChanges) {
            userRepository.save(existingUser);
        }
        
        return Optional.of(existingUser);
    }

    private User findUserByEmail(String email) {
        var user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            createFailedSession(null, "User not found");
            throw new RuntimeException("User not found");
        }
        return user.get();
    }

    private AuthIdentity findLocalAuthIdentity(User user) {
        var authIdentity = authIdentityRepository.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL);
        if (authIdentity.isEmpty()) {
            createFailedSession(user, "No local authentication found");
            throw new RuntimeException("No local authentication found for user");
        }
        return authIdentity.get();
    }

    private void validatePassword(String password, AuthIdentity authIdentity, User user) {
        if (!hashingService.matches(password, authIdentity.getPasswordHash())) {
            createFailedSession(user, "Invalid password");
            throw new RuntimeException("Invalid password");
        }
    }

    private void updateLastLogin(AuthIdentity authIdentity) {
        authIdentity.updateLastLogin();
        authIdentityRepository.save(authIdentity);
    }

    private void createSuccessfulLoginSession(User user) {
        var session = new UserSession(user, AuthProvider.LOCAL, "LOGIN", true);
        userSessionRepository.save(session);
        user.addSession(session);
        userRepository.save(user);
    }

    private void createFailedSession(User user, String errorMessage) {
        var failedSession = new UserSession(user, AuthProvider.LOCAL, "LOGIN", false, errorMessage);
        userSessionRepository.save(failedSession);
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
    }

    private void validateUsernameNotExists(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
    }


    private User createUserWithDefaults(String username, String email) {
        var user = new User(username);
        user.addEmail(email, true, false, null);
        user.addRole(roleService.getOrCreateDefaultRole());
        return userRepository.save(user);
    }

    private AuthIdentity createLocalAuthIdentity(User user, String password) {
        var authIdentity = new AuthIdentity(user, AuthProvider.LOCAL, hashingService.encode(password));
        authIdentityRepository.save(authIdentity);
        user.addAuthIdentity(authIdentity);
        return authIdentity;
    }

    private void createSuccessfulSignUpSession(User user) {
        var session = new UserSession(user, AuthProvider.LOCAL, "SIGNUP", true);
        userSessionRepository.save(session);
        user.addSession(session);
        userRepository.save(user);
    }

    private void updateUserPassword(User user, String newPassword) {
        var authIdentity = authIdentityRepository.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL);
        if (authIdentity.isPresent()) {
            authIdentity.get().setPasswordHash(hashingService.encode(newPassword));
            authIdentityRepository.save(authIdentity.get());
        } else {
            // Create new local auth identity if it doesn't exist
            var newAuthIdentity = new AuthIdentity(user, AuthProvider.LOCAL, hashingService.encode(newPassword));
            authIdentityRepository.save(newAuthIdentity);
            user.addAuthIdentity(newAuthIdentity);
        }
    }
}

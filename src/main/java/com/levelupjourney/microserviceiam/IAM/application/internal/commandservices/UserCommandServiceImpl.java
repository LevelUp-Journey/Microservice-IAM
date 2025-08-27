package com.levelupjourney.microserviceiam.IAM.application.internal.commandservices;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.hashing.HashingService;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SignInCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.AuthIdentity;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserSession;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.AuthProvider;
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

    public UserCommandServiceImpl(UserRepository userRepository, AuthIdentityRepository authIdentityRepository, UserSessionRepository userSessionRepository, HashingService hashingService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.userSessionRepository = userSessionRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
    }

    /**
     * Handle the sign-in command
     * <p>
     *     This method handles the {@link SignInCommand} command and returns the user and the token.
     * </p>
     * @param command the sign-in command containing the username and password
     * @return and optional containing the user matching the username and the generated token
     * @throws RuntimeException if the user is not found or the password is invalid
     */
    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty()) {
            // Create failed session for non-existent user
            var failedSession = new UserSession(null, AuthProvider.LOCAL, "LOGIN", false, "User not found");
            userSessionRepository.save(failedSession);
            throw new RuntimeException("User not found");
        }
        
        // Find local auth identity for this user
        var authIdentity = authIdentityRepository.findByUserIdAndProvider(user.get().getId(), AuthProvider.LOCAL);
        if (authIdentity.isEmpty()) {
            // Create failed session
            var failedSession = new UserSession(user.get(), AuthProvider.LOCAL, "LOGIN", false, "No local authentication found");
            userSessionRepository.save(failedSession);
            throw new RuntimeException("No local authentication found for user");
        }
        
        if (!hashingService.matches(command.password(), authIdentity.get().getPasswordHash())) {
            // Create failed session
            var failedSession = new UserSession(user.get(), AuthProvider.LOCAL, "LOGIN", false, "Invalid password");
            userSessionRepository.save(failedSession);
            throw new RuntimeException("Invalid password");
        }
        
        // Update last login
        authIdentity.get().updateLastLogin();
        authIdentityRepository.save(authIdentity.get());
        
        // Create successful session
        var session = new UserSession(user.get(), AuthProvider.LOCAL, "LOGIN", true);
        userSessionRepository.save(session);
        user.get().addSession(session);
        
        var token = tokenService.generateToken(user.get().getUsername());
        return Optional.of(ImmutablePair.of(user.get(), token));
    }

    /**
     * Handle the sign-up command
     * <p>
     *     This method handles the {@link SignUpCommand} command and returns the user.
     *     Users are automatically assigned STUDENT role by default.
     * </p>
     * @param command the sign-up command containing the username and password
     * @return the created user
     */
    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new RuntimeException("Username already exists");
        
        var user = new User(command.username());
        userRepository.save(user);
        
        // Create local auth identity
        var authIdentity = new AuthIdentity(user, AuthProvider.LOCAL, hashingService.encode(command.password()));
        authIdentityRepository.save(authIdentity);
        user.addAuthIdentity(authIdentity);
        
        return Optional.of(user);
    }
}

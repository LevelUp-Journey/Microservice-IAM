package com.levelupjourney.microserviceiam.iam.application.internal.commandservices;

import com.levelupjourney.microserviceiam.iam.application.internal.outboundservices.hashing.HashingService;
import com.levelupjourney.microserviceiam.iam.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.iam.domain.model.commands.SignInCommand;
import com.levelupjourney.microserviceiam.iam.domain.model.commands.SignUpCommand;
import com.levelupjourney.microserviceiam.iam.domain.model.entities.Role;
import com.levelupjourney.microserviceiam.iam.domain.model.events.UserRegisteredEvent;
import com.levelupjourney.microserviceiam.iam.domain.model.valueobjects.TokenPair;
import com.levelupjourney.microserviceiam.iam.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.iam.infrastructure.eventpublishers.IamEventPublisher;
import com.levelupjourney.microserviceiam.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.levelupjourney.microserviceiam.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final IamEventPublisher eventPublisher;

    public UserCommandServiceImpl(UserRepository userRepository,
                                 HashingService hashingService,
                                 TokenService tokenService,
                                 RoleRepository roleRepository,
                                 IamEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handle the sign-in command
     * <p>
     *     This method handles the {@link SignInCommand} command and returns the user and the token pair.
     * </p>
     * @param command the sign-in command containing the email_address and password
     * @return and optional containing the user matching the email_address and the generated token pair
     * @throws RuntimeException if the user is not found or the password is invalid
     */
    @Override
    public Optional<ImmutablePair<User, TokenPair>> handle(SignInCommand command) {
        var user = userRepository.findByEmail_address(command.email_address());
        if (user.isEmpty())
            throw new RuntimeException("User not found");
        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid password");
        var accessToken = tokenService.generateToken(user.get().getEmail_address());
        var refreshToken = tokenService.generateRefreshToken(user.get().getEmail_address());
        var tokenPair = new TokenPair(accessToken, refreshToken);
        return Optional.of(ImmutablePair.of(user.get(), tokenPair));
    }

    /**
     * Handle the sign-up command
     * <p>
     *     This method handles the {@link SignUpCommand} command and returns the user.
     * </p>
     * @param command the sign-up command containing the username and password
     * @return the created user
     */
    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByEmail_address(command.email_address()))
            throw new RuntimeException("Email address already exists");
        var validatedRoles = Role.validateRoleSet(command.roles());
        var roles = validatedRoles.stream().map(role -> roleRepository.findByName(role.getName()).orElseThrow(() -> new RuntimeException("Role name not found"))).toList();
        var user = new User(command.email_address(), hashingService.encode(command.password()), roles);
        userRepository.save(user);

        var savedUser = userRepository.findByEmail_address(command.email_address());

        // Publish user registered event for local registration
        if (savedUser.isPresent()) {
            publishUserRegisteredEvent(savedUser.get(), "local");
        }

        return savedUser;
    }

    /**
     * Publishes a user registered event to Kafka
     * Extracts email parts to generate basic first/last name for local registrations
     *
     * @param user the registered user
     * @param provider the provider ("local" for traditional registration, "google", "github" for OAuth2)
     */
    private void publishUserRegisteredEvent(User user, String provider) {
        String email = user.getEmail_address();
        String emailPrefix = email.split("@")[0];

        // Extract basic name from email (e.g., "john.doe@example.com" -> "John" "Doe")
        String[] nameParts = emailPrefix.split("[._-]");
        String firstName = nameParts.length > 0 ? capitalize(nameParts[0]) : "User";
        String lastName = nameParts.length > 1 ? capitalize(nameParts[1]) : "";

        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                email,
                firstName,
                lastName,
                null, // No profile URL for local registration
                provider,
                LocalDateTime.now()
        );

        eventPublisher.publishUserRegistered(event);
    }

    /**
     * Capitalizes the first letter of a string
     *
     * @param str the string to capitalize
     * @return the capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}

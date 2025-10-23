package com.levelupjourney.microserviceiam.iam.interfaces.rest;

import com.levelupjourney.microserviceiam.iam.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.iam.domain.model.queries.GetUserByEmailQuery;
import com.levelupjourney.microserviceiam.iam.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserQueryService;
import com.levelupjourney.microserviceiam.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.resources.SignInResource;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.resources.SignUpResource;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.resources.UserResource;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.levelupjourney.microserviceiam.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationController
 * <p>
 *     This controller is responsible for handling authentication requests.
 *     It exposes two endpoints:
 *     <ul>
 *         <li>POST /api/v1/auth/sign-in</li>
 *         <li>POST /api/v1/auth/sign-up</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Available Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final BearerTokenService tokenService;
    private final TokenService refreshTokenService;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    public AuthenticationController(UserCommandService userCommandService, UserQueryService userQueryService,
                                   BearerTokenService tokenService, TokenService refreshTokenService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Handles the sign-in request.
     * @param signInResource the sign-in request body.
     * @return the authenticated user resource.
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Sign-in", description = "Sign-in with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "401", description = "Invalid password.")})
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        try {
            var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
            var authenticatedUser = userCommandService.handle(signInCommand);
            if (authenticatedUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var user = authenticatedUser.get().getLeft();
            var tokenPair = authenticatedUser.get().getRight();
            var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(user, tokenPair.accessToken(), tokenPair.refreshToken());
            return ResponseEntity.ok(authenticatedUserResource);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("Invalid password")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Handles the sign-up request.
     * @param signUpResource the sign-up request body.
     * @return the created user resource.
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Sign-up", description = "Sign-up with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "409", description = "Email already exists.")})
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        try {
            logger.info("Sign up resource: {}", signUpResource);
            var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
            
            var user = userCommandService.handle(signUpCommand);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
            return new ResponseEntity<>(userResource, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email address already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            if (e.getMessage().contains("Password")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    /**
     * Validates the current token/session.
     * @param request the HTTP request.
     * @return the validation response.
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate Token", description = "Validate current user token/session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid."),
            @ApiResponse(responseCode = "401", description = "Token is invalid or expired.")})
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (tokenService.validateToken(token)) {
                    String email = tokenService.getEmailFromToken(token);
                    return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "message", "Token is valid",
                        "email", email
                    ));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "valid", false,
                    "message", "Invalid token: " + e.getMessage()
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "valid", false,
            "message", "Invalid or missing token"
        ));
    }

    /**
     * Handles the refresh token request.
     * Gets the refresh token from refresh_token header and generates new access token.
     * @param request the HTTP request.
     * @return the new access token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Generate new access token using refresh token from refresh_token header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully."),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token.")})
    public ResponseEntity<String> refreshToken(HttpServletRequest request) {
        try {
            String refreshToken = request.getHeader("refresh_token");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh_token header");
            }
            
            if (!refreshTokenService.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
            }
            
            String emailAddress = refreshTokenService.getEmailFromRefreshToken(refreshToken);
            var getUserQuery = new GetUserByEmailQuery(emailAddress);
            var user = userQueryService.handle(getUserQuery);
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            String newAccessToken = refreshTokenService.generateToken(emailAddress);
            
            return ResponseEntity.ok(newAccessToken);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token: " + e.getMessage());
        }
    }
}

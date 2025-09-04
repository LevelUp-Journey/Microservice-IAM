package com.levelupjourney.microserviceiam.iam.interfaces.rest;

import com.levelupjourney.microserviceiam.iam.domain.services.UserCommandService;
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
    private final BearerTokenService tokenService;

    public AuthenticationController(UserCommandService userCommandService, BearerTokenService tokenService) {
        this.userCommandService = userCommandService;
        this.tokenService = tokenService;
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
            @ApiResponse(responseCode = "404", description = "User not found.")})
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var authenticatedUser = userCommandService.handle(signInCommand);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(authenticatedUser.get().getLeft(), authenticatedUser.get().getRight());
        return ResponseEntity.ok(authenticatedUserResource);
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
            @ApiResponse(responseCode = "400", description = "Bad request.")})
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);

    }

    /**
     * Handles the logout request.
     * @param request the HTTP request.
     * @return the logout response.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current user session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully.")})
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
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
                    String username = tokenService.getUsernameFromToken(token);
                    return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "message", "Token is valid",
                        "username", username
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
}

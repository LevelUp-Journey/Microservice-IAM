package com.levelupjourney.microserviceiam.IAM.interfaces.web;

import com.levelupjourney.microserviceiam.IAM.application.internal.commandservices.OAuth2CommandService;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.oauth.GoogleOAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

@Controller
@Tag(name = "OAuth2 Authentication", description = "OAuth2 Authentication Flow (Non-REST)")
public class OAuth2Controller {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);

    private final GoogleOAuth2Service googleOAuth2Service;
    private final OAuth2CommandService oAuth2CommandService;

    private static final String OAUTH_STATE_COOKIE = "oauth_state";
    private static final String FRONTEND_URL = "http://localhost:8080/auth/success";

    public OAuth2Controller(GoogleOAuth2Service googleOAuth2Service,
                           OAuth2CommandService oAuth2CommandService) {
        this.googleOAuth2Service = googleOAuth2Service;
        this.oAuth2CommandService = oAuth2CommandService;
    }

    @GetMapping("/auth/google")
    @Operation(
        summary = "Start Google OAuth2 authentication",
        description = "Redirects the user to Google's OAuth2 authorization server"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirect to Google OAuth2 authorization server"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void startGoogleAuth(HttpServletResponse response) throws IOException {
        String state = googleOAuth2Service.generateState();
        String authUrl = googleOAuth2Service.buildAuthorizationUrl(state);

        Cookie stateCookie = new Cookie(OAUTH_STATE_COOKIE, state);
        stateCookie.setHttpOnly(true);
        stateCookie.setSecure(false); // Set to true in production with HTTPS
        stateCookie.setPath("/");
        stateCookie.setMaxAge(600); // 10 minutes
        response.addCookie(stateCookie);

        response.sendRedirect(authUrl);
    }

    @GetMapping("/auth/callback")
    @Operation(
        summary = "Handle Google OAuth2 callback",
        description = "Processes the authorization code from Google and creates/authenticates the user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirect to frontend with authentication token"),
        @ApiResponse(responseCode = "400", description = "Bad request - missing or invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Authentication failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void handleGoogleCallback(
        @Parameter(description = "Authorization code from Google") 
        @RequestParam(required = false) String code,
        
        @Parameter(description = "State parameter for CSRF protection") 
        @RequestParam(required = false) String state,
        
        @Parameter(description = "Error parameter from Google") 
        @RequestParam(required = false) String error,
        
        HttpServletRequest request,
        HttpServletResponse response) throws IOException {

        try {
            logger.info("OAuth2 callback received - code: {}, state: {}, error: {}", 
                       code != null ? "present" : "null", 
                       state != null ? "present" : "null", 
                       error);

            if (error != null) {
                logger.error("OAuth2 error from Google: {}", error);
                response.sendRedirect(FRONTEND_URL + "?error=" + URLDecoder.decode(error, StandardCharsets.UTF_8));
                return;
            }

            if (code == null || state == null) {
                logger.error("Missing required parameters - code: {}, state: {}", code, state);
                response.sendRedirect(FRONTEND_URL + "?error=missing_parameters");
                return;
            }

            String storedState = getStoredState(request);
            logger.info("Stored state: {}, received state: {}", storedState, state);
            
            if (storedState == null) {
                logger.error("No stored state found in cookies");
                response.sendRedirect(FRONTEND_URL + "?error=invalid_state");
                return;
            }

            logger.info("Processing OAuth2 callback...");
            String jwt = oAuth2CommandService.processGoogleCallback(code, state, storedState);
            logger.info("JWT token generated successfully");

            clearStateCookie(response);

            String redirectUrl = FRONTEND_URL + "?token=" + jwt;
            response.sendRedirect(redirectUrl);

        } catch (SecurityException e) {
            logger.error("Security exception during OAuth2 callback", e);
            clearStateCookie(response);
            response.sendRedirect(FRONTEND_URL + "?error=security_violation");
        } catch (Exception e) {
            logger.error("Exception during OAuth2 callback", e);
            clearStateCookie(response);
            response.sendRedirect(FRONTEND_URL + "?error=authentication_failed");
        }
    }

    @GetMapping("/auth/status")
    @ResponseBody
    @Operation(
        summary = "Get OAuth2 authentication status",
        description = "Returns the current OAuth2 authentication status"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication status retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAuthStatus() {
        return ResponseEntity.ok().body(
            new AuthStatusResponse("OAuth2 authentication service is running", "ready")
        );
    }

    private String getStoredState(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> stateCookie = Arrays.stream(cookies)
                .filter(cookie -> OAUTH_STATE_COOKIE.equals(cookie.getName()))
                .findFirst();
            
            if (stateCookie.isPresent()) {
                return stateCookie.get().getValue();
            }
        }
        return null;
    }

    private void clearStateCookie(HttpServletResponse response) {
        Cookie stateCookie = new Cookie(OAUTH_STATE_COOKIE, "");
        stateCookie.setHttpOnly(true);
        stateCookie.setSecure(false); // Set to true in production with HTTPS
        stateCookie.setPath("/");
        stateCookie.setMaxAge(0);
        response.addCookie(stateCookie);
    }

    @GetMapping("/auth/success")
    @ResponseBody
    public ResponseEntity<String> authSuccess(@RequestParam(required = false) String token,
                                            @RequestParam(required = false) String error) {
        if (token != null) {
            return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
                      "<h1>✅ OAuth2 Login Successful!</h1>" +
                      "<p><strong>JWT Token:</strong></p>" +
                      "<textarea style='width:100%; height:100px;'>" + token + "</textarea>" +
                      "<p><em>Copy this token for API requests</em></p></body></html>");
        }
        
        if (error != null) {
            return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
                      "<h1>❌ OAuth2 Login Failed!</h1>" +
                      "<p><strong>Error:</strong> " + error + "</p></body></html>");
        }
        
        return ResponseEntity.ok()
            .header("Content-Type", "text/html")
            .body("<h1>OAuth2 Test Page</h1>" +
                  "<p>This page shows OAuth2 results</p>");
    }

    public static class AuthStatusResponse {
        private final String message;
        private final String status;

        public AuthStatusResponse(String message, String status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
}
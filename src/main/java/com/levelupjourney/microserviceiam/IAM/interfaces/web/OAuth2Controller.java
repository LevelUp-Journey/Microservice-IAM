package com.levelupjourney.microserviceiam.IAM.interfaces.web;

import com.levelupjourney.microserviceiam.IAM.application.internal.commandservices.OAuth2CommandServiceImpl;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.oauth.GoogleOAuth2Service;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.oauth.GitHubOAuth2Service;
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
@RequestMapping("/oauth2")
@Tag(name = "OAuth2 Authentication", description = "OAuth2 Authentication Flow for Google and GitHub")
public class OAuth2Controller {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);

    private final GoogleOAuth2Service googleOAuth2Service;
    private final GitHubOAuth2Service gitHubOAuth2Service;
    private final OAuth2CommandServiceImpl oAuth2CommandService;

    private static final String OAUTH_STATE_COOKIE = "oauth_state";
    private static final String OAUTH_PROVIDER_COOKIE = "oauth_provider";
    private static final String FRONTEND_URL = "http://localhost:8080/oauth2/success";

    public OAuth2Controller(GoogleOAuth2Service googleOAuth2Service,
                           GitHubOAuth2Service gitHubOAuth2Service,
                           OAuth2CommandServiceImpl oAuth2CommandService) {
        this.googleOAuth2Service = googleOAuth2Service;
        this.gitHubOAuth2Service = gitHubOAuth2Service;
        this.oAuth2CommandService = oAuth2CommandService;
    }

    @GetMapping("/google")
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

        logger.info("Starting Google OAuth2 authentication with state: {}", state);
        
        // Set state cookie with proper attributes
        Cookie stateCookie = new Cookie(OAUTH_STATE_COOKIE, state);
        stateCookie.setHttpOnly(true);
        stateCookie.setSecure(false); // Set to true in production with HTTPS
        stateCookie.setPath("/");
        stateCookie.setMaxAge(600); // 10 minutes
        response.addCookie(stateCookie);
        response.addHeader("Set-Cookie", OAUTH_STATE_COOKIE + "=" + state + "; Path=/; Max-Age=600; HttpOnly; SameSite=Lax");

        // Set provider cookie to identify Google
        Cookie providerCookie = new Cookie(OAUTH_PROVIDER_COOKIE, "google");
        providerCookie.setHttpOnly(true);
        providerCookie.setSecure(false);
        providerCookie.setPath("/");
        providerCookie.setMaxAge(600);
        response.addCookie(providerCookie);
        response.addHeader("Set-Cookie", OAUTH_PROVIDER_COOKIE + "=google; Path=/; Max-Age=600; HttpOnly; SameSite=Lax");

        logger.info("Redirecting to Google OAuth2 URL: {}", authUrl);
        response.sendRedirect(authUrl);
    }

    @GetMapping("/github")
    @Operation(
        summary = "Start GitHub OAuth2 authentication",
        description = "Redirects the user to GitHub's OAuth2 authorization server"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirect to GitHub OAuth2 authorization server"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void startGitHubAuth(HttpServletResponse response) throws IOException {
        String state = gitHubOAuth2Service.generateState();
        String authUrl = gitHubOAuth2Service.buildAuthorizationUrl(state);

        logger.info("Starting GitHub OAuth2 authentication with state: {}", state);
        
        // Set state cookie with proper attributes
        Cookie stateCookie = new Cookie(OAUTH_STATE_COOKIE, state);
        stateCookie.setHttpOnly(true);
        stateCookie.setSecure(false); // Set to true in production with HTTPS
        stateCookie.setPath("/");
        stateCookie.setMaxAge(600); // 10 minutes
        response.addCookie(stateCookie);
        response.addHeader("Set-Cookie", OAUTH_STATE_COOKIE + "=" + state + "; Path=/; Max-Age=600; HttpOnly; SameSite=Lax");

        // Set provider cookie to identify GitHub
        Cookie providerCookie = new Cookie(OAUTH_PROVIDER_COOKIE, "github");
        providerCookie.setHttpOnly(true);
        providerCookie.setSecure(false);
        providerCookie.setPath("/");
        providerCookie.setMaxAge(600);
        response.addCookie(providerCookie);
        response.addHeader("Set-Cookie", OAUTH_PROVIDER_COOKIE + "=github; Path=/; Max-Age=600; HttpOnly; SameSite=Lax");

        logger.info("Redirecting to GitHub OAuth2 URL: {}", authUrl);
        response.sendRedirect(authUrl);
    }

    @GetMapping("/callback")
    @Operation(
        summary = "Handle OAuth2 callback",
        description = "Processes the authorization code from OAuth2 providers (Google, GitHub) and creates/authenticates the user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirect to frontend with authentication token"),
        @ApiResponse(responseCode = "400", description = "Bad request - missing or invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Authentication failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void handleOAuth2Callback(
        @Parameter(description = "Authorization code from OAuth2 provider") 
        @RequestParam(required = false) String code,
        
        @Parameter(description = "State parameter for CSRF protection") 
        @RequestParam(required = false) String state,
        
        @Parameter(description = "Error parameter from OAuth2 provider") 
        @RequestParam(required = false) String error,
        
        HttpServletRequest request,
        HttpServletResponse response) throws IOException {

        try {
            logger.info("OAuth2 callback received - code: {}, state: {}, error: {}", 
                       code != null ? "present" : "null", 
                       state != null ? "present" : "null", 
                       error);

            if (error != null) {
                logger.error("OAuth2 error from provider: {}", error);
                response.sendRedirect(FRONTEND_URL + "?error=" + URLDecoder.decode(error, StandardCharsets.UTF_8));
                return;
            }

            if (code == null || state == null) {
                logger.error("Missing required parameters - code: {}, state: {}", code, state);
                response.sendRedirect(FRONTEND_URL + "?error=missing_parameters");
                return;
            }

            String storedState = getStoredState(request);
            String provider = getStoredProvider(request);
            logger.info("Stored state: {}, received state: {}, provider: {}", storedState, state, provider);
            
            // Debug: Log all cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    logger.info("Cookie: {} = {}", cookie.getName(), cookie.getValue());
                }
            } else {
                logger.info("No cookies found in request");
            }
            
            if (storedState == null) {
                logger.error("No stored state found in cookies");
                response.sendRedirect(FRONTEND_URL + "?error=invalid_state");
                return;
            }

            if (provider == null) {
                logger.error("No stored provider found in cookies");
                response.sendRedirect(FRONTEND_URL + "?error=invalid_provider");
                return;
            }

            logger.info("Processing OAuth2 callback for provider: {}", provider);
            String jwt;
            if ("google".equals(provider)) {
                jwt = oAuth2CommandService.processGoogleCallback(code, state, storedState);
            } else if ("github".equals(provider)) {
                jwt = oAuth2CommandService.processGitHubCallback(code, state, storedState);
            } else {
                logger.error("Unknown OAuth2 provider: {}", provider);
                response.sendRedirect(FRONTEND_URL + "?error=unknown_provider");
                return;
            }
            logger.info("JWT token generated successfully");

            clearOAuthCookies(response);

            String redirectUrl = FRONTEND_URL + "?token=" + jwt;
            response.sendRedirect(redirectUrl);

        } catch (SecurityException e) {
            logger.error("Security exception during OAuth2 callback", e);
            clearOAuthCookies(response);
            response.sendRedirect(FRONTEND_URL + "?error=security_violation");
        } catch (Exception e) {
            logger.error("Exception during OAuth2 callback", e);
            clearOAuthCookies(response);
            response.sendRedirect(FRONTEND_URL + "?error=authentication_failed");
        }
    }

    @GetMapping("/status")
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

    private String getStoredProvider(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> providerCookie = Arrays.stream(cookies)
                .filter(cookie -> OAUTH_PROVIDER_COOKIE.equals(cookie.getName()))
                .findFirst();
            
            if (providerCookie.isPresent()) {
                return providerCookie.get().getValue();
            }
        }
        return null;
    }

    private void clearOAuthCookies(HttpServletResponse response) {
        // Clear state cookie
        Cookie stateCookie = new Cookie(OAUTH_STATE_COOKIE, "");
        stateCookie.setHttpOnly(true);
        stateCookie.setSecure(false); // Set to true in production with HTTPS
        stateCookie.setPath("/");
        stateCookie.setMaxAge(0);
        response.addCookie(stateCookie);
        
        // Clear provider cookie
        Cookie providerCookie = new Cookie(OAUTH_PROVIDER_COOKIE, "");
        providerCookie.setHttpOnly(true);
        providerCookie.setSecure(false);
        providerCookie.setPath("/");
        providerCookie.setMaxAge(0);
        response.addCookie(providerCookie);
    }

    @GetMapping("/success")
    @ResponseBody
    @Operation(
        summary = "OAuth2 authentication success page",
        description = "Shows successful OAuth2 authentication with JWT token"
    )
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
            .body("<h1>OAuth2 Authentication</h1>" +
                  "<p>Waiting for authentication result...</p>");
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
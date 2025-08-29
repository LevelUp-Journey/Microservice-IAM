package com.levelupjourney.microserviceiam.IAM.infrastructure.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.tokens.TokenService;
import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.Account;
import com.levelupjourney.microserviceiam.IAM.domain.model.commands.OAuth2SignInCommand;
import com.levelupjourney.microserviceiam.IAM.domain.model.valueobjects.*;
import com.levelupjourney.microserviceiam.IAM.domain.services.AccountCommandService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AccountCommandService accountCommandService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationSuccessHandler(AccountCommandService accountCommandService, 
                                            TokenService tokenService,
                                            ObjectMapper objectMapper) {
        this.accountCommandService = accountCommandService;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        try {
            // Determine provider from OAuth2User attributes
            String provider = determineProvider(oauth2User);
            String providerUserId = extractProviderUserId(oauth2User, provider);
            String email = extractEmail(oauth2User, provider);
            String name = extractName(oauth2User, provider);
            
            // Create OAuth2SignInCommand
            AuthProvider authProvider = switch (provider.toLowerCase()) {
                case "google" -> AuthProvider.google();
                case "github" -> AuthProvider.github();
                default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
            };
            
            OAuth2SignInCommand command = new OAuth2SignInCommand(
                authProvider,
                providerUserId,
                new EmailAddress(email),
                name,
                oauth2User.getAttributes(),
                Set.of(Role.getDefaultRole())
            );
            
            // Handle OAuth2 sign in
            var accountOpt = accountCommandService.handle(command);
            
            if (accountOpt.isEmpty()) {
                response.sendRedirect("/oauth2/error?message=OAuth2 sign in failed");
                return;
            }
            
            Account account = accountOpt.get();
            String accessToken = tokenService.generateToken(account);
            String refreshToken = tokenService.generateRefreshToken(account);
            
            // Redirect to success endpoint with tokens as query parameters
            String redirectUrl = String.format("/oauth2/success?access_token=%s&refresh_token=%s&provider=%s", 
                accessToken, refreshToken, provider);
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            response.sendRedirect("/oauth2/error?message=" + e.getMessage());
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, String error, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
    
    private String determineProvider(OAuth2User oauth2User) {
        // Check for Google-specific attributes
        if (oauth2User.getAttributes().containsKey("sub")) {
            return "google";
        }
        // Check for GitHub-specific attributes
        if (oauth2User.getAttributes().containsKey("login")) {
            return "github";
        }
        // Default fallback
        return "unknown";
    }
    
    private String extractEmail(OAuth2User oauth2User, String provider) {
        String email = switch (provider.toLowerCase()) {
            case "google" -> oauth2User.getAttribute("email");
            case "github" -> {
                String primaryEmail = oauth2User.getAttribute("email");
                if (primaryEmail == null || primaryEmail.trim().isEmpty()) {
                    // Fallback: use login@github.com if no email is available
                    String login = oauth2User.getAttribute("login");
                    yield login != null ? login + "@github.com" : null;
                }
                yield primaryEmail;
            }
            default -> oauth2User.getAttribute("email");
        };
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        return email;
    }
    
    private String extractName(OAuth2User oauth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oauth2User.getAttribute("name");
            case "github" -> {
                String name = oauth2User.getAttribute("name");
                yield name != null ? name : oauth2User.getAttribute("login");
            }
            default -> oauth2User.getAttribute("name");
        };
    }
    
    private String extractProviderUserId(OAuth2User oauth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oauth2User.getAttribute("sub");
            case "github" -> oauth2User.getAttribute("id").toString();
            default -> oauth2User.getAttribute("id").toString();
        };
    }
}
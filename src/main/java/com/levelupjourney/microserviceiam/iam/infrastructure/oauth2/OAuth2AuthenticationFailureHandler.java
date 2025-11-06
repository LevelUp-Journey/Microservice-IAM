package com.levelupjourney.microserviceiam.iam.infrastructure.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.authorized-redirect-uris}")
    private String authorizedRedirectUris;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException {
        System.err.println("OAuth2 Authentication Failed!");
        System.err.println("Exception: " + exception.getClass().getName());
        System.err.println("Message: " + exception.getMessage());
        System.err.println("Request URI: " + request.getRequestURI());
        System.err.println("Request URL: " + request.getRequestURL());
        if (exception.getCause() != null) {
            System.err.println("Cause: " + exception.getCause().getMessage());
        }
        
        String redirectUrl = getAuthorizedRedirectUri() + "?error=authentication_failed&message=" + exception.getMessage();
        response.sendRedirect(redirectUrl);
    }

    private String getAuthorizedRedirectUri() {
        String[] uris = authorizedRedirectUris.split(",");
        return uris[0].trim();
    }
}
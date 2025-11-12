package com.levelupjourney.microserviceiam.iam.infrastructure.oauth2;

import com.levelupjourney.microserviceiam.iam.infrastructure.configuration.FrontendConfigurationProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final FrontendConfigurationProperties frontendProperties;

    public OAuth2AuthenticationFailureHandler(FrontendConfigurationProperties frontendProperties) {
        this.frontendProperties = frontendProperties;
    }

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
        
        String redirectUrl = frontendProperties.getPrimaryRedirectUri() + "?error=authentication_failed&message=" + exception.getMessage();
        response.sendRedirect(redirectUrl);
    }
}

package com.levelupjourney.microserviceiam.shared.interfaces.rest.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class to set up CORS (Cross-Origin Resource Sharing) for the application.
 * This allows the application to handle requests from different origins.
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    /**
     * Configures CORS settings for the application.
     *
     * @param registry the CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * Configures view controllers to redirect root path to Swagger UI.
     *
     * @param registry the view controller registry to configure
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/swagger-ui.html");
    }
}

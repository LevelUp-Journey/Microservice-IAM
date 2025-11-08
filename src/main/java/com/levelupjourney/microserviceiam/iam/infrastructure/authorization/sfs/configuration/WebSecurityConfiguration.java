package com.levelupjourney.microserviceiam.iam.infrastructure.authorization.sfs.configuration;

import com.levelupjourney.microserviceiam.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.levelupjourney.microserviceiam.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.levelupjourney.microserviceiam.iam.infrastructure.oauth2.OAuth2AuthenticationSuccessHandler;
import com.levelupjourney.microserviceiam.iam.infrastructure.oauth2.OAuth2AuthenticationFailureHandler;
import com.levelupjourney.microserviceiam.iam.infrastructure.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.levelupjourney.microserviceiam.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Web Security Configuration.
 * <p>
 * This class is responsible for configuring the web security.
 * It enables the method security and configures the security filter chain.
 * It includes the authentication manager, the authentication provider, the password encoder and the authentication entry point.
 * </p>
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private final UserDetailsService userDetailsService;

    private final BearerTokenService tokenService;

    private final BCryptHashingService hashingService;

    private final AuthenticationEntryPoint unauthorizedRequestHandler;

    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    /**
     * This method creates the Bearer Authorization Request Filter.
     * @return The Bearer Authorization Request Filter
     * @see BearerAuthorizationRequestFilter
     */
    @Bean
    public BearerAuthorizationRequestFilter authorizationRequestFilter() {
        return new BearerAuthorizationRequestFilter(tokenService, userDetailsService);
    }

    /**
     * This method creates the authentication manager.
     * @param authenticationConfiguration The {@link AuthenticationConfiguration} object with the authentication configuration
     * @return The {@link AuthenticationManager} instance from the authentication configuration
     *
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * This method creates the authentication provider.
     * @return The {@link DaoAuthenticationProvider} authentication provider with the user details service and the password encoder
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(hashingService);
        return authenticationProvider;
    }

    /**
     * This method creates the password encoder.
     * @return The {@link PasswordEncoder} instance with the hashing service
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return hashingService;
    }

    /**
     * This method creates the security filter chain.
     * It also configures the http security.
     *
     * @param http The {@link HttpSecurity} object to configure with the security filter chain
     * @return The {@link SecurityFilterChain} instance with the application http security configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(configurer -> configurer.configurationSource(_ -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(List.of("*"));
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            cors.setAllowedHeaders(List.of("*"));
            return cors;
        }));
        http.csrf(csrfConfigurer -> csrfConfigurer.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedRequestHandler))
                .sessionManagement( customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(
                                "/",
                                "/api/v1/authentication/**",
                                "/oauth2/**",
                                "/api/v1/iam/health/**",
                                "/login/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorizationEndpoint ->
                                authorizationEndpoint
                                        .baseUri("/login/oauth2/authorization")
                                        .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
                        .successHandler(oauth2AuthenticationSuccessHandler)
                        .failureHandler(oauth2AuthenticationFailureHandler));
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authorizationRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    /**
     * This is the constructor of the class.
     * @param userDetailsService The user details service
     * @param tokenService The token service
     * @param hashingService The hashing service
     * @param authenticationEntryPoint The authentication entry point
     * @param oauth2AuthenticationSuccessHandler The OAuth2 success handler
     * @param oauth2AuthenticationFailureHandler The OAuth2 failure handler
     */
    public WebSecurityConfiguration(@Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService, 
                                   BearerTokenService tokenService, 
                                   BCryptHashingService hashingService, 
                                   AuthenticationEntryPoint authenticationEntryPoint,
                                   OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler,
                                   OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler,
                                   HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.hashingService = hashingService;
        this.unauthorizedRequestHandler = authenticationEntryPoint;
        this.oauth2AuthenticationSuccessHandler = oauth2AuthenticationSuccessHandler;
        this.oauth2AuthenticationFailureHandler = oauth2AuthenticationFailureHandler;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }
}

package com.bluewave.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * =========================================================================================
 *                         DOWNSTREAM MICROSERVICE SECURITY CONFIGURATION
 * =========================================================================================
 *
 * HOW IT WORKS IN MICROSERVICES ARCHITECTURE:
 * -----------------------------------------------------------------------------------------
 * 1. [PRODUCTION / GATEWAY MODE - RECOMMENDED]:
 *    - All external client requests MUST go through the API Gateway (port 8900).
 *    - API Gateway verifies JWT + checks Redis session key (user:<username>:<sessionId>).
 *    - API Gateway injects trusted headers:
 *        * X-User-Id: <UUID>
 *        * X-User-Name: <username>
 *        * X-User-Roles: ROLE_CUSTOMER,ROLE_PROVIDER
 *    - GatewayHeaderSecurityFilter reads these headers and creates the Spring SecurityContext.
 *
 * -----------------------------------------------------------------------------------------
 * 2. [STANDALONE / DIRECT SERVICE TESTING IN POSTMAN (ISOLATION DEV MODE)]:
 *    - If you want to test a SINGLE service (e.g. Booking Service at port 8700) directly in
 *      Postman without running API Gateway or Auth Service:
 *      * Send request directly to http://localhost:8700/api/v1/bookings
 *      * In Postman Headers tab, manually add:
 *          Key: X-User-Id     Value: 123e4567-e89b-12d3-a456-426614174000
 *          Key: X-User-Name   Value: testuser
 *          Key: X-User-Roles  Value: ROLE_CUSTOMER (or ROLE_PROVIDER, ROLE_ADMIN)
 *      * GatewayHeaderSecurityFilter will read these headers and authenticate your request!
 *
 * -----------------------------------------------------------------------------------------
 * 3. [TEMPORARILY BYPASS SECURITY FOR OFFLINE DEBUGGING]:
 *    - Set property in application.properties:
 *        bookforge.security.downstream.enabled=false
 *      OR comment out .anyRequest().authenticated() below to permit all requests.
 * =========================================================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bookforge.security.downstream.enabled", havingValue = "true", matchIfMissing = true)
public class DownStreamSecurityConfig {

    private final GatewayHeaderSecurityFilter headerFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST microservices
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session management (no HTTP session stored in memory)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules:
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (Swagger, OpenAPI, Actuator health checks)
                        .requestMatchers(
                                "/actuator/**"
                        ).permitAll()

                        // =======================================================================
                        // [SECURITY ENFORCEMENT]:
                        // Every request must be authenticated via GatewayHeaderSecurityFilter
                        // (either injected by Gateway OR supplied in Postman headers during dev).
                        // To disable security for quick local debug: change .authenticated() to .permitAll()
                        // =======================================================================
                        .anyRequest().authenticated()
                )

                // Register Gateway Header Filter before Spring's UsernamePasswordAuthenticationFilter
                .addFilterBefore(headerFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

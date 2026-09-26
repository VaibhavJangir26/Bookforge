package com.bluewave.utils;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * =========================================================================================
 *                   INDUSTRY STANDARD FEIGN SECURITY INTERCEPTOR
 * =========================================================================================
 *
 * Automatically propagates authentication and identity headers across inter-service
 * OpenFeign HTTP calls:
 *
 * 1. Authorization Header : "Bearer <jwt-token>"
 * 2. Identity Headers     : "X-User-Id", "X-User-Name", "X-User-Roles"
 *
 * Flow:
 * Client Request -> Gateway -> Service A (reads headers) -> Feign Interceptor -> Service B (authenticates)
 * =========================================================================================
 */
@Slf4j
@Configuration
public class FeignSecurityConfig {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 1. Forward Authorization Bearer JWT Token
                String authHeader = request.getHeader(AUTHORIZATION_HEADER);
                if (authHeader != null && !authHeader.isBlank()) {
                    template.header(AUTHORIZATION_HEADER, authHeader);
                }

                // 2. Forward X-User-Id
                String userId = request.getHeader(GatewayHeaderSecurityFilter.HEADER_USER_ID);
                if (userId != null && !userId.isBlank()) {
                    template.header(GatewayHeaderSecurityFilter.HEADER_USER_ID, userId);
                }

                // 3. Forward X-User-Name
                String username = request.getHeader(GatewayHeaderSecurityFilter.HEADER_USER_NAME);
                if (username != null && !username.isBlank()) {
                    template.header(GatewayHeaderSecurityFilter.HEADER_USER_NAME, username);
                }

                // 4. Forward X-User-Roles
                String userRoles = request.getHeader(GatewayHeaderSecurityFilter.HEADER_USER_ROLES);
                if (userRoles != null && !userRoles.isBlank()) {
                    template.header(GatewayHeaderSecurityFilter.HEADER_USER_ROLES, userRoles);
                }

            } else {
                // Fallback: Check Spring SecurityContextHolder if executed outside standard servlet thread
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                    if (principal.getUserId() != null) {
                        template.header(GatewayHeaderSecurityFilter.HEADER_USER_ID, principal.getUserId());
                    }
                    if (principal.getUsername() != null) {
                        template.header(GatewayHeaderSecurityFilter.HEADER_USER_NAME, principal.getUsername());
                    }
                    if (principal.getRoles() != null && !principal.getRoles().isEmpty()) {
                        template.header(GatewayHeaderSecurityFilter.HEADER_USER_ROLES, String.join(",", principal.getRoles()));
                    }
                }
            }
        };
    }
}

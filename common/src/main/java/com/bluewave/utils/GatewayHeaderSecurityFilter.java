package com.bluewave.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * =========================================================================================
 *                         GATEWAY HEADER SECURITY FILTER
 * =========================================================================================
 *
 * This filter extracts trusted user identity headers and populates Spring's SecurityContext.
 *
 * SUPPORTED HEADERS:
 *   - X-User-Id    : The unique database UUID of the authenticated user
 *   - X-User-Name  : The username or email of the user
 *   - X-User-Roles : Comma-delimited roles (e.g. ROLE_CUSTOMER,ROLE_PROVIDER,ROLE_ADMIN)
 *   - X-User-Role  : Single role fallback (e.g. ROLE_CUSTOMER)
 *
 * HOW TO USE DURING DIRECT POSTMAN TESTING (WITHOUT GATEWAY):
 *   When testing a single service directly (e.g. http://localhost:8700), simply add these
 *   headers in Postman's Headers tab:
 *     X-User-Id: <test-user-id>
 *     X-User-Name: <test-username>
 *     X-User-Roles: ROLE_CUSTOMER,ROLE_PROVIDER
 * =========================================================================================
 */
@Component
public class GatewayHeaderSecurityFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USER_NAME);
        String rolesHeader = request.getHeader(HEADER_USER_ROLES);
        if (rolesHeader == null || rolesHeader.isBlank()) {
            rolesHeader = request.getHeader(HEADER_USER_ROLE);
        }

        if (username != null && !username.isBlank()) {
            Set<String> roleSet = Collections.emptySet();
            List<SimpleGrantedAuthority> authorities = Collections.emptyList();

            if (rolesHeader != null && !rolesHeader.isBlank()) {
                roleSet = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .filter(r -> !r.isEmpty())
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .collect(Collectors.toSet());

                authorities = roleSet.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            // Create UserPrincipal POJO
            UserPrincipal principal = UserPrincipal.builder()
                    .userId(userId)
                    .username(username)
                    .roles(roleSet)
                    .build();

            // Set Spring Security Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

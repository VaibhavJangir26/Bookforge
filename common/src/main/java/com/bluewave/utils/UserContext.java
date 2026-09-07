package com.bluewave.utils;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * USER CONTEXT UTILITY (Zero-DB Microservice Identity Access)
 * ============================================================================
 * Downstream microservices (catalog, booking, payment) use this utility
 * to retrieve the authenticated caller's identity without hitting the database.
 *
 * Identity is populated by GatewayHeaderSecurityFilter from trusted headers:
 * - X-User-Id
 * - X-User-Name
 * - X-User-Roles
 *
 * Example Usage in Service or Controller:
 *   String userId = UserContext.getUserId();
 *   String username = UserContext.getUsername();
 *   boolean isProvider = UserContext.hasRole("ROLE_PROVIDER");
 * ============================================================================
 */
@Component
public class UserContext {

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static UserPrincipal getUserPrincipal() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadCredentialsException("No authenticated user found in SecurityContext");
        }
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return UserPrincipal.builder()
                .username(auth.getName())
                .roles(getUserRoles())
                .build();
    }

    public static String getUserId() {
        UserPrincipal principal = getUserPrincipal();
        return principal != null ? principal.getUserId() : null;
    }

    public static String getUsername() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadCredentialsException("No authenticated user found in SecurityContext");
        }
        return auth.getName();
    }

    public static Set<String> getUserRoles() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptySet();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static boolean hasRole(String roleName) {
        if (roleName == null) {
            return false;
        }
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String expectedRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(authority -> authority.equals(expectedRole));
    }
}

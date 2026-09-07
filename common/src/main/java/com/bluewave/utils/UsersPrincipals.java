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

@Component
public class UsersPrincipals {

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadCredentialsException("No authenticated user found in SecurityContext");
        }
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return UserPrincipal.builder()
                .username(auth.getName())
                .roles(getCurrentUserRoles())
                .build();
    }

    public static String getCurrentUserId() {
        UserPrincipal principal = getCurrentUserPrincipal();
        return principal != null ? principal.getUserId() : null;
    }

    public static String getCurrentUsername() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadCredentialsException("No authenticated user found in SecurityContext");
        }
        return auth.getName();
    }

    public static Set<String> getCurrentUserRoles() {
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

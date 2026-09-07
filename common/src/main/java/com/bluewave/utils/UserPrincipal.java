package com.bluewave.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Serializable {

    private String userId;
    private String username;
    @Builder.Default
    private Set<String> roles = Collections.emptySet();

    public boolean hasRole(String role) {
        if (roles == null || role == null) {
            return false;
        }
        String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return roles.contains(normalized) || roles.contains(role);
    }
}

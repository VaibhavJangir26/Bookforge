package com.bluewave.utils;

import com.bluewave.entity.Users;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

public record CustomServiceDetails(Users users) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (users == null || users.getRoles() == null) {
            return java.util.Collections.emptyList();
        }
        return users.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getAppRole().name()))
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable String getPassword() {
        return users.getPassword();
    }

    @Override
    public String getUsername() {
        return users.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

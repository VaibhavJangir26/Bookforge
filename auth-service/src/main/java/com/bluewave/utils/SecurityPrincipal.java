package com.bluewave.utils;

import com.bluewave.entity.Users;
import com.bluewave.repo.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SecurityPrincipal {

    private final UsersRepo usersRepo;

    public Users getUserByUsername(String username){
        return usersRepo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("user not found with this username"));
    }

    public String getCurrentLoginUsername(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null|| !authentication.isAuthenticated()){
            throw new BadCredentialsException("no authenticated user found");
        }
        return authentication.getName();
    }

    public Users getCurrentLoginUserEntity(){
        String username= getCurrentLoginUsername();
        return usersRepo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("user not found with this username"));
    }

    public Set<String> getUserRole(Users users){
        if(users==null|| users.getRoles()==null){
            return Collections.emptySet();
        }
        return users.getRoles().stream().map(r->r.getAppRole().name()).collect(Collectors.toSet());
    }

    public Set<String> getCurrentLoginUserRole(){
        return getUserRole(getCurrentLoginUserEntity());
    }

    public boolean hasRole(String roleName){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null|| !authentication.isAuthenticated()){
            return false;
        }
        String expectedRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .anyMatch(authority -> authority.equals(expectedRole));
    }


}

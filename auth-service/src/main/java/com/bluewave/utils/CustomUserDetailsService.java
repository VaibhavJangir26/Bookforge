package com.bluewave.utils;

import com.bluewave.entity.Users;
import com.bluewave.repo.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepo usersRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users=usersRepo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("user not found with username"));
        return new CustomServiceDetails(users);
    }
}

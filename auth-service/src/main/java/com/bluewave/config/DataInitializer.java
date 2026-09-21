package com.bluewave.config;

import com.bluewave.constants.AppRole;
import com.bluewave.entity.Roles;
import com.bluewave.entity.Users;
import com.bluewave.repo.RoleRepo;
import com.bluewave.repo.UsersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"dev", "local", "default"}) // Never runs in prod by default
@ConditionalOnProperty(name = "app.database.seed-enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final UsersRepo usersRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking initial system roles and testing accounts...");

        // 1. Ensure Roles exist (Idempotent)
        Roles adminRole = getOrCreateRole(AppRole.ROLE_ADMIN);
        Roles providerRole = getOrCreateRole(AppRole.ROLE_PROVIDER);
        Roles customerRole = getOrCreateRole(AppRole.ROLE_CUSTOMER);

        // 2. Seed Test Users (Deterministic: Won't throw errors or duplicate)
        seedUserIfAbsent("admin", "admin@bluewave.com", "admin@123", Set.of(adminRole));
        seedUserIfAbsent("ram123", "provider@bluewave.com", "ram@123", Set.of(providerRole, customerRole));
        seedUserIfAbsent("shyam123", "customer@bluewave.com", "shyam@123", Set.of(customerRole));

        log.info("Database test seed verification complete.");
    }

    private Roles getOrCreateRole(AppRole appRole) {
        return roleRepo.findByAppRole(appRole)
                .orElseGet(() -> {
                    Roles newRole = new Roles(null, appRole, new HashSet<>());
                    Roles saved = roleRepo.save(newRole);
                    log.info("Initialized missing role: [{}]", appRole.name());
                    return saved;
                });
    }

    private void seedUserIfAbsent(String username, String email, String rawPassword, Set<Roles> roles) {
        if (usersRepo.findByUsername(username).isPresent()) {
            return; // Silently skip if user exists
        }

        if (usersRepo.findByEmail(email).isPresent()) {
            return; // Silently skip if email exists
        }

        Users user = new Users();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(new HashSet<>(roles));

        usersRepo.save(user);
        log.info("Seeded test account -> username: [{}], roles: {}", username, roles.stream().map(r -> r.getAppRole().name()).toList());
    }
}
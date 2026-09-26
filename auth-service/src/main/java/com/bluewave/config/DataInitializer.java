package com.bluewave.config;

import com.bluewave.constants.AppRole;
import com.bluewave.entity.Address;
import com.bluewave.entity.Profile;
import com.bluewave.entity.Roles;
import com.bluewave.entity.Users;
import com.bluewave.repo.RoleRepo;
import com.bluewave.repo.UsersRepo;
import com.bluewave.utils.ProviderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
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

        // 2. Seed Test Users with Profiles (Deterministic: Won't throw errors or duplicate)
        Profile adminProfile = createProfile("System Administrator", "9999999999", "BookForge Platform Inc", "ADMIN-GST-001", ProviderStatus.APPROVED,
                new Address("Mumbai", "Maharashtra", 400001, "Level 24, BookForge Tower"));
        seedUserIfAbsent("admin", "admin@bluewave.com", "admin@123", Set.of(adminRole), adminProfile);

        Profile providerProfile = createProfile("Ram Sharma", "9876543210", "Ram Acoustic Studios LLC", "27ABCDE1234F1Z5", ProviderStatus.APPROVED,
                new Address("Mumbai", "Maharashtra", 400001, "742 Studio Sound Way, Suite 400"));
        seedUserIfAbsent("ram123", "provider@bluewave.com", "ram@123", Set.of(providerRole, customerRole), providerProfile);

        Profile customerProfile1 = createProfile("Shyam Verma", "9123456780", null, null, ProviderStatus.NONE,
                new Address("Mumbai", "Maharashtra", 400050, "12 Creative Avenue, Bandra"));
        seedUserIfAbsent("shyam123", "customer@bluewave.com", "shyam@123", Set.of(customerRole), customerProfile1);

        // Additional Customer Seeded
        Profile customerProfile2 = createProfile("Rahul Kumar", "9700000000", null, null, ProviderStatus.NONE,
                new Address("Jaipur", "Rajasthan", 302001, "45 MG Road, Civil Lines"));
        seedUserIfAbsent("rahul123", "rahul@bluewave.com", "rahul@123", Set.of(customerRole), customerProfile2);

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

    private Profile createProfile(String fullName, String mobileNo, String bizName, String taxId, ProviderStatus status, Address address) {
        Profile p = new Profile();
        p.setFullName(fullName);
        p.setMobileNo(mobileNo);
        p.setBusinessName(bizName);
        p.setTaxOrGstNumber(taxId);
        p.setProviderStatus(status);
        p.setAddress(address);
        return p;
    }

    private void seedUserIfAbsent(String username, String email, String rawPassword, Set<Roles> roles, Profile profile) {
        Optional<Users> existingOpt = usersRepo.findByUsername(username);
        if (existingOpt.isPresent()) {
            Users existing = existingOpt.get();
            if (existing.getProfile() == null) {
                existing.setProfile(profile);
                profile.setUsers(existing);
                usersRepo.save(existing);
                log.info("Attached missing profile to existing user [{}]", username);
            }
            return;
        }

        if (usersRepo.findByEmail(email).isPresent()) {
            return;
        }

        Users user = new Users();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(new HashSet<>(roles));
        user.setProfile(profile);
        profile.setUsers(user);

        usersRepo.save(user);
        log.info("Seeded test account -> username: [{}], roles: {}", username, roles.stream().map(r -> r.getAppRole().name()).toList());
    }
}
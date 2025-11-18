package com.analytics.config;

import com.analytics.domain.entity.Role;
import com.analytics.domain.entity.User;
import com.analytics.domain.repository.RoleRepository;
import com.analytics.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Database Initializer
 * Creates default roles and admin user
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing database...");

        // Create default roles
        createRoleIfNotExists(Role.RoleType.ROLE_USER, "Default user role");
        createRoleIfNotExists(Role.RoleType.ROLE_MANAGER, "Manager role with additional permissions");
        createRoleIfNotExists(Role.RoleType.ROLE_ADMIN, "Administrator role with full access");

        // Create default admin user if not exists
        createDefaultAdminUser();

        log.info("Database initialization completed");
    }

    private void createRoleIfNotExists(Role.RoleType roleType, String description) {
        if (!roleRepository.existsByName(roleType)) {
            Role role = Role.builder()
                    .name(roleType)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {}", roleType);
        }
    }

    private void createDefaultAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(Role.RoleType.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .username("admin")
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@analytics.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            log.info("Created default admin user (username: admin, password: admin123)");
        }
    }
}

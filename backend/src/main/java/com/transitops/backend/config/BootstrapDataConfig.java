package com.transitops.backend.config;

import java.util.EnumSet;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.auth.model.Role;
import com.transitops.backend.auth.model.RoleName;
import com.transitops.backend.auth.model.User;
import com.transitops.backend.auth.repository.RoleRepository;
import com.transitops.backend.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BootstrapDataConfig {
    private final AppProperties properties;

    @Bean
    CommandLineRunner bootstrapData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> initialize(roleRepository, userRepository, passwordEncoder);
    }

    @Transactional
    void initialize(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                return roleRepository.save(role);
            });
        }

        String email = properties.getBootstrapAdmin().getEmail().trim().toLowerCase();
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            User admin = new User();
            admin.setName(properties.getBootstrapAdmin().getName());
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(properties.getBootstrapAdmin().getPassword()));
            admin.setEnabled(true);
            admin.setRoles(EnumSet.allOf(RoleName.class).stream()
                    .map(name -> roleRepository.findByName(name).orElseThrow())
                    .collect(java.util.stream.Collectors.toSet()));
            userRepository.save(admin);
        }
    }
}

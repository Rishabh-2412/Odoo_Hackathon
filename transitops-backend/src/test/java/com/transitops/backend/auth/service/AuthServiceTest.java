package com.transitops.backend.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.transitops.backend.auth.dto.SignupRequest;
import com.transitops.backend.auth.dto.UserSummary;
import com.transitops.backend.auth.model.Role;
import com.transitops.backend.auth.model.RoleName;
import com.transitops.backend.auth.model.User;
import com.transitops.backend.auth.repository.RoleRepository;
import com.transitops.backend.auth.repository.UserRepository;
import com.transitops.backend.common.exception.DuplicateResourceException;
import com.transitops.backend.config.AppProperties;
import com.transitops.backend.security.CurrentUserService;
import com.transitops.backend.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private CurrentUserService currentUserService;
    @Mock private AppProperties properties;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                userRepository,
                roleRepository,
                passwordEncoder,
                jwtService,
                refreshTokenService,
                currentUserService,
                properties);
    }

    @Test
    void publicSignupAssignsOnlyDriverRoleAndHashesPassword() {
        Role driverRole = new Role();
        driverRole.setId(5L);
        driverRole.setName(RoleName.DRIVER);

        when(userRepository.existsByEmailIgnoreCase("driver@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.DRIVER)).thenReturn(Optional.of(driverRole));
        when(passwordEncoder.encode("Driver@123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        UserSummary result = authService.signup(
                new SignupRequest("  Test Driver  ", "  DRIVER@EXAMPLE.COM  ", "Driver@123"));

        assertEquals(10L, result.id());
        assertEquals("Test Driver", result.name());
        assertEquals("driver@example.com", result.email());
        assertTrue(result.enabled());
        assertEquals(1, result.roles().size());
        assertTrue(result.roles().contains(RoleName.DRIVER));
        verify(passwordEncoder).encode("Driver@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void publicSignupRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("driver@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.signup(
                new SignupRequest("Test Driver", "driver@example.com", "Driver@123")));
    }
}

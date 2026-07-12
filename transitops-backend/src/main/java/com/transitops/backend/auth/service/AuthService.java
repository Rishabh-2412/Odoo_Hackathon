package com.transitops.backend.auth.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.auth.dto.AuthResponse;
import com.transitops.backend.auth.dto.LoginRequest;
import com.transitops.backend.auth.dto.LogoutRequest;
import com.transitops.backend.auth.dto.RefreshTokenRequest;
import com.transitops.backend.auth.dto.SignupRequest;
import com.transitops.backend.auth.dto.UserSummary;
import com.transitops.backend.auth.model.Role;
import com.transitops.backend.auth.model.RoleName;
import com.transitops.backend.auth.model.User;
import com.transitops.backend.auth.repository.RoleRepository;
import com.transitops.backend.auth.repository.UserRepository;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.exception.DuplicateResourceException;
import com.transitops.backend.common.exception.ResourceNotFoundException;
import com.transitops.backend.config.AppProperties;
import com.transitops.backend.security.CurrentUserService;
import com.transitops.backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CurrentUserService currentUserService;
    private final AppProperties properties;

    @Transactional
    public UserSummary signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A user with this email already exists");
        }

        Role driverRole = roleRepository.findByName(RoleName.DRIVER)
                .orElseThrow(() -> new BusinessRuleException(
                        "ROLE_NOT_CONFIGURED", "The DRIVER role is not configured"));

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(driverRole)));

        return UserMapper.toSummary(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        } catch (AuthenticationException ex) {
            throw new BusinessRuleException("INVALID_CREDENTIALS", "Email or password is incorrect");
        }
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isEnabled()) {
            throw new BusinessRuleException("USER_DISABLED", "This user account is disabled");
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        RefreshTokenService.IssuedRefreshToken refresh = refreshTokenService.create(user, request.deviceInfo());
        return buildResponse(user, refresh.rawToken());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.RotatedRefreshToken rotated =
                refreshTokenService.rotate(request.refreshToken(), request.deviceInfo());
        return buildResponse(rotated.user(), rotated.rawToken());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        User user = currentUserService.requireCurrentUser();
        if (request.allDevices()) {
            refreshTokenService.revokeAll(user);
        } else {
            refreshTokenService.revoke(request.refreshToken(), user);
        }
    }

    @Transactional(readOnly = true)
    public UserSummary me() {
        return UserMapper.toSummary(currentUserService.requireCurrentUser());
    }

    private AuthResponse buildResponse(User user, String refreshToken) {
        return new AuthResponse(
                "Bearer",
                jwtService.createAccessToken(user),
                properties.getJwt().getAccessTokenTtl().toSeconds(),
                refreshToken,
                properties.getJwt().getRefreshTokenTtl().toSeconds(),
                UserMapper.toSummary(user));
    }
}

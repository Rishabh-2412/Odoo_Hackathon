package com.transitops.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.dto.auth.AuthTokenResponse;
import com.transitops.backend.dto.auth.AuthUserResponse;
import com.transitops.backend.dto.auth.ChangePasswordRequest;
import com.transitops.backend.dto.auth.LoginRequest;
import com.transitops.backend.dto.auth.LogoutRequest;
import com.transitops.backend.dto.auth.MessageResponse;
import com.transitops.backend.dto.auth.RefreshTokenRequest;
import com.transitops.backend.entity.User;
import com.transitops.backend.exception.ApiException;
import com.transitops.backend.repository.UserRepository;
import com.transitops.backend.security.DatabaseUserDetailsService;
import com.transitops.backend.security.JwtService;
import com.transitops.backend.security.LoginAttemptService;
import com.transitops.backend.security.RefreshTokenService;
import com.transitops.backend.security.TransitOpsUserPrincipal;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DatabaseUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            DatabaseUserDetailsService userDetailsService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            LoginAttemptService loginAttemptService,
            PasswordEncoder passwordEncoder) {

        this.authenticationManager =
            authenticationManager;

        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthTokenResponse login(
            LoginRequest request,
            String clientIp) {

        String normalizedEmail =
            request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Authentication authentication;

        try {
            authentication =
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.password()
                    )
                );
        }
        catch (BadCredentialsException exception) {

            loginAttemptService.recordFailure(
                normalizedEmail
            );

            throw invalidCredentials();
        }
        catch (AuthenticationException exception) {
            throw invalidCredentials();
        }

        TransitOpsUserPrincipal principal =
            requirePrincipal(
                authentication.getPrincipal()
            );

        loginAttemptService.recordSuccess(
            principal.getUserId()
        );

        User user = findUser(principal.getUserId());

        RefreshTokenService.IssuedRefreshToken
            refreshToken =
                refreshTokenService.issue(
                    user,
                    clientIp
                );

        return createTokenResponse(
            principal,
            refreshToken.rawToken(),
            refreshToken.expiresAt()
        );
    }

    public AuthTokenResponse refresh(
            RefreshTokenRequest request,
            String clientIp) {

        RefreshTokenService.RotatedRefreshToken
            rotatedToken =
                refreshTokenService.rotate(
                    request.refreshToken(),
                    clientIp
                );

        TransitOpsUserPrincipal principal =
            loadPrincipal(
                rotatedToken.user().getEmail()
            );

        if (!principal.isEnabled()
                || !principal.isAccountNonLocked()) {

            throw new ApiException(
                HttpStatus.UNAUTHORIZED,
                "User account is not available"
            );
        }

        return createTokenResponse(
            principal,
            rotatedToken.rawToken(),
            rotatedToken.expiresAt()
        );
    }

    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser(
            Long userId) {

        User user = findUser(userId);

        TransitOpsUserPrincipal principal =
            loadPrincipal(user.getEmail());

        return toUserResponse(principal);
    }

    @Transactional
    public MessageResponse logout(
            Long userId,
            LogoutRequest request) {

        if (request.allDevices()) {

            refreshTokenService
                .revokeAllForUser(userId);

            User user = findUser(userId);

            user.setTokenVersion(
                user.getTokenVersion() + 1
            );

            userRepository.save(user);

            return new MessageResponse(
                "Logged out successfully from all devices"
            );
        }

        refreshTokenService.revoke(
            request.refreshToken()
        );

        return new MessageResponse(
            "Logged out successfully"
        );
    }

    @Transactional
    public MessageResponse changePassword(
            Long userId,
            ChangePasswordRequest request) {

        if (!request.newPassword()
                .equals(request.confirmPassword())) {

            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "New password and confirmation do not match"
            );
        }

        User user = findUser(userId);

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash())) {

            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPasswordHash())) {

            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "New password must be different from the current password"
            );
        }

        user.setPasswordHash(
            passwordEncoder.encode(
                request.newPassword()
            )
        );

        user.setMustChangePassword(false);
        user.setPasswordChangedAt(Instant.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);

        user.setTokenVersion(
            user.getTokenVersion() + 1
        );

        userRepository.save(user);

        refreshTokenService
            .revokeAllForUser(userId);

        return new MessageResponse(
            "Password changed successfully. Please log in again."
        );
    }

    private AuthTokenResponse createTokenResponse(
            TransitOpsUserPrincipal principal,
            String refreshToken,
            Instant refreshTokenExpiresAt) {

        String accessToken =
            jwtService.generateAccessToken(
                principal
            );

        return new AuthTokenResponse(
            "Bearer",
            accessToken,
            jwtService
                .getAccessTokenExpiresInSeconds(),
            refreshToken,
            refreshTokenExpiresAt,
            toUserResponse(principal)
        );
    }

    private AuthUserResponse toUserResponse(
            TransitOpsUserPrincipal principal) {

        List<String> authorities =
            principal.getAuthorities()
                .stream()
                .map(authority ->
                    authority.getAuthority()
                )
                .distinct()
                .sorted()
                .toList();

        List<String> roles =
            authorities.stream()
                .filter(authority ->
                    authority.startsWith("ROLE_")
                )
                .map(authority ->
                    authority.substring(5)
                )
                .toList();

        List<String> permissions =
            authorities.stream()
                .filter(authority ->
                    !authority.startsWith("ROLE_")
                )
                .toList();

        return new AuthUserResponse(
            principal.getUserId(),
            principal.getFullName(),
            principal.getUsername(),
            principal.isEnabled()
                ? "ACTIVE"
                : "INACTIVE",
            principal.isMustChangePassword(),
            roles,
            permissions
        );
    }

    private TransitOpsUserPrincipal loadPrincipal(
            String email) {

        Object userDetails =
            userDetailsService
                .loadUserByUsername(email);

        return requirePrincipal(userDetails);
    }

    private TransitOpsUserPrincipal requirePrincipal(
            Object principal) {

        if (principal
                instanceof TransitOpsUserPrincipal userPrincipal) {

            return userPrincipal;
        }

        throw new ApiException(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed"
        );
    }

    private User findUser(Long userId) {

        return userRepository.findById(userId)
            .orElseThrow(() ->
                new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
                )
            );
    }

    private ApiException invalidCredentials() {

        return new ApiException(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password"
        );
    }
}

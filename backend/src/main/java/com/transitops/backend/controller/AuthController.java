package com.transitops.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.dto.auth.AuthTokenResponse;
import com.transitops.backend.dto.auth.AuthUserResponse;
import com.transitops.backend.dto.auth.ChangePasswordRequest;
import com.transitops.backend.dto.auth.LoginRequest;
import com.transitops.backend.dto.auth.LogoutRequest;
import com.transitops.backend.dto.auth.MessageResponse;
import com.transitops.backend.dto.auth.RefreshTokenRequest;
import com.transitops.backend.exception.ApiException;
import com.transitops.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthTokenResponse login(
            @Valid @RequestBody
            LoginRequest request,
            HttpServletRequest httpRequest) {

        return authService.login(
            request,
            httpRequest.getRemoteAddr()
        );
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(
            @Valid @RequestBody
            RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        return authService.refresh(
            request,
            httpRequest.getRemoteAddr()
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AuthUserResponse me(
            JwtAuthenticationToken authentication) {

        return authService.getCurrentUser(
            extractUserId(authentication)
        );
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public MessageResponse logout(
            @Valid @RequestBody
            LogoutRequest request,
            JwtAuthenticationToken authentication) {

        return authService.logout(
            extractUserId(authentication),
            request
        );
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public MessageResponse changePassword(
            @Valid @RequestBody
            ChangePasswordRequest request,
            JwtAuthenticationToken authentication) {

        return authService.changePassword(
            extractUserId(authentication),
            request
        );
    }

    private Long extractUserId(
            JwtAuthenticationToken authentication) {

        Object value = authentication
            .getToken()
            .getClaims()
            .get("uid");

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            }
            catch (NumberFormatException ignored) {
                // Handled below.
            }
        }

        throw new ApiException(
            HttpStatus.UNAUTHORIZED,
            "Authenticated user ID is missing"
        );
    }
}

package com.transitops.backend.security;

import java.time.Instant;
import java.util.Optional;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.transitops.backend.entity.User;
import com.transitops.backend.enums.UserStatus;
import com.transitops.backend.repository.UserRepository;

public class JwtTokenVersionValidator
        implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_USER_ERROR =
        new OAuth2Error(
            "invalid_token",
            "The token user is invalid or inactive",
            null
        );

    private static final OAuth2Error INVALID_VERSION_ERROR =
        new OAuth2Error(
            "invalid_token",
            "The token has been revoked",
            null
        );

    private final UserRepository userRepository;

    public JwtTokenVersionValidator(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        Long userId = readLongClaim(jwt, "uid");
        Long tokenVersion = readLongClaim(jwt, "ver");

        if (userId == null || tokenVersion == null) {
            return OAuth2TokenValidatorResult.failure(
                INVALID_USER_ERROR
            );
        }

        Optional<User> optionalUser =
            userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return OAuth2TokenValidatorResult.failure(
                INVALID_USER_ERROR
            );
        }

        User user = optionalUser.get();

        boolean temporarilyLocked =
            user.getAccountLockedUntil() != null
            && user.getAccountLockedUntil()
                .isAfter(Instant.now());

        if (user.getStatus() != UserStatus.ACTIVE
                || temporarilyLocked) {

            return OAuth2TokenValidatorResult.failure(
                INVALID_USER_ERROR
            );
        }

        if (user.getTokenVersion()
                != tokenVersion.longValue()) {

            return OAuth2TokenValidatorResult.failure(
                INVALID_VERSION_ERROR
            );
        }

        return OAuth2TokenValidatorResult.success();
    }

    private Long readLongClaim(
            Jwt jwt,
            String claimName) {

        Object claimValue =
            jwt.getClaims().get(claimName);

        if (claimValue instanceof Number number) {
            return number.longValue();
        }

        if (claimValue instanceof String text) {
            try {
                return Long.parseLong(text);
            }
            catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }
}
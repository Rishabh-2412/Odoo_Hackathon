package com.transitops.backend.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtAudienceValidator
        implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_AUDIENCE_ERROR =
        new OAuth2Error(
            "invalid_token",
            "The JWT audience is invalid",
            null
        );

    private final String requiredAudience;

    public JwtAudienceValidator(String requiredAudience) {

        if (requiredAudience == null
                || requiredAudience.isBlank()) {

            throw new IllegalArgumentException(
                "JWT audience must not be empty"
            );
        }

        this.requiredAudience = requiredAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        if (jwt.getAudience() != null
                && jwt.getAudience()
                    .contains(requiredAudience)) {

            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(
            INVALID_AUDIENCE_ERROR
        );
    }
}
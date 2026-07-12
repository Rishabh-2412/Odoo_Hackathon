package com.transitops.backend.dto.auth;

import java.time.Instant;

public record AuthTokenResponse(

    String tokenType,
    String accessToken,
    long accessTokenExpiresInSeconds,
    String refreshToken,
    Instant refreshTokenExpiresAt,
    AuthUserResponse user

) {
}
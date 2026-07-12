package com.transitops.backend.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.entity.RefreshToken;
import com.transitops.backend.entity.User;
import com.transitops.backend.enums.UserStatus;
import com.transitops.backend.exception.ApiException;
import com.transitops.backend.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTE_LENGTH = 64;
    private static final int GENERATION_ATTEMPTS = 5;

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashService tokenHashService;
    private final SecureRandom secureRandom;
    private final Duration refreshTokenDuration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            TokenHashService tokenHashService,
            @Value("${app.jwt.refresh-token-days}")
            long refreshTokenDays) {

        if (refreshTokenDays <= 0) {
            throw new IllegalArgumentException(
                "Refresh-token duration must be positive"
            );
        }

        this.refreshTokenRepository =
            refreshTokenRepository;

        this.tokenHashService = tokenHashService;
        this.secureRandom = new SecureRandom();

        this.refreshTokenDuration =
            Duration.ofDays(refreshTokenDays);
    }

    @Transactional
    public IssuedRefreshToken issue(
            User user,
            String clientIp) {

        GeneratedToken generatedToken =
            generateUniqueToken();

        Instant expiresAt =
            Instant.now().plus(refreshTokenDuration);

        RefreshToken refreshToken =
            new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(generatedToken.hash());
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevoked(false);
        refreshToken.setCreatedByIp(clientIp);

        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(
            generatedToken.rawToken(),
            expiresAt
        );
    }

    @Transactional
    public RotatedRefreshToken rotate(
            String rawRefreshToken,
            String clientIp) {

        String currentHash =
            tokenHashService.sha256(rawRefreshToken);

        RefreshToken existingToken =
            refreshTokenRepository
                .findByTokenHashAndRevokedFalse(
                    currentHash
                )
                .orElseThrow(
                    this::invalidRefreshToken
                );

        Instant now = Instant.now();

        if (!existingToken.getExpiresAt().isAfter(now)) {
            throw invalidRefreshToken();
        }

        User user = existingToken.getUser();

        boolean temporarilyLocked =
            user.getAccountLockedUntil() != null
            && user.getAccountLockedUntil().isAfter(now);

        if (user.getStatus() != UserStatus.ACTIVE
                || temporarilyLocked) {

            throw invalidRefreshToken();
        }

        GeneratedToken newToken =
            generateUniqueToken();

        Instant newExpiry =
            now.plus(refreshTokenDuration);

        existingToken.setRevoked(true);
        existingToken.setRevokedAt(now);

        existingToken.setReplacedByTokenHash(
            newToken.hash()
        );

        RefreshToken replacementToken =
            new RefreshToken();

        replacementToken.setUser(user);
        replacementToken.setTokenHash(newToken.hash());
        replacementToken.setExpiresAt(newExpiry);
        replacementToken.setRevoked(false);
        replacementToken.setCreatedByIp(clientIp);

        refreshTokenRepository.save(existingToken);
        refreshTokenRepository.save(replacementToken);

        return new RotatedRefreshToken(
            user,
            newToken.rawToken(),
            newExpiry
        );
    }

    @Transactional
    public void revoke(String rawRefreshToken) {

        String tokenHash =
            tokenHashService.sha256(rawRefreshToken);

        refreshTokenRepository
            .findByTokenHashAndRevokedFalse(tokenHash)
            .ifPresent(token -> {

                token.setRevoked(true);
                token.setRevokedAt(Instant.now());

                refreshTokenRepository.save(token);
            });
    }

    @Transactional
    public void revokeAllForUser(Long userId) {

        List<RefreshToken> activeTokens =
            refreshTokenRepository
                .findAllByUserIdAndRevokedFalse(userId);

        Instant revokedAt = Instant.now();

        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
            token.setRevokedAt(revokedAt);
        }

        refreshTokenRepository.saveAll(activeTokens);
    }

    private GeneratedToken generateUniqueToken() {

        for (int attempt = 0;
                attempt < GENERATION_ATTEMPTS;
                attempt++) {

            byte[] randomBytes =
                new byte[TOKEN_BYTE_LENGTH];

            secureRandom.nextBytes(randomBytes);

            String rawToken =
                Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);

            String tokenHash =
                tokenHashService.sha256(rawToken);

            if (!refreshTokenRepository
                    .existsByTokenHash(tokenHash)) {

                return new GeneratedToken(
                    rawToken,
                    tokenHash
                );
            }
        }

        throw new IllegalStateException(
            "Unable to generate a unique refresh token"
        );
    }

    private ApiException invalidRefreshToken() {
        return new ApiException(
            HttpStatus.UNAUTHORIZED,
            "Refresh token is invalid, expired or revoked"
        );
    }

    private record GeneratedToken(
        String rawToken,
        String hash
    ) {
    }

    public record IssuedRefreshToken(
        String rawToken,
        Instant expiresAt
    ) {
    }

    public record RotatedRefreshToken(
        User user,
        String rawToken,
        Instant expiresAt
    ) {
    }
}

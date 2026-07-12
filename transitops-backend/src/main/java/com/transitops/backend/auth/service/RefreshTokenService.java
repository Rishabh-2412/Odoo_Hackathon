package com.transitops.backend.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.auth.model.RefreshToken;
import com.transitops.backend.auth.model.User;
import com.transitops.backend.auth.repository.RefreshTokenRepository;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.util.HashingUtils;
import com.transitops.backend.config.AppProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final AppProperties properties;

    @Transactional
    public IssuedRefreshToken create(User user, String deviceInfo) {
        String rawToken = generateToken();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(HashingUtils.sha256(rawToken));
        token.setExpiresAt(Instant.now().plus(properties.getJwt().getRefreshTokenTtl()));
        token.setDeviceInfo(normalizeDeviceInfo(deviceInfo));
        repository.save(token);
        return new IssuedRefreshToken(rawToken, token);
    }

    @Transactional
    public RotatedRefreshToken rotate(String rawToken, String deviceInfo) {
        Instant now = Instant.now();
        RefreshToken current = repository.findByTokenHashForUpdate(HashingUtils.sha256(rawToken))
                .orElseThrow(() -> new BusinessRuleException("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));
        if (!current.isActive(now)) {
            throw new BusinessRuleException("REFRESH_TOKEN_EXPIRED_OR_REVOKED", "Refresh token has expired or was revoked");
        }
        if (!current.getUser().isEnabled()) {
            throw new BusinessRuleException("USER_DISABLED", "This user account is disabled");
        }

        IssuedRefreshToken replacement = create(current.getUser(), deviceInfo);
        current.setRevokedAt(now);
        current.setReplacedByTokenHash(replacement.entity().getTokenHash());
        repository.save(current);
        return new RotatedRefreshToken(current.getUser(), replacement.rawToken(), replacement.entity());
    }

    @Transactional
    public void revoke(String rawToken, User expectedUser) {
        repository.findByTokenHashForUpdate(HashingUtils.sha256(rawToken)).ifPresent(token -> {
            if (!token.getUser().getId().equals(expectedUser.getId())) {
                throw new BusinessRuleException("REFRESH_TOKEN_USER_MISMATCH",
                        "Refresh token does not belong to the authenticated user");
            }
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
                repository.save(token);
            }
        });
    }

    @Transactional
    public void revokeAll(User user) {
        repository.revokeAllActiveForUser(user.getId(), Instant.now());
    }

    @Scheduled(cron = "0 30 2 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        repository.deleteByExpiresAtBefore(Instant.now().minusSeconds(86400));
    }

    private String generateToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeDeviceInfo(String deviceInfo) {
        if (deviceInfo == null || deviceInfo.isBlank()) {
            return null;
        }
        return deviceInfo.length() <= 255 ? deviceInfo : deviceInfo.substring(0, 255);
    }

    public record IssuedRefreshToken(String rawToken, RefreshToken entity) {
    }

    public record RotatedRefreshToken(User user, String rawToken, RefreshToken entity) {
    }
}

package com.transitops.backend.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final String audience;
    private final Duration accessTokenDuration;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}")
            String issuer,
            @Value("${app.jwt.audience}")
            String audience,
            @Value("${app.jwt.access-token-minutes}")
            long accessTokenMinutes) {

        if (accessTokenMinutes <= 0) {
            throw new IllegalArgumentException(
                "Access-token duration must be positive"
            );
        }

        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.audience = audience;

        this.accessTokenDuration =
            Duration.ofMinutes(accessTokenMinutes);
    }

    public String generateAccessToken(
            TransitOpsUserPrincipal principal) {

        Instant issuedAt = Instant.now();
        Instant expiresAt =
            issuedAt.plus(accessTokenDuration);

        List<String> authorities =
            principal.getAuthorities()
                .stream()
                .map(authority ->
                    authority.getAuthority()
                )
                .distinct()
                .sorted()
                .toList();

        JwtClaimsSet claims =
            JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(principal.getUsername())
                .audience(List.of(audience))
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)

                .claim(
                    "uid",
                    principal.getUserId()
                )

                .claim(
                    "name",
                    principal.getFullName()
                )

                .claim(
                    "authorities",
                    authorities
                )

                .claim(
                    "ver",
                    principal.getTokenVersion()
                )

                .claim(
                    "mustChangePassword",
                    principal
                        .isMustChangePassword()
                )

                .build();

        JwsHeader headers =
            JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return jwtEncoder
            .encode(
                JwtEncoderParameters.from(
                    headers,
                    claims
                )
            )
            .getTokenValue();
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenDuration.toSeconds();
    }
}
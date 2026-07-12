package com.transitops.backend.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.transitops.backend.repository.UserRepository;
import com.transitops.backend.security.JwtAudienceValidator;
import com.transitops.backend.security.JwtTokenVersionValidator;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret}")
            String base64Secret) {

        byte[] keyBytes;

        try {
            keyBytes = Base64
                .getDecoder()
                .decode(base64Secret.trim());
        }
        catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                "app.jwt.secret must be valid Base64",
                exception
            );
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must contain at least 32 bytes"
            );
        }

        return new SecretKeySpec(
            keyBytes,
            "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(
            SecretKey jwtSecretKey) {

        return NimbusJwtEncoder
            .withSecretKey(jwtSecretKey)
            .algorithm(MacAlgorithm.HS256)
            .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            UserRepository userRepository,
            @Value("${app.jwt.issuer}")
            String issuer,
            @Value("${app.jwt.audience}")
            String audience) {

        NimbusJwtDecoder jwtDecoder =
            NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
            JwtValidators.createDefaultWithIssuer(
                issuer
            );

        OAuth2TokenValidator<Jwt> audienceValidator =
            new JwtAudienceValidator(audience);

        OAuth2TokenValidator<Jwt> versionValidator =
            new JwtTokenVersionValidator(
                userRepository
            );

        jwtDecoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                audienceValidator,
                versionValidator
            )
        );

        return jwtDecoder;
    }
}
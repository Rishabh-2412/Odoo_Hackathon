package com.transitops.backend.security;

import java.util.Collections;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtToAuthenticationConverter
        implements Converter<
            Jwt,
            AbstractAuthenticationToken
        > {

    @Override
    public AbstractAuthenticationToken convert(
            Jwt jwt) {

        List<String> authorityNames =
            jwt.getClaimAsStringList(
                "authorities"
            );

        if (authorityNames == null) {
            authorityNames =
                Collections.emptyList();
        }

        List<SimpleGrantedAuthority> authorities =
            authorityNames
                .stream()
                .filter(authority ->
                    authority != null
                    && !authority.isBlank()
                )
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new JwtAuthenticationToken(
            jwt,
            authorities,
            jwt.getSubject()
        );
    }
}
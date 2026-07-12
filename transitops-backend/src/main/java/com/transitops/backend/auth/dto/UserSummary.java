package com.transitops.backend.auth.dto;

import java.time.Instant;
import java.util.Set;

import com.transitops.backend.auth.model.RoleName;

public record UserSummary(
        Long id,
        String name,
        String email,
        boolean enabled,
        Set<RoleName> roles,
        Instant lastLoginAt,
        Instant createdAt) {
}

package com.transitops.backend.auth.service;

import java.util.stream.Collectors;

import com.transitops.backend.auth.dto.UserSummary;
import com.transitops.backend.auth.model.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()),
                user.getLastLoginAt(),
                user.getCreatedAt());
    }
}

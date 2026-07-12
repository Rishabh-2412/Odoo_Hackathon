package com.transitops.backend.dto.auth;

import java.util.List;

public record AuthUserResponse(

    Long id,
    String fullName,
    String email,
    String status,
    boolean mustChangePassword,
    List<String> roles,
    List<String> permissions

) {
}
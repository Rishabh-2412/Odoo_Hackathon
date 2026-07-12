package com.transitops.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank String refreshToken,
        boolean allDevices) {
}

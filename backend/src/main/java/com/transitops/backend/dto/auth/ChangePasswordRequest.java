package com.transitops.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

    @NotBlank(message = "Current password is required")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @Size(
        min = 8,
        max = 64,
        message = "New password must contain between 8 and 64 characters"
    )
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S+$",
        message = "Password must contain uppercase, lowercase, number and special character"
    )
    String newPassword,

    @NotBlank(message = "Password confirmation is required")
    String confirmPassword

) {
}
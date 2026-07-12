package com.transitops.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Public self-registration request.
 *
 * The role is intentionally not accepted from the client. Every public signup
 * is assigned the DRIVER role by the server to prevent privilege escalation.
 */
public record SignupRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 190) String email,
        @NotBlank @Size(min = 8, max = 72) String password) {
}

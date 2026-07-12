package com.transitops.backend.driver.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDriverRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 60) String licenseCategory,
        @NotNull LocalDate licenseExpiryDate,
        @NotBlank @Size(max = 30) String contactNumber,
        @Email @Size(max = 190) String email,
        @NotBlank @Size(max = 100) String region,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal safetyScore) {
}

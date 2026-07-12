package com.transitops.backend.driver.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.transitops.backend.driver.model.DriverStatus;

public record DriverResponse(
        Long id,
        String name,
        String licenseNumber,
        String licenseCategory,
        LocalDate licenseExpiryDate,
        String contactNumber,
        String email,
        String region,
        BigDecimal safetyScore,
        DriverStatus status,
        boolean licenseValid,
        Instant createdAt,
        Instant updatedAt,
        Long version) {
}

package com.transitops.backend.notification.dto;

import java.time.LocalDate;

public record LicenseExpiryAlert(
        Long driverId,
        String driverName,
        String licenseNumber,
        LocalDate expiryDate,
        long daysRemaining,
        String email) {
}

package com.transitops.backend.maintenance.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.transitops.backend.maintenance.model.MaintenanceStatus;

public record MaintenanceResponse(
        Long id,
        Long vehicleId,
        String vehicleRegistrationNumber,
        String vehicleNameModel,
        String serviceType,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal cost,
        BigDecimal odometerAtService,
        MaintenanceStatus status,
        String closingNotes,
        Instant createdAt,
        Instant updatedAt,
        Long version) {
}

package com.transitops.backend.fuel.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FuelLogResponse(
        Long id,
        Long vehicleId,
        String vehicleRegistrationNumber,
        Long tripId,
        BigDecimal liters,
        BigDecimal cost,
        LocalDate logDate,
        BigDecimal odometerKm,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        Long version) {
}

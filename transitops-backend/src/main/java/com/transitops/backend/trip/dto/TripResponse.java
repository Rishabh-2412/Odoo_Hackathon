package com.transitops.backend.trip.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.transitops.backend.trip.model.TripStatus;

public record TripResponse(
        Long id,
        String source,
        String destination,
        Long vehicleId,
        String vehicleRegistrationNumber,
        String vehicleNameModel,
        Long driverId,
        String driverName,
        String driverLicenseNumber,
        BigDecimal cargoWeightKg,
        BigDecimal plannedDistanceKm,
        BigDecimal actualDistanceKm,
        BigDecimal startOdometerKm,
        BigDecimal finalOdometerKm,
        BigDecimal fuelConsumedLiters,
        BigDecimal revenue,
        TripStatus status,
        Instant dispatchedAt,
        Instant completedAt,
        Instant cancelledAt,
        String notes,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt,
        Long version) {
}

package com.transitops.backend.vehicle.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.model.VehicleType;

public record VehicleResponse(
        Long id,
        String registrationNumber,
        String nameModel,
        VehicleType type,
        String region,
        BigDecimal maxLoadCapacityKg,
        BigDecimal odometerKm,
        BigDecimal acquisitionCost,
        VehicleStatus status,
        Instant createdAt,
        Instant updatedAt,
        Long version) {
}

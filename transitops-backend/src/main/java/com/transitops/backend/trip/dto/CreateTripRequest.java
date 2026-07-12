package com.transitops.backend.trip.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTripRequest(
        @NotBlank @Size(max = 180) String source,
        @NotBlank @Size(max = 180) String destination,
        @NotNull Long vehicleId,
        @NotNull Long driverId,
        @NotNull @DecimalMin("0.01") BigDecimal cargoWeightKg,
        @NotNull @DecimalMin("0.01") BigDecimal plannedDistanceKm,
        @Size(max = 1000) String notes) {
}

package com.transitops.backend.trip.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompleteTripRequest(
        @NotNull @DecimalMin("0.00") BigDecimal finalOdometerKm,
        @DecimalMin("0.00") BigDecimal fuelConsumedLiters,
        @DecimalMin("0.00") BigDecimal fuelCost,
        @DecimalMin("0.00") BigDecimal revenue,
        @Size(max = 1000) String notes) {
}

package com.transitops.backend.fuel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFuelLogRequest(
        @NotNull @DecimalMin("0.001") BigDecimal liters,
        @NotNull @DecimalMin("0.00") BigDecimal cost,
        @NotNull LocalDate logDate,
        @DecimalMin("0.00") BigDecimal odometerKm,
        @Size(max = 500) String notes) {
}

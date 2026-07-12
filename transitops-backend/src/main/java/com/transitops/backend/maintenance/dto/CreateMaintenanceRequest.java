package com.transitops.backend.maintenance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMaintenanceRequest(
        @NotNull Long vehicleId,
        @NotBlank @Size(max = 120) String serviceType,
        @Size(max = 1000) String description,
        @NotNull LocalDate startDate,
        @DecimalMin("0.00") BigDecimal estimatedOrInitialCost,
        @DecimalMin("0.00") BigDecimal odometerAtService) {
}

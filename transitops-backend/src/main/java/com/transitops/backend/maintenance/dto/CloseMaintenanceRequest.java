package com.transitops.backend.maintenance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CloseMaintenanceRequest(
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0.00") BigDecimal finalCost,
        @Size(max = 1000) String closingNotes) {
}

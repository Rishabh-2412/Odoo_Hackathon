package com.transitops.backend.report.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyCostPoint(
        YearMonth month,
        BigDecimal fuelCost,
        BigDecimal maintenanceCost,
        BigDecimal otherExpenseCost,
        BigDecimal totalCost) {
}

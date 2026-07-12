package com.transitops.backend.report.dto;

import java.math.BigDecimal;

public record VehicleAnalyticsResponse(
        Long vehicleId,
        String registrationNumber,
        String nameModel,
        BigDecimal completedDistanceKm,
        BigDecimal fuelLiters,
        BigDecimal fuelEfficiencyKmPerLiter,
        BigDecimal fuelCost,
        BigDecimal maintenanceCost,
        BigDecimal otherExpenseCost,
        BigDecimal operationalCost,
        BigDecimal revenue,
        BigDecimal acquisitionCost,
        BigDecimal roiPercent) {
}

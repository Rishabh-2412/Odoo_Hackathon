package com.transitops.backend.report.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FleetAnalyticsResponse(
        Instant generatedAt,
        BigDecimal fleetUtilizationPercent,
        BigDecimal totalDistanceKm,
        BigDecimal totalFuelLiters,
        BigDecimal averageFuelEfficiencyKmPerLiter,
        BigDecimal totalFuelCost,
        BigDecimal totalMaintenanceCost,
        BigDecimal totalOtherExpenses,
        BigDecimal totalOperationalCost,
        BigDecimal totalRevenue,
        List<VehicleAnalyticsResponse> vehicles,
        List<MonthlyCostPoint> monthlyCostTrend) {
}

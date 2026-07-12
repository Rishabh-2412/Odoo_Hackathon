package com.transitops.backend.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record DashboardResponse(
        Instant generatedAt,
        long activeVehicles,
        long availableVehicles,
        long vehiclesInMaintenance,
        long retiredVehicles,
        long activeTrips,
        long pendingTrips,
        long completedTrips,
        long driversOnDuty,
        long availableDrivers,
        BigDecimal fleetUtilizationPercent,
        Map<String, Long> vehicleStatusDistribution,
        Map<String, Long> tripStatusDistribution,
        Map<String, Long> driverStatusDistribution) {
}

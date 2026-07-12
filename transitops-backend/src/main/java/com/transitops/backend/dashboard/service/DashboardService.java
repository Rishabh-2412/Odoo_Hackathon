package com.transitops.backend.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.dashboard.dto.DashboardResponse;
import com.transitops.backend.driver.model.Driver;
import com.transitops.backend.driver.model.DriverStatus;
import com.transitops.backend.driver.repository.DriverRepository;
import com.transitops.backend.driver.repository.DriverSpecifications;
import com.transitops.backend.trip.model.Trip;
import com.transitops.backend.trip.model.TripStatus;
import com.transitops.backend.trip.repository.TripRepository;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.model.VehicleType;
import com.transitops.backend.vehicle.repository.VehicleRepository;
import com.transitops.backend.vehicle.repository.VehicleSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    @Transactional(readOnly = true)
    public DashboardResponse get(VehicleType type, VehicleStatus status, String region) {
        List<Vehicle> vehicles = vehicleRepository.findAll(VehicleSpecifications.filter(null, type, status, region));
        Set<Long> vehicleIds = vehicles.stream().map(Vehicle::getId).collect(Collectors.toSet());
        List<Trip> trips = tripRepository.findAll().stream()
                .filter(trip -> vehicleIds.contains(trip.getVehicle().getId()))
                .toList();
        List<Driver> drivers = driverRepository.findAll(DriverSpecifications.filter(null, null, region, null));

        long activeVehicles = vehicles.stream().filter(v -> v.getStatus() != VehicleStatus.RETIRED).count();
        long availableVehicles = countVehicles(vehicles, VehicleStatus.AVAILABLE);
        long inMaintenance = countVehicles(vehicles, VehicleStatus.IN_SHOP);
        long retired = countVehicles(vehicles, VehicleStatus.RETIRED);
        long activeTrips = countTrips(trips, TripStatus.DISPATCHED);
        long pendingTrips = countTrips(trips, TripStatus.DRAFT);
        long completedTrips = countTrips(trips, TripStatus.COMPLETED);
        long driversOnDuty = countDrivers(drivers, DriverStatus.ON_TRIP);
        long availableDrivers = countDrivers(drivers, DriverStatus.AVAILABLE);

        BigDecimal utilization = activeVehicles == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(countVehicles(vehicles, VehicleStatus.ON_TRIP))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(activeVehicles), 2, RoundingMode.HALF_UP);

        return new DashboardResponse(
                Instant.now(), activeVehicles, availableVehicles, inMaintenance, retired,
                activeTrips, pendingTrips, completedTrips, driversOnDuty, availableDrivers, utilization,
                vehicleDistribution(vehicles), tripDistribution(trips), driverDistribution(drivers));
    }

    private long countVehicles(List<Vehicle> items, VehicleStatus status) {
        return items.stream().filter(item -> item.getStatus() == status).count();
    }

    private long countTrips(List<Trip> items, TripStatus status) {
        return items.stream().filter(item -> item.getStatus() == status).count();
    }

    private long countDrivers(List<Driver> items, DriverStatus status) {
        return items.stream().filter(item -> item.getStatus() == status).count();
    }

    private Map<String, Long> vehicleDistribution(List<Vehicle> vehicles) {
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (VehicleStatus value : VehicleStatus.values()) {
            result.put(value.name(), countVehicles(vehicles, value));
        }
        return result;
    }

    private Map<String, Long> tripDistribution(List<Trip> trips) {
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (TripStatus value : TripStatus.values()) {
            result.put(value.name(), countTrips(trips, value));
        }
        return result;
    }

    private Map<String, Long> driverDistribution(List<Driver> drivers) {
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (DriverStatus value : DriverStatus.values()) {
            result.put(value.name(), countDrivers(drivers, value));
        }
        return result;
    }
}

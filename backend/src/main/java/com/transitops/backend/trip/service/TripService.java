package com.transitops.backend.trip.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.exception.ResourceNotFoundException;
import com.transitops.backend.driver.model.Driver;
import com.transitops.backend.driver.model.DriverStatus;
import com.transitops.backend.driver.repository.DriverRepository;
import com.transitops.backend.fuel.model.FuelLog;
import com.transitops.backend.fuel.repository.FuelLogRepository;
import com.transitops.backend.trip.dto.CancelTripRequest;
import com.transitops.backend.trip.dto.CompleteTripRequest;
import com.transitops.backend.trip.dto.CreateTripRequest;
import com.transitops.backend.trip.dto.TripResponse;
import com.transitops.backend.trip.dto.UpdateTripRequest;
import com.transitops.backend.trip.model.Trip;
import com.transitops.backend.trip.model.TripStatus;
import com.transitops.backend.trip.repository.TripRepository;
import com.transitops.backend.trip.repository.TripSpecifications;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final FuelLogRepository fuelLogRepository;

    @Transactional
    public TripResponse createDraft(CreateTripRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));
        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + request.driverId()));
        validateAssignment(vehicle, driver, request.cargoWeightKg());

        Trip trip = new Trip();
        applyDraftFields(trip, request.source(), request.destination(), vehicle, driver,
                request.cargoWeightKg(), request.plannedDistanceKm(), request.notes());
        trip.setStartOdometerKm(vehicle.getOdometerKm());
        trip.setStatus(TripStatus.DRAFT);
        return toResponse(tripRepository.save(trip));
    }

    @Transactional(readOnly = true)
    public PageResponse<TripResponse> list(
            String search,
            TripStatus status,
            Long vehicleId,
            Long driverId,
            Instant from,
            Instant to,
            Pageable pageable) {
        return PageResponse.from(
                tripRepository.findAll(TripSpecifications.filter(search, status, vehicleId, driverId, from, to), pageable),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public TripResponse get(Long id) {
        return toResponse(requireTrip(id));
    }

    @Transactional
    public TripResponse updateDraft(Long id, UpdateTripRequest request) {
        Trip trip = requireTrip(id);
        requireStatus(trip, TripStatus.DRAFT, "Only a draft trip can be edited");
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));
        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + request.driverId()));
        validateAssignment(vehicle, driver, request.cargoWeightKg());
        applyDraftFields(trip, request.source(), request.destination(), vehicle, driver,
                request.cargoWeightKg(), request.plannedDistanceKm(), request.notes());
        trip.setStartOdometerKm(vehicle.getOdometerKm());
        return toResponse(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse dispatch(Long id) {
        Trip trip = tripRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id));
        requireStatus(trip, TripStatus.DRAFT, "Only a draft trip can be dispatched");

        Vehicle vehicle = vehicleRepository.findByIdForUpdate(trip.getVehicle().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        Driver driver = driverRepository.findByIdForUpdate(trip.getDriver().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        validateAssignment(vehicle, driver, trip.getCargoWeightKg());

        trip.setStartOdometerKm(vehicle.getOdometerKm());
        trip.setStatus(TripStatus.DISPATCHED);
        trip.setDispatchedAt(Instant.now());
        vehicle.setStatus(VehicleStatus.ON_TRIP);
        driver.setStatus(DriverStatus.ON_TRIP);
        vehicleRepository.save(vehicle);
        driverRepository.save(driver);
        return toResponse(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse complete(Long id, CompleteTripRequest request) {
        Trip trip = tripRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id));
        requireStatus(trip, TripStatus.DISPATCHED, "Only a dispatched trip can be completed");

        Vehicle vehicle = vehicleRepository.findByIdForUpdate(trip.getVehicle().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        Driver driver = driverRepository.findByIdForUpdate(trip.getDriver().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (request.finalOdometerKm().compareTo(trip.getStartOdometerKm()) <= 0) {
            throw new BusinessRuleException("INVALID_FINAL_ODOMETER",
                    "Final odometer must be greater than the trip start odometer");
        }
        BigDecimal actualDistance = request.finalOdometerKm()
                .subtract(trip.getStartOdometerKm())
                .setScale(2, RoundingMode.HALF_UP);

        trip.setFinalOdometerKm(request.finalOdometerKm());
        trip.setActualDistanceKm(actualDistance);
        trip.setFuelConsumedLiters(zeroIfNull(request.fuelConsumedLiters()));
        trip.setRevenue(zeroIfNull(request.revenue()));
        trip.setNotes(mergeNotes(trip.getNotes(), request.notes()));
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(Instant.now());

        vehicle.setOdometerKm(request.finalOdometerKm());
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        driver.setStatus(driver.getLicenseExpiryDate().isBefore(LocalDate.now())
                ? DriverStatus.OFF_DUTY : DriverStatus.AVAILABLE);
        vehicleRepository.save(vehicle);
        driverRepository.save(driver);
        tripRepository.save(trip);

        if (request.fuelConsumedLiters() != null && request.fuelConsumedLiters().compareTo(BigDecimal.ZERO) > 0) {
            FuelLog fuelLog = new FuelLog();
            fuelLog.setVehicle(vehicle);
            fuelLog.setTrip(trip);
            fuelLog.setLiters(request.fuelConsumedLiters());
            fuelLog.setCost(zeroIfNull(request.fuelCost()));
            fuelLog.setLogDate(LocalDate.now());
            fuelLog.setOdometerKm(request.finalOdometerKm());
            fuelLog.setNotes("Automatically created during trip completion");
            fuelLogRepository.save(fuelLog);
        }
        return toResponse(trip);
    }

    @Transactional
    public TripResponse cancel(Long id, CancelTripRequest request) {
        Trip trip = tripRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id));
        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new BusinessRuleException("INVALID_TRIP_TRANSITION", "Completed or cancelled trips cannot be cancelled");
        }
        if (trip.getStatus() == TripStatus.DISPATCHED) {
            Vehicle vehicle = vehicleRepository.findByIdForUpdate(trip.getVehicle().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
            Driver driver = driverRepository.findByIdForUpdate(trip.getDriver().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
            if (vehicle.getStatus() != VehicleStatus.RETIRED) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
            }
            if (driver.getStatus() != DriverStatus.SUSPENDED) {
                driver.setStatus(driver.getLicenseExpiryDate().isBefore(LocalDate.now())
                        ? DriverStatus.OFF_DUTY : DriverStatus.AVAILABLE);
            }
            vehicleRepository.save(vehicle);
            driverRepository.save(driver);
        }
        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelledAt(Instant.now());
        trip.setCancellationReason(request.reason().trim());
        return toResponse(tripRepository.save(trip));
    }

    @Transactional
    public void delete(Long id) {
        Trip trip = requireTrip(id);
        if (trip.getStatus() != TripStatus.DRAFT && trip.getStatus() != TripStatus.CANCELLED) {
            throw new BusinessRuleException("TRIP_CANNOT_BE_DELETED", "Only draft or cancelled trips can be deleted");
        }
        tripRepository.delete(trip);
    }

    public Trip requireTrip(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id));
    }

    public TripResponse toResponse(Trip trip) {
        return new TripResponse(
                trip.getId(), trip.getSource(), trip.getDestination(),
                trip.getVehicle().getId(), trip.getVehicle().getRegistrationNumber(), trip.getVehicle().getNameModel(),
                trip.getDriver().getId(), trip.getDriver().getName(), trip.getDriver().getLicenseNumber(),
                trip.getCargoWeightKg(), trip.getPlannedDistanceKm(), trip.getActualDistanceKm(),
                trip.getStartOdometerKm(), trip.getFinalOdometerKm(), trip.getFuelConsumedLiters(),
                trip.getRevenue(), trip.getStatus(), trip.getDispatchedAt(), trip.getCompletedAt(), trip.getCancelledAt(),
                trip.getNotes(), trip.getCancellationReason(), trip.getCreatedAt(), trip.getUpdatedAt(), trip.getVersion());
    }

    private void validateAssignment(Vehicle vehicle, Driver driver, BigDecimal cargoWeightKg) {
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessRuleException("VEHICLE_NOT_AVAILABLE", "Selected vehicle is not available for dispatch");
        }
        if (vehicle.getStatus() == VehicleStatus.RETIRED || vehicle.getStatus() == VehicleStatus.IN_SHOP) {
            throw new BusinessRuleException("VEHICLE_NOT_DISPATCHABLE", "Retired or in-shop vehicles cannot be dispatched");
        }
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new BusinessRuleException("DRIVER_NOT_AVAILABLE", "Selected driver is not available for dispatch");
        }
        if (driver.getStatus() == DriverStatus.SUSPENDED) {
            throw new BusinessRuleException("DRIVER_SUSPENDED", "A suspended driver cannot be assigned to a trip");
        }
        if (driver.getLicenseExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("LICENSE_EXPIRED", "A driver with an expired license cannot be assigned to a trip");
        }
        if (cargoWeightKg.compareTo(vehicle.getMaxLoadCapacityKg()) > 0) {
            throw new BusinessRuleException("CARGO_CAPACITY_EXCEEDED",
                    "Cargo weight exceeds the vehicle maximum load capacity of " + vehicle.getMaxLoadCapacityKg() + " kg");
        }
    }

    private void applyDraftFields(Trip trip, String source, String destination, Vehicle vehicle, Driver driver,
            BigDecimal cargoWeightKg, BigDecimal plannedDistanceKm, String notes) {
        trip.setSource(source.trim());
        trip.setDestination(destination.trim());
        trip.setVehicle(vehicle);
        trip.setDriver(driver);
        trip.setCargoWeightKg(cargoWeightKg);
        trip.setPlannedDistanceKm(plannedDistanceKm);
        trip.setNotes(notes == null || notes.isBlank() ? null : notes.trim());
    }

    private void requireStatus(Trip trip, TripStatus status, String message) {
        if (trip.getStatus() != status) {
            throw new BusinessRuleException("INVALID_TRIP_TRANSITION", message);
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String mergeNotes(String existing, String additional) {
        if (additional == null || additional.isBlank()) {
            return existing;
        }
        return existing == null || existing.isBlank() ? additional.trim() : existing + "\n" + additional.trim();
    }
}

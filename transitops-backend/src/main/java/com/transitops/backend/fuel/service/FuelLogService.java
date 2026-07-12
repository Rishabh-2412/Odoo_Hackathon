package com.transitops.backend.fuel.service;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.exception.ResourceNotFoundException;
import com.transitops.backend.fuel.dto.CreateFuelLogRequest;
import com.transitops.backend.fuel.dto.FuelLogResponse;
import com.transitops.backend.fuel.dto.UpdateFuelLogRequest;
import com.transitops.backend.fuel.model.FuelLog;
import com.transitops.backend.fuel.repository.FuelLogRepository;
import com.transitops.backend.fuel.repository.FuelLogSpecifications;
import com.transitops.backend.trip.model.Trip;
import com.transitops.backend.trip.repository.TripRepository;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FuelLogService {
    private final FuelLogRepository repository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    @Transactional
    public FuelLogResponse create(CreateFuelLogRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));
        Trip trip = resolveTrip(request.tripId(), vehicle);
        FuelLog log = new FuelLog();
        log.setVehicle(vehicle);
        log.setTrip(trip);
        apply(log, request.liters(), request.cost(), request.logDate(), request.odometerKm(), request.notes());
        return toResponse(repository.save(log));
    }

    @Transactional(readOnly = true)
    public PageResponse<FuelLogResponse> list(Long vehicleId, Long tripId, LocalDate from, LocalDate to, Pageable pageable) {
        return PageResponse.from(repository.findAll(FuelLogSpecifications.filter(vehicleId, tripId, from, to), pageable),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public FuelLogResponse get(Long id) {
        return toResponse(requireLog(id));
    }

    @Transactional
    public FuelLogResponse update(Long id, UpdateFuelLogRequest request) {
        FuelLog log = requireLog(id);
        apply(log, request.liters(), request.cost(), request.logDate(), request.odometerKm(), request.notes());
        return toResponse(repository.save(log));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(requireLog(id));
    }

    private Trip resolveTrip(Long tripId, Vehicle vehicle) {
        if (tripId == null) {
            return null;
        }
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        if (!trip.getVehicle().getId().equals(vehicle.getId())) {
            throw new BusinessRuleException("TRIP_VEHICLE_MISMATCH", "The selected trip belongs to a different vehicle");
        }
        return trip;
    }

    private FuelLog requireLog(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel log not found: " + id));
    }

    private void apply(FuelLog log, java.math.BigDecimal liters, java.math.BigDecimal cost,
            LocalDate date, java.math.BigDecimal odometer, String notes) {
        log.setLiters(liters);
        log.setCost(cost);
        log.setLogDate(date);
        log.setOdometerKm(odometer);
        log.setNotes(notes == null || notes.isBlank() ? null : notes.trim());
    }

    public FuelLogResponse toResponse(FuelLog log) {
        return new FuelLogResponse(
                log.getId(), log.getVehicle().getId(), log.getVehicle().getRegistrationNumber(),
                log.getTrip() == null ? null : log.getTrip().getId(), log.getLiters(), log.getCost(),
                log.getLogDate(), log.getOdometerKm(), log.getNotes(), log.getCreatedAt(), log.getUpdatedAt(), log.getVersion());
    }
}

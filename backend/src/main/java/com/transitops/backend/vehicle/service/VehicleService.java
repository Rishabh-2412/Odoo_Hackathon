package com.transitops.backend.vehicle.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.exception.DuplicateResourceException;
import com.transitops.backend.common.exception.ResourceNotFoundException;
import com.transitops.backend.document.repository.VehicleDocumentRepository;
import com.transitops.backend.expense.repository.ExpenseRepository;
import com.transitops.backend.fuel.repository.FuelLogRepository;
import com.transitops.backend.maintenance.repository.MaintenanceLogRepository;
import com.transitops.backend.trip.repository.TripRepository;
import com.transitops.backend.vehicle.dto.CreateVehicleRequest;
import com.transitops.backend.vehicle.dto.UpdateVehicleRequest;
import com.transitops.backend.vehicle.dto.VehicleResponse;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.model.VehicleType;
import com.transitops.backend.vehicle.repository.VehicleRepository;
import com.transitops.backend.vehicle.repository.VehicleSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final FuelLogRepository fuelLogRepository;
    private final ExpenseRepository expenseRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;

    @Transactional
    public VehicleResponse create(CreateVehicleRequest request) {
        String registration = normalizeRegistration(request.registrationNumber());
        if (vehicleRepository.existsByRegistrationNumberIgnoreCase(registration)) {
            throw new DuplicateResourceException("Vehicle registration number already exists: " + registration);
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber(registration);
        vehicle.setNameModel(request.nameModel().trim());
        vehicle.setType(request.type());
        vehicle.setRegion(request.region().trim());
        vehicle.setMaxLoadCapacityKg(request.maxLoadCapacityKg());
        vehicle.setOdometerKm(request.odometerKm());
        vehicle.setAcquisitionCost(request.acquisitionCost());
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> list(
            String search,
            VehicleType type,
            VehicleStatus status,
            String region,
            Pageable pageable) {
        return PageResponse.from(
                vehicleRepository.findAll(VehicleSpecifications.filter(search, type, status, region), pageable),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> available(String region, VehicleType type) {
        return vehicleRepository.findAll(VehicleSpecifications.filter(null, type, VehicleStatus.AVAILABLE, region))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse get(Long id) {
        return toResponse(requireVehicle(id));
    }

    @Transactional
    public VehicleResponse update(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = requireVehicle(id);
        if (vehicle.getStatus() == VehicleStatus.ON_TRIP) {
            throw new BusinessRuleException("VEHICLE_ON_TRIP", "A vehicle on an active trip cannot be edited");
        }
        vehicle.setNameModel(request.nameModel().trim());
        vehicle.setType(request.type());
        vehicle.setRegion(request.region().trim());
        vehicle.setMaxLoadCapacityKg(request.maxLoadCapacityKg());
        vehicle.setAcquisitionCost(request.acquisitionCost());
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse changeStatus(Long id, VehicleStatus requestedStatus) {
        Vehicle vehicle = vehicleRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
        VehicleStatus current = vehicle.getStatus();
        if (current == VehicleStatus.ON_TRIP) {
            throw new BusinessRuleException("VEHICLE_ON_TRIP", "Trip completion or cancellation controls this vehicle status");
        }
        if (current == VehicleStatus.IN_SHOP && requestedStatus != VehicleStatus.RETIRED) {
            throw new BusinessRuleException("VEHICLE_IN_MAINTENANCE", "Close active maintenance before changing vehicle status");
        }
        if (requestedStatus == VehicleStatus.ON_TRIP || requestedStatus == VehicleStatus.IN_SHOP) {
            throw new BusinessRuleException("INVALID_MANUAL_STATUS", "ON_TRIP and IN_SHOP are controlled by trip and maintenance workflows");
        }
        vehicle.setStatus(requestedStatus);
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse retire(Long id) {
        return changeStatus(id, VehicleStatus.RETIRED);
    }

    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = requireVehicle(id);
        boolean hasHistory = tripRepository.existsByVehicleId(id)
                || maintenanceLogRepository.existsByVehicleId(id)
                || fuelLogRepository.existsByVehicleId(id)
                || expenseRepository.existsByVehicleId(id)
                || vehicleDocumentRepository.existsByVehicleId(id);
        if (hasHistory) {
            throw new BusinessRuleException("VEHICLE_HAS_HISTORY", "This vehicle has operational history; retire it instead of deleting it");
        }
        vehicleRepository.delete(vehicle);
    }

    public Vehicle requireVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(), vehicle.getRegistrationNumber(), vehicle.getNameModel(), vehicle.getType(),
                vehicle.getRegion(), vehicle.getMaxLoadCapacityKg(), vehicle.getOdometerKm(),
                vehicle.getAcquisitionCost(), vehicle.getStatus(), vehicle.getCreatedAt(), vehicle.getUpdatedAt(),
                vehicle.getVersion());
    }

    private String normalizeRegistration(String value) {
        return value.trim().toUpperCase().replaceAll("\\s+", "");
    }
}

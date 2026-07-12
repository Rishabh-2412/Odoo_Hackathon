package com.transitops.backend.maintenance.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.exception.ResourceNotFoundException;
import com.transitops.backend.maintenance.dto.CloseMaintenanceRequest;
import com.transitops.backend.maintenance.dto.CreateMaintenanceRequest;
import com.transitops.backend.maintenance.dto.MaintenanceResponse;
import com.transitops.backend.maintenance.model.MaintenanceLog;
import com.transitops.backend.maintenance.model.MaintenanceStatus;
import com.transitops.backend.maintenance.repository.MaintenanceLogRepository;
import com.transitops.backend.maintenance.repository.MaintenanceSpecifications;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private final MaintenanceLogRepository repository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public MaintenanceResponse create(CreateMaintenanceRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdForUpdate(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));
        if (vehicle.getStatus() == VehicleStatus.ON_TRIP) {
            throw new BusinessRuleException("VEHICLE_ON_TRIP", "A vehicle on a trip cannot enter maintenance");
        }
        if (vehicle.getStatus() == VehicleStatus.RETIRED) {
            throw new BusinessRuleException("VEHICLE_RETIRED", "A retired vehicle cannot enter active maintenance");
        }
        if (repository.existsByVehicleIdAndStatus(vehicle.getId(), MaintenanceStatus.ACTIVE)) {
            throw new BusinessRuleException("ACTIVE_MAINTENANCE_EXISTS", "This vehicle already has an active maintenance record");
        }

        MaintenanceLog log = new MaintenanceLog();
        log.setVehicle(vehicle);
        log.setServiceType(request.serviceType().trim());
        log.setDescription(normalize(request.description()));
        log.setStartDate(request.startDate());
        log.setCost(request.estimatedOrInitialCost() == null ? BigDecimal.ZERO : request.estimatedOrInitialCost());
        log.setOdometerAtService(request.odometerAtService() == null ? vehicle.getOdometerKm() : request.odometerAtService());
        log.setStatus(MaintenanceStatus.ACTIVE);

        vehicle.setStatus(VehicleStatus.IN_SHOP);
        vehicleRepository.save(vehicle);
        return toResponse(repository.save(log));
    }

    @Transactional(readOnly = true)
    public PageResponse<MaintenanceResponse> list(
            Long vehicleId,
            MaintenanceStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {
        return PageResponse.from(repository.findAll(MaintenanceSpecifications.filter(vehicleId, status, from, to), pageable),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse get(Long id) {
        return toResponse(requireLog(id));
    }

    @Transactional
    public MaintenanceResponse close(Long id, CloseMaintenanceRequest request) {
        MaintenanceLog log = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found: " + id));
        if (log.getStatus() != MaintenanceStatus.ACTIVE) {
            throw new BusinessRuleException("MAINTENANCE_ALREADY_CLOSED", "This maintenance record is already closed");
        }
        if (request.endDate().isBefore(log.getStartDate())) {
            throw new BusinessRuleException("INVALID_MAINTENANCE_DATES", "Maintenance end date cannot be before start date");
        }

        Vehicle vehicle = vehicleRepository.findByIdForUpdate(log.getVehicle().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        log.setEndDate(request.endDate());
        log.setCost(request.finalCost());
        log.setClosingNotes(normalize(request.closingNotes()));
        log.setStatus(MaintenanceStatus.CLOSED);
        if (vehicle.getStatus() != VehicleStatus.RETIRED) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }
        return toResponse(repository.save(log));
    }

    @Transactional
    public void delete(Long id) {
        MaintenanceLog log = requireLog(id);
        if (log.getStatus() == MaintenanceStatus.ACTIVE) {
            throw new BusinessRuleException("ACTIVE_MAINTENANCE_CANNOT_BE_DELETED", "Close active maintenance before deleting the record");
        }
        repository.delete(log);
    }

    private MaintenanceLog requireLog(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found: " + id));
    }

    public MaintenanceResponse toResponse(MaintenanceLog log) {
        return new MaintenanceResponse(
                log.getId(), log.getVehicle().getId(), log.getVehicle().getRegistrationNumber(),
                log.getVehicle().getNameModel(), log.getServiceType(), log.getDescription(), log.getStartDate(),
                log.getEndDate(), log.getCost(), log.getOdometerAtService(), log.getStatus(), log.getClosingNotes(),
                log.getCreatedAt(), log.getUpdatedAt(), log.getVersion());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

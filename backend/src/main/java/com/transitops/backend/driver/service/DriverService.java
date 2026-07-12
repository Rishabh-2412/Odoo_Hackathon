package com.transitops.backend.driver.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.exception.DuplicateResourceException;
import com.transitops.backend.common.exception.ResourceNotFoundException;
import com.transitops.backend.driver.dto.CreateDriverRequest;
import com.transitops.backend.driver.dto.DriverResponse;
import com.transitops.backend.driver.dto.UpdateDriverRequest;
import com.transitops.backend.driver.model.Driver;
import com.transitops.backend.driver.model.DriverStatus;
import com.transitops.backend.driver.repository.DriverRepository;
import com.transitops.backend.driver.repository.DriverSpecifications;
import com.transitops.backend.trip.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    @Transactional
    public DriverResponse create(CreateDriverRequest request) {
        String licenseNumber = normalizeLicense(request.licenseNumber());
        if (driverRepository.existsByLicenseNumberIgnoreCase(licenseNumber)) {
            throw new DuplicateResourceException("Driver license number already exists: " + licenseNumber);
        }
        Driver driver = new Driver();
        driver.setName(request.name().trim());
        driver.setLicenseNumber(licenseNumber);
        driver.setLicenseCategory(request.licenseCategory().trim());
        driver.setLicenseExpiryDate(request.licenseExpiryDate());
        driver.setContactNumber(request.contactNumber().trim());
        driver.setEmail(normalizeNullable(request.email()));
        driver.setRegion(request.region().trim());
        driver.setSafetyScore(request.safetyScore());
        driver.setStatus(request.licenseExpiryDate().isBefore(LocalDate.now())
                ? DriverStatus.OFF_DUTY : DriverStatus.AVAILABLE);
        return toResponse(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public PageResponse<DriverResponse> list(
            String search,
            DriverStatus status,
            String region,
            Boolean licenseValid,
            Pageable pageable) {
        return PageResponse.from(
                driverRepository.findAll(DriverSpecifications.filter(search, status, region, licenseValid), pageable),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> available(String region) {
        return driverRepository.findAll(
                        DriverSpecifications.filter(null, DriverStatus.AVAILABLE, region, true))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse get(Long id) {
        return toResponse(requireDriver(id));
    }

    @Transactional
    public DriverResponse update(Long id, UpdateDriverRequest request) {
        Driver driver = requireDriver(id);
        if (driver.getStatus() == DriverStatus.ON_TRIP) {
            throw new BusinessRuleException("DRIVER_ON_TRIP", "A driver on an active trip cannot be edited");
        }
        driver.setName(request.name().trim());
        driver.setLicenseCategory(request.licenseCategory().trim());
        driver.setLicenseExpiryDate(request.licenseExpiryDate());
        driver.setContactNumber(request.contactNumber().trim());
        driver.setEmail(normalizeNullable(request.email()));
        driver.setRegion(request.region().trim());
        driver.setSafetyScore(request.safetyScore());
        if (request.licenseExpiryDate().isBefore(LocalDate.now()) && driver.getStatus() == DriverStatus.AVAILABLE) {
            driver.setStatus(DriverStatus.OFF_DUTY);
        }
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse changeStatus(Long id, DriverStatus requestedStatus) {
        Driver driver = driverRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        if (driver.getStatus() == DriverStatus.ON_TRIP) {
            throw new BusinessRuleException("DRIVER_ON_TRIP", "Trip completion or cancellation controls this driver status");
        }
        if (requestedStatus == DriverStatus.ON_TRIP) {
            throw new BusinessRuleException("INVALID_MANUAL_STATUS", "ON_TRIP is controlled by the trip workflow");
        }
        if (requestedStatus == DriverStatus.AVAILABLE && driver.getLicenseExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("LICENSE_EXPIRED", "A driver with an expired license cannot be made available");
        }
        driver.setStatus(requestedStatus);
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public void delete(Long id) {
        Driver driver = requireDriver(id);
        if (tripRepository.existsByDriverId(id)) {
            throw new BusinessRuleException("DRIVER_HAS_HISTORY", "This driver has trip history and cannot be deleted; set the driver off duty or suspended");
        }
        driverRepository.delete(driver);
    }

    public Driver requireDriver(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
    }

    public DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
                driver.getId(), driver.getName(), driver.getLicenseNumber(), driver.getLicenseCategory(),
                driver.getLicenseExpiryDate(), driver.getContactNumber(), driver.getEmail(), driver.getRegion(),
                driver.getSafetyScore(), driver.getStatus(),
                !driver.getLicenseExpiryDate().isBefore(LocalDate.now()),
                driver.getCreatedAt(), driver.getUpdatedAt(), driver.getVersion());
    }

    private String normalizeLicense(String value) {
        return value.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase();
    }
}

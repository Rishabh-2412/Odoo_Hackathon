package com.transitops.backend.vehicle.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.vehicle.dto.CreateVehicleRequest;
import com.transitops.backend.vehicle.dto.UpdateVehicleRequest;
import com.transitops.backend.vehicle.dto.UpdateVehicleStatusRequest;
import com.transitops.backend.vehicle.dto.VehicleResponse;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.model.VehicleType;
import com.transitops.backend.vehicle.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponse create(@Valid @RequestBody CreateVehicleRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<VehicleResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) VehicleType type,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) String region,
            @PageableDefault(size = 20, sort = "registrationNumber") Pageable pageable) {
        return service.list(search, type, status, region, pageable);
    }

    @GetMapping("/available")
    public List<VehicleResponse> available(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) VehicleType type) {
        return service.available(region, type);
    }

    @GetMapping("/{id}")
    public VehicleResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponse changeStatus(@PathVariable Long id, @Valid @RequestBody UpdateVehicleStatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    @PostMapping("/{id}/retire")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponse retire(@PathVariable Long id) {
        return service.retire(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

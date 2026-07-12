package com.transitops.backend.maintenance.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.maintenance.dto.CloseMaintenanceRequest;
import com.transitops.backend.maintenance.dto.CreateMaintenanceRequest;
import com.transitops.backend.maintenance.dto.MaintenanceResponse;
import com.transitops.backend.maintenance.model.MaintenanceStatus;
import com.transitops.backend.maintenance.service.MaintenanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
public class MaintenanceController {
    private final MaintenanceService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceResponse create(@Valid @RequestBody CreateMaintenanceRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<MaintenanceResponse> list(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return service.list(vehicleId, status, from, to, pageable);
    }

    @GetMapping("/{id}")
    public MaintenanceResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/close")
    public MaintenanceResponse close(@PathVariable Long id, @Valid @RequestBody CloseMaintenanceRequest request) {
        return service.close(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

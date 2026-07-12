package com.transitops.backend.fuel.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.fuel.dto.CreateFuelLogRequest;
import com.transitops.backend.fuel.dto.FuelLogResponse;
import com.transitops.backend.fuel.dto.UpdateFuelLogRequest;
import com.transitops.backend.fuel.service.FuelLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fuel-logs")
@RequiredArgsConstructor
public class FuelLogController {
    private final FuelLogService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FINANCIAL_ANALYST','FLEET_MANAGER')")
    public FuelLogResponse create(@Valid @RequestBody CreateFuelLogRequest request) {
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCIAL_ANALYST','FLEET_MANAGER')")
    public PageResponse<FuelLogResponse> list(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "logDate") Pageable pageable) {
        return service.list(vehicleId, tripId, from, to, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCIAL_ANALYST','FLEET_MANAGER')")
    public FuelLogResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCIAL_ANALYST')")
    public FuelLogResponse update(@PathVariable Long id, @Valid @RequestBody UpdateFuelLogRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','FINANCIAL_ANALYST')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

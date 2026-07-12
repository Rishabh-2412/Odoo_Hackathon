package com.transitops.backend.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.dashboard.dto.DashboardResponse;
import com.transitops.backend.dashboard.service.DashboardService;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.model.VehicleType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;

    @GetMapping
    public DashboardResponse get(
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) VehicleStatus vehicleStatus,
            @RequestParam(required = false) String region) {
        return service.get(vehicleType, vehicleStatus, region);
    }
}

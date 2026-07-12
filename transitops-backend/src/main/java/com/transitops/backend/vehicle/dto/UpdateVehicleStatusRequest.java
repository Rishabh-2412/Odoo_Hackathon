package com.transitops.backend.vehicle.dto;

import com.transitops.backend.vehicle.model.VehicleStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateVehicleStatusRequest(@NotNull VehicleStatus status) {
}

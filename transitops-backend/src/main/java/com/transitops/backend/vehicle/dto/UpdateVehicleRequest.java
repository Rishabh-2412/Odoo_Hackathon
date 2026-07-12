package com.transitops.backend.vehicle.dto;

import java.math.BigDecimal;

import com.transitops.backend.vehicle.model.VehicleType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateVehicleRequest(
        @NotBlank @Size(max = 120) String nameModel,
        @NotNull VehicleType type,
        @NotBlank @Size(max = 100) String region,
        @NotNull @DecimalMin(value = "0.01") BigDecimal maxLoadCapacityKg,
        @NotNull @DecimalMin(value = "0.00") BigDecimal acquisitionCost) {
}

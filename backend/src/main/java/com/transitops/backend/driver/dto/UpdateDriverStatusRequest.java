package com.transitops.backend.driver.dto;

import com.transitops.backend.driver.model.DriverStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateDriverStatusRequest(@NotNull DriverStatus status) {
}

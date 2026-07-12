package com.transitops.backend.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelTripRequest(@NotBlank @Size(max = 500) String reason) {
}

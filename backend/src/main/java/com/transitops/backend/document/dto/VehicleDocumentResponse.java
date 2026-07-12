package com.transitops.backend.document.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.transitops.backend.document.model.VehicleDocumentType;

public record VehicleDocumentResponse(
        Long id,
        Long vehicleId,
        String vehicleRegistrationNumber,
        VehicleDocumentType documentType,
        String originalFileName,
        String contentType,
        long sizeBytes,
        LocalDate expiryDate,
        boolean expired,
        Instant createdAt) {
}

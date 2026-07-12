package com.transitops.backend.expense.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.transitops.backend.expense.model.ExpenseType;

public record ExpenseResponse(
        Long id,
        Long vehicleId,
        String vehicleRegistrationNumber,
        Long tripId,
        ExpenseType type,
        BigDecimal amount,
        LocalDate expenseDate,
        String description,
        Instant createdAt,
        Instant updatedAt,
        Long version) {
}

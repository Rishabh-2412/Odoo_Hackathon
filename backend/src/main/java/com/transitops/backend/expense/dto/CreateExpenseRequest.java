package com.transitops.backend.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.transitops.backend.expense.model.ExpenseType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateExpenseRequest(
        @NotNull Long vehicleId,
        Long tripId,
        @NotNull ExpenseType type,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate expenseDate,
        @NotBlank @Size(max = 500) String description) {
}

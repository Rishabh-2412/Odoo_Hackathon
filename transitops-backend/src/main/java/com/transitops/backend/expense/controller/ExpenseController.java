package com.transitops.backend.expense.controller;

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
import com.transitops.backend.expense.dto.CreateExpenseRequest;
import com.transitops.backend.expense.dto.ExpenseResponse;
import com.transitops.backend.expense.dto.UpdateExpenseRequest;
import com.transitops.backend.expense.model.ExpenseType;
import com.transitops.backend.expense.service.ExpenseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','FINANCIAL_ANALYST','FLEET_MANAGER')")
public class ExpenseController {
    private final ExpenseService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody CreateExpenseRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<ExpenseResponse> list(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) ExpenseType type,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "expenseDate") Pageable pageable) {
        return service.list(vehicleId, tripId, type, from, to, pageable);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCIAL_ANALYST')")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody UpdateExpenseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','FINANCIAL_ANALYST')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

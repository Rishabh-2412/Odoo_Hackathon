package com.transitops.backend.expense.service;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.exception.ResourceNotFoundException;
import com.transitops.backend.expense.dto.CreateExpenseRequest;
import com.transitops.backend.expense.dto.ExpenseResponse;
import com.transitops.backend.expense.dto.UpdateExpenseRequest;
import com.transitops.backend.expense.model.Expense;
import com.transitops.backend.expense.model.ExpenseType;
import com.transitops.backend.expense.repository.ExpenseRepository;
import com.transitops.backend.expense.repository.ExpenseSpecifications;
import com.transitops.backend.trip.model.Trip;
import com.transitops.backend.trip.repository.TripRepository;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository repository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    @Transactional
    public ExpenseResponse create(CreateExpenseRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));
        Trip trip = resolveTrip(request.tripId(), vehicle);
        Expense expense = new Expense();
        expense.setVehicle(vehicle);
        expense.setTrip(trip);
        apply(expense, request.type(), request.amount(), request.expenseDate(), request.description());
        return toResponse(repository.save(expense));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> list(
            Long vehicleId, Long tripId, ExpenseType type, LocalDate from, LocalDate to, Pageable pageable) {
        return PageResponse.from(repository.findAll(ExpenseSpecifications.filter(vehicleId, tripId, type, from, to), pageable),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return toResponse(requireExpense(id));
    }

    @Transactional
    public ExpenseResponse update(Long id, UpdateExpenseRequest request) {
        Expense expense = requireExpense(id);
        apply(expense, request.type(), request.amount(), request.expenseDate(), request.description());
        return toResponse(repository.save(expense));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(requireExpense(id));
    }

    private Trip resolveTrip(Long tripId, Vehicle vehicle) {
        if (tripId == null) {
            return null;
        }
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        if (!trip.getVehicle().getId().equals(vehicle.getId())) {
            throw new BusinessRuleException("TRIP_VEHICLE_MISMATCH", "The selected trip belongs to a different vehicle");
        }
        return trip;
    }

    private Expense requireExpense(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    private void apply(Expense expense, ExpenseType type, java.math.BigDecimal amount,
            LocalDate date, String description) {
        expense.setType(type);
        expense.setAmount(amount);
        expense.setExpenseDate(date);
        expense.setDescription(description.trim());
    }

    public ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(), expense.getVehicle().getId(), expense.getVehicle().getRegistrationNumber(),
                expense.getTrip() == null ? null : expense.getTrip().getId(), expense.getType(), expense.getAmount(),
                expense.getExpenseDate(), expense.getDescription(), expense.getCreatedAt(), expense.getUpdatedAt(),
                expense.getVersion());
    }
}

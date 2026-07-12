package com.transitops.backend.expense.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.transitops.backend.expense.model.Expense;
import com.transitops.backend.expense.model.ExpenseType;

import jakarta.persistence.criteria.Predicate;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {
    }

    public static Specification<Expense> filter(
            Long vehicleId,
            Long tripId,
            ExpenseType type,
            LocalDate from,
            LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (vehicleId != null) {
                predicates.add(cb.equal(root.get("vehicle").get("id"), vehicleId));
            }
            if (tripId != null) {
                predicates.add(cb.equal(root.get("trip").get("id"), tripId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

package com.transitops.backend.fuel.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.transitops.backend.fuel.model.FuelLog;

import jakarta.persistence.criteria.Predicate;

public final class FuelLogSpecifications {
    private FuelLogSpecifications() {
    }

    public static Specification<FuelLog> filter(Long vehicleId, Long tripId, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (vehicleId != null) {
                predicates.add(cb.equal(root.get("vehicle").get("id"), vehicleId));
            }
            if (tripId != null) {
                predicates.add(cb.equal(root.get("trip").get("id"), tripId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("logDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("logDate"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

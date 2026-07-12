package com.transitops.backend.maintenance.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.transitops.backend.maintenance.model.MaintenanceLog;
import com.transitops.backend.maintenance.model.MaintenanceStatus;

import jakarta.persistence.criteria.Predicate;

public final class MaintenanceSpecifications {
    private MaintenanceSpecifications() {
    }

    public static Specification<MaintenanceLog> filter(
            Long vehicleId,
            MaintenanceStatus status,
            LocalDate from,
            LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (vehicleId != null) {
                predicates.add(cb.equal(root.get("vehicle").get("id"), vehicleId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

package com.transitops.backend.vehicle.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.model.VehicleType;

import jakarta.persistence.criteria.Predicate;

public final class VehicleSpecifications {
    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> filter(
            String search,
            VehicleType type,
            VehicleStatus status,
            String region) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("registrationNumber")), pattern),
                        cb.like(cb.lower(root.get("nameModel")), pattern)));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (region != null && !region.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("region")), region.trim().toLowerCase()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

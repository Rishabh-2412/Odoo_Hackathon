package com.transitops.backend.trip.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.transitops.backend.trip.model.Trip;
import com.transitops.backend.trip.model.TripStatus;

import jakarta.persistence.criteria.Predicate;

public final class TripSpecifications {
    private TripSpecifications() {
    }

    public static Specification<Trip> filter(
            String search,
            TripStatus status,
            Long vehicleId,
            Long driverId,
            Instant from,
            Instant to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("source")), pattern),
                        cb.like(cb.lower(root.get("destination")), pattern),
                        cb.like(cb.lower(root.join("vehicle").get("registrationNumber")), pattern),
                        cb.like(cb.lower(root.join("driver").get("name")), pattern)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (vehicleId != null) {
                predicates.add(cb.equal(root.get("vehicle").get("id"), vehicleId));
            }
            if (driverId != null) {
                predicates.add(cb.equal(root.get("driver").get("id"), driverId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

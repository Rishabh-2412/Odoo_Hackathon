package com.transitops.backend.driver.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.transitops.backend.driver.model.Driver;
import com.transitops.backend.driver.model.DriverStatus;

import jakarta.persistence.criteria.Predicate;

public final class DriverSpecifications {
    private DriverSpecifications() {
    }

    public static Specification<Driver> filter(
            String search,
            DriverStatus status,
            String region,
            Boolean licenseValid) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("licenseNumber")), pattern),
                        cb.like(cb.lower(root.get("contactNumber")), pattern)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (region != null && !region.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("region")), region.trim().toLowerCase()));
            }
            if (licenseValid != null) {
                LocalDate today = LocalDate.now();
                predicates.add(licenseValid
                        ? cb.greaterThanOrEqualTo(root.get("licenseExpiryDate"), today)
                        : cb.lessThan(root.get("licenseExpiryDate"), today));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

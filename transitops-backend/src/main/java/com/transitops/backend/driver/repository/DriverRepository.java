package com.transitops.backend.driver.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transitops.backend.driver.model.Driver;
import com.transitops.backend.driver.model.DriverStatus;

import jakarta.persistence.LockModeType;

public interface DriverRepository extends JpaRepository<Driver, Long>, JpaSpecificationExecutor<Driver> {
    boolean existsByLicenseNumberIgnoreCase(String licenseNumber);

    Optional<Driver> findByLicenseNumberIgnoreCase(String licenseNumber);

    List<Driver> findByStatusAndLicenseExpiryDateGreaterThanEqualOrderByNameAsc(DriverStatus status, LocalDate date);

    long countByStatus(DriverStatus status);

    List<Driver> findByLicenseExpiryDateBetween(LocalDate from, LocalDate to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Driver d where d.id = :id")
    Optional<Driver> findByIdForUpdate(@Param("id") Long id);
}

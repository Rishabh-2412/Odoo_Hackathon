package com.transitops.backend.vehicle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;

import jakarta.persistence.LockModeType;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);

    Optional<Vehicle> findByRegistrationNumberIgnoreCase(String registrationNumber);

    List<Vehicle> findByStatusOrderByRegistrationNumberAsc(VehicleStatus status);

    long countByStatus(VehicleStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehicle v where v.id = :id")
    Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);
}

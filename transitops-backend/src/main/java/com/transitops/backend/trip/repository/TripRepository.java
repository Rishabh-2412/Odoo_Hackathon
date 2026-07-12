package com.transitops.backend.trip.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transitops.backend.trip.model.Trip;
import com.transitops.backend.trip.model.TripStatus;

import jakarta.persistence.LockModeType;

public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {
    boolean existsByVehicleId(Long vehicleId);

    boolean existsByDriverId(Long driverId);

    long countByStatus(TripStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t join fetch t.vehicle join fetch t.driver where t.id = :id")
    Optional<Trip> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(t.actualDistanceKm), 0) from Trip t where t.vehicle.id = :vehicleId and t.status = com.transitops.backend.trip.model.TripStatus.COMPLETED")
    BigDecimal sumCompletedDistanceByVehicle(@Param("vehicleId") Long vehicleId);

    @Query("select coalesce(sum(t.revenue), 0) from Trip t where t.vehicle.id = :vehicleId and t.status = com.transitops.backend.trip.model.TripStatus.COMPLETED")
    BigDecimal sumRevenueByVehicle(@Param("vehicleId") Long vehicleId);

    @Query("select count(t) from Trip t where t.status = :status and t.createdAt between :from and :to")
    long countByStatusAndCreatedAtBetween(@Param("status") TripStatus status, @Param("from") Instant from, @Param("to") Instant to);
}

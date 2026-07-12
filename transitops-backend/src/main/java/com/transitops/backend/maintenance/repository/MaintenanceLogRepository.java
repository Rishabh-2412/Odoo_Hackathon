package com.transitops.backend.maintenance.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transitops.backend.maintenance.model.MaintenanceLog;
import com.transitops.backend.maintenance.model.MaintenanceStatus;

import jakarta.persistence.LockModeType;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long>, JpaSpecificationExecutor<MaintenanceLog> {
    boolean existsByVehicleId(Long vehicleId);

    boolean existsByVehicleIdAndStatus(Long vehicleId, MaintenanceStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MaintenanceLog m join fetch m.vehicle where m.id = :id")
    Optional<MaintenanceLog> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(m.cost), 0) from MaintenanceLog m where m.vehicle.id = :vehicleId")
    BigDecimal sumCostByVehicle(@Param("vehicleId") Long vehicleId);

    @Query("select coalesce(sum(m.cost), 0) from MaintenanceLog m")
    BigDecimal sumAllCost();
}

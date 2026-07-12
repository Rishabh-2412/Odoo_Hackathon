package com.transitops.backend.fuel.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transitops.backend.fuel.model.FuelLog;

public interface FuelLogRepository extends JpaRepository<FuelLog, Long>, JpaSpecificationExecutor<FuelLog> {
    boolean existsByVehicleId(Long vehicleId);

    @Query("select coalesce(sum(f.liters), 0) from FuelLog f where f.vehicle.id = :vehicleId")
    BigDecimal sumLitersByVehicle(@Param("vehicleId") Long vehicleId);

    @Query("select coalesce(sum(f.cost), 0) from FuelLog f where f.vehicle.id = :vehicleId")
    BigDecimal sumCostByVehicle(@Param("vehicleId") Long vehicleId);

    @Query("select coalesce(sum(f.cost), 0) from FuelLog f")
    BigDecimal sumAllCost();
}

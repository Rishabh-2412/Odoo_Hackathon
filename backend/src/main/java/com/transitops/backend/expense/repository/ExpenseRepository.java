package com.transitops.backend.expense.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transitops.backend.expense.model.Expense;
import com.transitops.backend.expense.model.ExpenseType;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    boolean existsByVehicleId(Long vehicleId);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.vehicle.id = :vehicleId")
    BigDecimal sumAmountByVehicle(@Param("vehicleId") Long vehicleId);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.vehicle.id = :vehicleId and e.type = :type")
    BigDecimal sumAmountByVehicleAndType(@Param("vehicleId") Long vehicleId, @Param("type") ExpenseType type);

    @Query("select coalesce(sum(e.amount), 0) from Expense e")
    BigDecimal sumAllAmount();
}

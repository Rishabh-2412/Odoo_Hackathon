package com.transitops.backend.trip.model;

import java.math.BigDecimal;
import java.time.Instant;

import com.transitops.backend.common.model.BaseEntity;
import com.transitops.backend.driver.model.Driver;
import com.transitops.backend.vehicle.model.Vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trips")
public class Trip extends BaseEntity {
    @Column(nullable = false, length = 180)
    private String source;

    @Column(nullable = false, length = 180)
    private String destination;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "cargo_weight_kg", nullable = false, precision = 14, scale = 2)
    private BigDecimal cargoWeightKg;

    @Column(name = "planned_distance_km", nullable = false, precision = 14, scale = 2)
    private BigDecimal plannedDistanceKm;

    @Column(name = "actual_distance_km", precision = 14, scale = 2)
    private BigDecimal actualDistanceKm;

    @Column(name = "start_odometer_km", nullable = false, precision = 16, scale = 2)
    private BigDecimal startOdometerKm;

    @Column(name = "final_odometer_km", precision = 16, scale = 2)
    private BigDecimal finalOdometerKm;

    @Column(name = "fuel_consumed_liters", precision = 14, scale = 3)
    private BigDecimal fuelConsumedLiters;

    @Column(precision = 16, scale = 2)
    private BigDecimal revenue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TripStatus status = TripStatus.DRAFT;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(length = 1000)
    private String notes;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
}

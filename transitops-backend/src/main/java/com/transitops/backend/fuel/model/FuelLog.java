package com.transitops.backend.fuel.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.transitops.backend.common.model.BaseEntity;
import com.transitops.backend.trip.model.Trip;
import com.transitops.backend.vehicle.model.Vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "fuel_logs")
public class FuelLog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal liters;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal cost;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "odometer_km", precision = 16, scale = 2)
    private BigDecimal odometerKm;

    @Column(length = 500)
    private String notes;
}

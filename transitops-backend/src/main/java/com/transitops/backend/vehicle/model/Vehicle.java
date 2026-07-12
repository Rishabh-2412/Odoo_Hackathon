package com.transitops.backend.vehicle.model;

import java.math.BigDecimal;

import com.transitops.backend.common.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vehicles", uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_registration", columnNames = "registration_number"))
public class Vehicle extends BaseEntity {
    @Column(name = "registration_number", nullable = false, length = 50)
    private String registrationNumber;

    @Column(name = "name_model", nullable = false, length = 120)
    private String nameModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleType type;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(name = "max_load_capacity_kg", nullable = false, precision = 14, scale = 2)
    private BigDecimal maxLoadCapacityKg;

    @Column(name = "odometer_km", nullable = false, precision = 16, scale = 2)
    private BigDecimal odometerKm;

    @Column(name = "acquisition_cost", nullable = false, precision = 16, scale = 2)
    private BigDecimal acquisitionCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleStatus status = VehicleStatus.AVAILABLE;
}

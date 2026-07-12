package com.transitops.backend.maintenance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.transitops.backend.common.model.BaseEntity;
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
@Table(name = "maintenance_logs")
public class MaintenanceLog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "service_type", nullable = false, length = 120)
    private String serviceType;

    @Column(length = 1000)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "odometer_at_service", precision = 16, scale = 2)
    private BigDecimal odometerAtService;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceStatus status = MaintenanceStatus.ACTIVE;

    @Column(name = "closing_notes", length = 1000)
    private String closingNotes;
}

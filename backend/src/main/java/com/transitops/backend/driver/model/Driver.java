package com.transitops.backend.driver.model;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "drivers", uniqueConstraints = @UniqueConstraint(name = "uk_driver_license", columnNames = "license_number"))
public class Driver extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "license_number", nullable = false, length = 80)
    private String licenseNumber;

    @Column(name = "license_category", nullable = false, length = 60)
    private String licenseCategory;

    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;

    @Column(name = "contact_number", nullable = false, length = 30)
    private String contactNumber;

    @Column(length = 190)
    private String email;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(name = "safety_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal safetyScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DriverStatus status = DriverStatus.AVAILABLE;
}

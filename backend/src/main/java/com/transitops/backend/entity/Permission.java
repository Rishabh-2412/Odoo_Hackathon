package com.transitops.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "permissions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_permissions_code",
            columnNames = "code"
        )
    },
    indexes = {
        @Index(
            name = "idx_permissions_module",
            columnList = "module"
        ),
        @Index(
            name = "idx_permissions_active",
            columnList = "active"
        )
    }
)
public class Permission extends BaseEntity {

    /*
     * Examples:
     * VEHICLE_READ
     * VEHICLE_CREATE
     * TRIP_DISPATCH
     * REPORT_EXPORT
     */
    @Column(
        name = "code",
        nullable = false,
        length = 100
    )
    private String code;

    @Column(
        name = "name",
        nullable = false,
        length = 150
    )
    private String name;

    @Column(
        name = "module",
        nullable = false,
        length = 80
    )
    private String module;

    @Column(
        name = "description",
        length = 500
    )
    private String description;

    @Column(
        name = "active",
        nullable = false
    )
    private boolean active = true;
}
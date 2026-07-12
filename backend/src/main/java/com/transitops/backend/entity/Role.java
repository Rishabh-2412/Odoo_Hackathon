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
    name = "roles",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_roles_code",
            columnNames = "code"
        )
    },
    indexes = {
        @Index(
            name = "idx_roles_active",
            columnList = "active"
        )
    }
)
public class Role extends BaseEntity {

    /*
     * Examples:
     * FLEET_MANAGER
     * DRIVER
     * SAFETY_OFFICER
     * FINANCIAL_ANALYST
     */
    @Column(
        name = "code",
        nullable = false,
        length = 80
    )
    private String code;

    @Column(
        name = "name",
        nullable = false,
        length = 120
    )
    private String name;

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
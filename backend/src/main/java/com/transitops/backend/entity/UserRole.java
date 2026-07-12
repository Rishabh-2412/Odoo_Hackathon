package com.transitops.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    name = "user_roles",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_roles_user_role",
            columnNames = {
                "user_id",
                "role_id"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_user_roles_user",
            columnList = "user_id"
        ),
        @Index(
            name = "idx_user_roles_role",
            columnList = "role_id"
        ),
        @Index(
            name = "idx_user_roles_active",
            columnList = "active"
        )
    }
)
public class UserRole extends BaseEntity {

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_user_roles_user"
        )
    )
    private User user;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "role_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_user_roles_role"
        )
    )
    private Role role;

    /*
     * User who assigned this role.
     * Null is permitted for system-generated assignments.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "assigned_by_user_id",
        foreignKey = @ForeignKey(
            name = "fk_user_roles_assigned_by"
        )
    )
    private User assignedBy;

    @Column(
        name = "active",
        nullable = false
    )
    private boolean active = true;

    /*
     * Null means that the role does not expire.
     */
    @Column(name = "expires_at")
    private Instant expiresAt;
}
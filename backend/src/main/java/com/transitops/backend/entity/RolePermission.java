package com.transitops.backend.entity;

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
    name = "role_permissions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_role_permissions_role_permission",
            columnNames = {
                "role_id",
                "permission_id"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_role_permissions_role",
            columnList = "role_id"
        ),
        @Index(
            name = "idx_role_permissions_permission",
            columnList = "permission_id"
        ),
        @Index(
            name = "idx_role_permissions_active",
            columnList = "active"
        )
    }
)
public class RolePermission extends BaseEntity {

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "role_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_role_permissions_role"
        )
    )
    private Role role;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "permission_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_role_permissions_permission"
        )
    )
    private Permission permission;

    /*
     * User who granted this permission.
     * Null is allowed for initial system data.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "granted_by_user_id",
        foreignKey = @ForeignKey(
            name = "fk_role_permissions_granted_by"
        )
    )
    private User grantedBy;

    @Column(
        name = "active",
        nullable = false
    )
    private boolean active = true;
}
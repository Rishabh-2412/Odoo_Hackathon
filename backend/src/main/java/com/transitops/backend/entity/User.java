package com.transitops.backend.entity;

import java.time.Instant;

import com.transitops.backend.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "app_users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_app_users_email",
            columnNames = "email"
        )
    },
    indexes = {
        @Index(
            name = "idx_app_users_status",
            columnList = "status"
        )
    }
)
public class User extends BaseEntity {

    @Column(
        name = "full_name",
        nullable = false,
        length = 120
    )
    private String fullName;

    @Column(
        name = "email",
        nullable = false,
        length = 190
    )
    private String email;

    /*
     * Never store the original password here.
     * Only store the BCrypt encoded password.
     */
    @Column(
        name = "password_hash",
        nullable = false,
        length = 255
    )
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 30
    )
    private UserStatus status = UserStatus.ACTIVE;

    @Column(
        name = "email_verified",
        nullable = false
    )
    private boolean emailVerified = false;

    @Column(
        name = "failed_login_attempts",
        nullable = false
    )
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private Instant accountLockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(
        name = "token_version",
        nullable = false
    )
    private long tokenVersion = 0L;
    @Column(
    name = "must_change_password",
    nullable = false
)
private boolean mustChangePassword = true;

@Column(name = "password_changed_at")
private Instant passwordChangedAt;
}
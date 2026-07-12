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
    name = "refresh_tokens",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_refresh_tokens_token_hash",
            columnNames = "token_hash"
        )
    },
    indexes = {
        @Index(
            name = "idx_refresh_tokens_user",
            columnList = "user_id"
        ),
        @Index(
            name = "idx_refresh_tokens_expiry",
            columnList = "expires_at"
        ),
        @Index(
            name = "idx_refresh_tokens_revoked",
            columnList = "revoked"
        )
    }
)
public class RefreshToken extends BaseEntity {

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_refresh_tokens_user"
        )
    )
    private User user;

    /*
     * Store only a SHA-256 hash of the refresh token.
     * Do not store the raw refresh token.
     */
    @Column(
        name = "token_hash",
        nullable = false,
        length = 64
    )
    private String tokenHash;

    @Column(
        name = "expires_at",
        nullable = false
    )
    private Instant expiresAt;

    @Column(
        name = "revoked",
        nullable = false
    )
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(
        name = "created_by_ip",
        length = 64
    )
    private String createdByIp;

    @Column(
        name = "replaced_by_token_hash",
        length = 64
    )
    private String replacedByTokenHash;
}
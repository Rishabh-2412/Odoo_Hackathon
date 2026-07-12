package com.transitops.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transitops.backend.entity.RefreshToken;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(
        String tokenHash
    );

    List<RefreshToken> findAllByUserIdAndRevokedFalse(
        Long userId
    );

    boolean existsByTokenHash(String tokenHash);
}
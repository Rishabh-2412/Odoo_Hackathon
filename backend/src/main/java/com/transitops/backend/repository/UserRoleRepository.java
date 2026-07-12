package com.transitops.backend.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transitops.backend.entity.UserRole;

public interface UserRoleRepository
        extends JpaRepository<UserRole, Long> {

    boolean existsByUserIdAndRoleId(
        Long userId,
        Long roleId
    );

    List<UserRole> findAllByUserId(Long userId);

    @Query("""
        SELECT ur
        FROM UserRole ur
        JOIN FETCH ur.role role
        WHERE ur.user.id = :userId
          AND ur.active = true
          AND role.active = true
          AND (
                ur.expiresAt IS NULL
                OR ur.expiresAt > :currentTime
              )
        """)
    List<UserRole> findActiveRolesByUserId(
        @Param("userId") Long userId,
        @Param("currentTime") Instant currentTime
    );
}
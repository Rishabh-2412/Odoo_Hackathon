package com.transitops.backend.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transitops.backend.entity.RolePermission;

public interface RolePermissionRepository
        extends JpaRepository<RolePermission, Long> {

    boolean existsByRoleIdAndPermissionId(
        Long roleId,
        Long permissionId
    );

    List<RolePermission> findAllByRoleId(Long roleId);

    @Query("""
        SELECT DISTINCT rp
        FROM RolePermission rp
        JOIN FETCH rp.permission permission
        WHERE rp.role.id IN :roleIds
          AND rp.active = true
          AND permission.active = true
        """)
    List<RolePermission> findActivePermissionsByRoleIds(
        @Param("roleIds") Collection<Long> roleIds
    );
}
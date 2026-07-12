package com.transitops.backend.security;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.entity.RolePermission;
import com.transitops.backend.entity.User;
import com.transitops.backend.entity.UserRole;
import com.transitops.backend.enums.UserStatus;
import com.transitops.backend.repository.RolePermissionRepository;
import com.transitops.backend.repository.UserRepository;
import com.transitops.backend.repository.UserRoleRepository;

@Service
public class DatabaseUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public DatabaseUserDetailsService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository) {

        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String normalizedEmail = email
            .trim()
            .toLowerCase(Locale.ROOT);

        User user = userRepository
            .findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Invalid email or password"
                )
            );

        Instant currentTime = Instant.now();

        List<UserRole> activeUserRoles =
            userRoleRepository.findActiveRolesByUserId(
                user.getId(),
                currentTime
            );

        Set<GrantedAuthority> authorities =
            new LinkedHashSet<>();

        /*
         * Add role authorities.
         *
         * Examples:
         * ROLE_SYSTEM_ADMIN
         * ROLE_FLEET_MANAGER
         */
        for (UserRole userRole : activeUserRoles) {

            authorities.add(
                new SimpleGrantedAuthority(
                    "ROLE_" + userRole.getRole().getCode()
                )
            );
        }

        List<Long> roleIds = activeUserRoles.stream()
            .map(userRole -> userRole.getRole().getId())
            .distinct()
            .toList();

        /*
         * Add permission authorities.
         *
         * Examples:
         * VEHICLE_READ
         * VEHICLE_CREATE
         * TRIP_DISPATCH
         */
        if (!roleIds.isEmpty()) {

            List<RolePermission> rolePermissions =
                rolePermissionRepository
                    .findActivePermissionsByRoleIds(roleIds);

            for (RolePermission rolePermission : rolePermissions) {

                authorities.add(
                    new SimpleGrantedAuthority(
                        rolePermission
                            .getPermission()
                            .getCode()
                    )
                );
            }
        }

        boolean temporaryLockActive =
            user.getAccountLockedUntil() != null
            && user.getAccountLockedUntil().isAfter(currentTime);

        boolean accountNonLocked =
            user.getStatus() != UserStatus.LOCKED
            && !temporaryLockActive;

        return new TransitOpsUserPrincipal(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getStatus(),
            accountNonLocked,
            user.isMustChangePassword(),
            user.getTokenVersion(),
            List.copyOf(authorities)
        );
    }
}
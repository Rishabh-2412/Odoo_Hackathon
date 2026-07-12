package com.transitops.backend.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.entity.Permission;
import com.transitops.backend.entity.Role;
import com.transitops.backend.entity.RolePermission;
import com.transitops.backend.entity.User;
import com.transitops.backend.entity.UserRole;
import com.transitops.backend.enums.UserStatus;
import com.transitops.backend.repository.PermissionRepository;
import com.transitops.backend.repository.RolePermissionRepository;
import com.transitops.backend.repository.RoleRepository;
import com.transitops.backend.repository.UserRepository;
import com.transitops.backend.repository.UserRoleRepository;
import com.transitops.backend.security.PermissionCodes;
import com.transitops.backend.security.RoleCodes;

@Component
public class RbacDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.name}")
    private String administratorName;

    @Value("${app.bootstrap.admin.email}")
    private String administratorEmail;

    @Value("${app.bootstrap.admin.password}")
    private String administratorPassword;

    public RbacDataInitializer(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {

        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        seedRoles();
        seedPermissions();
        assignPermissionsToRoles();
        seedSystemAdministrator();

        System.out.println(
            "TransitOps RBAC initialization completed successfully"
        );
    }

    private void seedRoles() {

        List<RoleSeed> roles = List.of(
            new RoleSeed(
                RoleCodes.SYSTEM_ADMIN,
                "System Administrator",
                "Manages users, roles, permissions and the complete system"
            ),
            new RoleSeed(
                RoleCodes.FLEET_MANAGER,
                "Fleet Manager",
                "Manages fleet assets, trips and maintenance operations"
            ),
            new RoleSeed(
                RoleCodes.DRIVER,
                "Driver",
                "Manages permitted trip and delivery operations"
            ),
            new RoleSeed(
                RoleCodes.SAFETY_OFFICER,
                "Safety Officer",
                "Manages driver compliance, licenses and safety information"
            ),
            new RoleSeed(
                RoleCodes.FINANCIAL_ANALYST,
                "Financial Analyst",
                "Manages expenses, fuel records and financial reports"
            )
        );

        for (RoleSeed roleSeed : roles) {

            Role role = roleRepository
                .findByCodeIgnoreCase(roleSeed.code())
                .orElseGet(Role::new);

            role.setCode(normalizeCode(roleSeed.code()));
            role.setName(roleSeed.name());
            role.setDescription(roleSeed.description());
            role.setActive(true);

            roleRepository.save(role);
        }
    }

    private void seedPermissions() {

        List<PermissionSeed> permissions = List.of(

            permission(
                PermissionCodes.DASHBOARD_READ,
                "View Dashboard",
                "DASHBOARD"
            ),

            permission(
                PermissionCodes.VEHICLE_READ,
                "View Vehicles",
                "VEHICLE"
            ),
            permission(
                PermissionCodes.VEHICLE_CREATE,
                "Create Vehicles",
                "VEHICLE"
            ),
            permission(
                PermissionCodes.VEHICLE_UPDATE,
                "Update Vehicles",
                "VEHICLE"
            ),
            permission(
                PermissionCodes.VEHICLE_DELETE,
                "Delete Vehicles",
                "VEHICLE"
            ),

            permission(
                PermissionCodes.DRIVER_READ,
                "View Drivers",
                "DRIVER"
            ),
            permission(
                PermissionCodes.DRIVER_CREATE,
                "Create Drivers",
                "DRIVER"
            ),
            permission(
                PermissionCodes.DRIVER_UPDATE,
                "Update Drivers",
                "DRIVER"
            ),
            permission(
                PermissionCodes.DRIVER_DELETE,
                "Delete Drivers",
                "DRIVER"
            ),

            permission(
                PermissionCodes.TRIP_READ,
                "View Trips",
                "TRIP"
            ),
            permission(
                PermissionCodes.TRIP_CREATE,
                "Create Trips",
                "TRIP"
            ),
            permission(
                PermissionCodes.TRIP_UPDATE,
                "Update Trips",
                "TRIP"
            ),
            permission(
                PermissionCodes.TRIP_DISPATCH,
                "Dispatch Trips",
                "TRIP"
            ),
            permission(
                PermissionCodes.TRIP_COMPLETE,
                "Complete Trips",
                "TRIP"
            ),
            permission(
                PermissionCodes.TRIP_CANCEL,
                "Cancel Trips",
                "TRIP"
            ),

            permission(
                PermissionCodes.MAINTENANCE_READ,
                "View Maintenance",
                "MAINTENANCE"
            ),
            permission(
                PermissionCodes.MAINTENANCE_CREATE,
                "Create Maintenance",
                "MAINTENANCE"
            ),
            permission(
                PermissionCodes.MAINTENANCE_UPDATE,
                "Update Maintenance",
                "MAINTENANCE"
            ),
            permission(
                PermissionCodes.MAINTENANCE_COMPLETE,
                "Complete Maintenance",
                "MAINTENANCE"
            ),
            permission(
                PermissionCodes.MAINTENANCE_CANCEL,
                "Cancel Maintenance",
                "MAINTENANCE"
            ),

            permission(
                PermissionCodes.FUEL_READ,
                "View Fuel Logs",
                "FUEL"
            ),
            permission(
                PermissionCodes.FUEL_CREATE,
                "Create Fuel Logs",
                "FUEL"
            ),
            permission(
                PermissionCodes.FUEL_UPDATE,
                "Update Fuel Logs",
                "FUEL"
            ),
            permission(
                PermissionCodes.FUEL_DELETE,
                "Delete Fuel Logs",
                "FUEL"
            ),

            permission(
                PermissionCodes.EXPENSE_READ,
                "View Expenses",
                "EXPENSE"
            ),
            permission(
                PermissionCodes.EXPENSE_CREATE,
                "Create Expenses",
                "EXPENSE"
            ),
            permission(
                PermissionCodes.EXPENSE_UPDATE,
                "Update Expenses",
                "EXPENSE"
            ),
            permission(
                PermissionCodes.EXPENSE_DELETE,
                "Delete Expenses",
                "EXPENSE"
            ),

            permission(
                PermissionCodes.REPORT_READ,
                "View Reports",
                "REPORT"
            ),
            permission(
                PermissionCodes.REPORT_EXPORT,
                "Export Reports",
                "REPORT"
            ),

            permission(
                PermissionCodes.USER_READ,
                "View Users",
                "USER"
            ),
            permission(
                PermissionCodes.USER_CREATE,
                "Create Users",
                "USER"
            ),
            permission(
                PermissionCodes.USER_UPDATE,
                "Update Users",
                "USER"
            ),
            permission(
                PermissionCodes.USER_ASSIGN_ROLE,
                "Assign User Roles",
                "USER"
            ),
            permission(
                PermissionCodes.RBAC_ROLE_MANAGE,
                "Manage Roles",
                "RBAC"
            ),
            permission(
                PermissionCodes.PERMISSION_MANAGE,
                "Manage Permissions",
                "RBAC"
            )
        );

        for (PermissionSeed permissionSeed : permissions) {

            Permission permission = permissionRepository
                .findByCodeIgnoreCase(permissionSeed.code())
                .orElseGet(Permission::new);

            permission.setCode(
                normalizeCode(permissionSeed.code())
            );

            permission.setName(permissionSeed.name());
            permission.setModule(permissionSeed.module());

            permission.setDescription(
                permissionSeed.description()
            );

            permission.setActive(true);

            permissionRepository.save(permission);
        }
    }

    private void assignPermissionsToRoles() {

        Map<String, Role> roles = roleRepository.findAll()
            .stream()
            .collect(
                Collectors.toMap(
                    Role::getCode,
                    Function.identity()
                )
            );

        Map<String, Permission> permissions =
            permissionRepository.findAll()
                .stream()
                .collect(
                    Collectors.toMap(
                        Permission::getCode,
                        Function.identity()
                    )
                );

        Map<String, Set<String>> rolePermissions =
            createRolePermissionMap(permissions.keySet());

        for (Map.Entry<String, Set<String>> entry :
                rolePermissions.entrySet()) {

            Role role = roles.get(entry.getKey());

            if (role == null) {
                throw new IllegalStateException(
                    "Required role was not found: "
                        + entry.getKey()
                );
            }

            for (String permissionCode : entry.getValue()) {

                Permission permission =
                    permissions.get(permissionCode);

                if (permission == null) {
                    throw new IllegalStateException(
                        "Required permission was not found: "
                            + permissionCode
                    );
                }

                boolean relationshipExists =
                    rolePermissionRepository
                        .existsByRoleIdAndPermissionId(
                            role.getId(),
                            permission.getId()
                        );

                if (!relationshipExists) {

                    RolePermission rolePermission =
                        new RolePermission();

                    rolePermission.setRole(role);
                    rolePermission.setPermission(permission);
                    rolePermission.setActive(true);
                    rolePermission.setGrantedBy(null);

                    rolePermissionRepository.save(rolePermission);
                }
            }
        }
    }

    private Map<String, Set<String>> createRolePermissionMap(
            Set<String> allPermissionCodes) {

        Map<String, Set<String>> permissions =
            new LinkedHashMap<>();

        // Administrator receives every permission.
        permissions.put(
            RoleCodes.SYSTEM_ADMIN,
            Set.copyOf(allPermissionCodes)
        );

        permissions.put(
            RoleCodes.FLEET_MANAGER,
            Set.of(
                PermissionCodes.DASHBOARD_READ,

                PermissionCodes.VEHICLE_READ,
                PermissionCodes.VEHICLE_CREATE,
                PermissionCodes.VEHICLE_UPDATE,
                PermissionCodes.VEHICLE_DELETE,

                PermissionCodes.DRIVER_READ,

                PermissionCodes.TRIP_READ,
                PermissionCodes.TRIP_CREATE,
                PermissionCodes.TRIP_UPDATE,
                PermissionCodes.TRIP_DISPATCH,
                PermissionCodes.TRIP_COMPLETE,
                PermissionCodes.TRIP_CANCEL,

                PermissionCodes.MAINTENANCE_READ,
                PermissionCodes.MAINTENANCE_CREATE,
                PermissionCodes.MAINTENANCE_UPDATE,
                PermissionCodes.MAINTENANCE_COMPLETE,
                PermissionCodes.MAINTENANCE_CANCEL,

                PermissionCodes.FUEL_READ,
                PermissionCodes.EXPENSE_READ,

                PermissionCodes.REPORT_READ,
                PermissionCodes.REPORT_EXPORT
            )
        );

        permissions.put(
            RoleCodes.DRIVER,
            Set.of(
                PermissionCodes.DASHBOARD_READ,
                PermissionCodes.VEHICLE_READ,
                PermissionCodes.DRIVER_READ,

                PermissionCodes.TRIP_READ,
                PermissionCodes.TRIP_CREATE,
                PermissionCodes.TRIP_UPDATE,
                PermissionCodes.TRIP_COMPLETE
            )
        );

        permissions.put(
            RoleCodes.SAFETY_OFFICER,
            Set.of(
                PermissionCodes.DASHBOARD_READ,

                PermissionCodes.DRIVER_READ,
                PermissionCodes.DRIVER_CREATE,
                PermissionCodes.DRIVER_UPDATE,

                PermissionCodes.TRIP_READ,
                PermissionCodes.VEHICLE_READ,
                PermissionCodes.REPORT_READ
            )
        );

        permissions.put(
            RoleCodes.FINANCIAL_ANALYST,
            Set.of(
                PermissionCodes.DASHBOARD_READ,

                PermissionCodes.VEHICLE_READ,
                PermissionCodes.TRIP_READ,
                PermissionCodes.MAINTENANCE_READ,

                PermissionCodes.FUEL_READ,
                PermissionCodes.FUEL_CREATE,
                PermissionCodes.FUEL_UPDATE,
                PermissionCodes.FUEL_DELETE,

                PermissionCodes.EXPENSE_READ,
                PermissionCodes.EXPENSE_CREATE,
                PermissionCodes.EXPENSE_UPDATE,
                PermissionCodes.EXPENSE_DELETE,

                PermissionCodes.REPORT_READ,
                PermissionCodes.REPORT_EXPORT
            )
        );

        return permissions;
    }

    private void seedSystemAdministrator() {

        String normalizedEmail = administratorEmail
            .trim()
            .toLowerCase(Locale.ROOT);

        User administrator = userRepository
            .findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> {

                User newAdministrator = new User();

                newAdministrator.setFullName(
                    administratorName.trim()
                );

                newAdministrator.setEmail(normalizedEmail);

                newAdministrator.setPasswordHash(
                    passwordEncoder.encode(
                        administratorPassword
                    )
                );

                newAdministrator.setStatus(UserStatus.ACTIVE);
                newAdministrator.setEmailVerified(true);
                newAdministrator.setFailedLoginAttempts(0);
                newAdministrator.setTokenVersion(0L);
                newAdministrator.setMustChangePassword(true);

                return userRepository.save(newAdministrator);
            });

        Role administratorRole = roleRepository
            .findByCodeIgnoreCase(RoleCodes.SYSTEM_ADMIN)
            .orElseThrow(() ->
                new IllegalStateException(
                    "SYSTEM_ADMIN role was not found"
                )
            );

        boolean assignmentExists =
            userRoleRepository.existsByUserIdAndRoleId(
                administrator.getId(),
                administratorRole.getId()
            );

        if (!assignmentExists) {

            UserRole userRole = new UserRole();

            userRole.setUser(administrator);
            userRole.setRole(administratorRole);
            userRole.setAssignedBy(null);
            userRole.setActive(true);
            userRole.setExpiresAt(null);

            userRoleRepository.save(userRole);
        }
    }

    private PermissionSeed permission(
            String code,
            String name,
            String module) {

        return new PermissionSeed(
            code,
            name,
            module,
            name + " permission"
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private record RoleSeed(
        String code,
        String name,
        String description
    ) {
    }

    private record PermissionSeed(
        String code,
        String name,
        String module,
        String description
    ) {
    }
}
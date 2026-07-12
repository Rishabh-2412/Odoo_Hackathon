package com.transitops.backend.security;

public final class PermissionCodes {

    // Dashboard
    public static final String DASHBOARD_READ = "DASHBOARD_READ";

    // Vehicles
    public static final String VEHICLE_READ = "VEHICLE_READ";
    public static final String VEHICLE_CREATE = "VEHICLE_CREATE";
    public static final String VEHICLE_UPDATE = "VEHICLE_UPDATE";
    public static final String VEHICLE_DELETE = "VEHICLE_DELETE";

    // Drivers
    public static final String DRIVER_READ = "DRIVER_READ";
    public static final String DRIVER_CREATE = "DRIVER_CREATE";
    public static final String DRIVER_UPDATE = "DRIVER_UPDATE";
    public static final String DRIVER_DELETE = "DRIVER_DELETE";

    // Trips
    public static final String TRIP_READ = "TRIP_READ";
    public static final String TRIP_CREATE = "TRIP_CREATE";
    public static final String TRIP_UPDATE = "TRIP_UPDATE";
    public static final String TRIP_DISPATCH = "TRIP_DISPATCH";
    public static final String TRIP_COMPLETE = "TRIP_COMPLETE";
    public static final String TRIP_CANCEL = "TRIP_CANCEL";

    // Maintenance
    public static final String MAINTENANCE_READ = "MAINTENANCE_READ";
    public static final String MAINTENANCE_CREATE = "MAINTENANCE_CREATE";
    public static final String MAINTENANCE_UPDATE = "MAINTENANCE_UPDATE";
    public static final String MAINTENANCE_COMPLETE =
        "MAINTENANCE_COMPLETE";
    public static final String MAINTENANCE_CANCEL =
        "MAINTENANCE_CANCEL";

    // Fuel logs
    public static final String FUEL_READ = "FUEL_READ";
    public static final String FUEL_CREATE = "FUEL_CREATE";
    public static final String FUEL_UPDATE = "FUEL_UPDATE";
    public static final String FUEL_DELETE = "FUEL_DELETE";

    // Expenses
    public static final String EXPENSE_READ = "EXPENSE_READ";
    public static final String EXPENSE_CREATE = "EXPENSE_CREATE";
    public static final String EXPENSE_UPDATE = "EXPENSE_UPDATE";
    public static final String EXPENSE_DELETE = "EXPENSE_DELETE";

    // Reports
    public static final String REPORT_READ = "REPORT_READ";
    public static final String REPORT_EXPORT = "REPORT_EXPORT";

    // Users and RBAC
    public static final String USER_READ = "USER_READ";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_ASSIGN_ROLE = "USER_ASSIGN_ROLE";
    public static final String RBAC_ROLE_MANAGE =
    "RBAC_ROLE_MANAGE";
    public static final String PERMISSION_MANAGE =
        "PERMISSION_MANAGE";

    private PermissionCodes() {
        throw new IllegalStateException(
            "PermissionCodes is a utility class and cannot be instantiated"
        );
    }
}
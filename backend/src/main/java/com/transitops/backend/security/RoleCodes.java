package com.transitops.backend.security;

public final class RoleCodes {

    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String FLEET_MANAGER = "FLEET_MANAGER";
    public static final String DRIVER = "DRIVER";
    public static final String SAFETY_OFFICER = "SAFETY_OFFICER";
    public static final String FINANCIAL_ANALYST = "FINANCIAL_ANALYST";

    private RoleCodes() {
        throw new IllegalStateException(
            "RoleCodes is a utility class and cannot be instantiated"
        );
    }
}
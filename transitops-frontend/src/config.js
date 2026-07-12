export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export const ROLES = {
  ADMIN: 'ADMIN',
  FLEET_MANAGER: 'FLEET_MANAGER',
  DRIVER: 'DRIVER',
  SAFETY_OFFICER: 'SAFETY_OFFICER',
  FINANCIAL_ANALYST: 'FINANCIAL_ANALYST',
};

// This mirrors the backend RBAC rules. Navigation is hidden when a role cannot
// use the corresponding module, while the backend remains the final authority.
export const ROLE_NAV_ACCESS = {
  ADMIN: ['dashboard', 'vehicles', 'drivers', 'trips', 'maintenance', 'fuel-logs', 'expenses', 'reports', 'users-roles'],
  FLEET_MANAGER: ['dashboard', 'vehicles', 'drivers', 'trips', 'maintenance', 'fuel-logs', 'expenses', 'reports'],
  DRIVER: ['dashboard', 'vehicles', 'drivers', 'trips'],
  SAFETY_OFFICER: ['dashboard', 'vehicles', 'drivers', 'trips'],
  FINANCIAL_ANALYST: ['dashboard', 'vehicles', 'drivers', 'trips', 'fuel-logs', 'expenses', 'reports'],
};

export const VEHICLE_STATUSES = ['AVAILABLE', 'ON_TRIP', 'IN_SHOP', 'RETIRED'];
export const DRIVER_STATUSES = ['AVAILABLE', 'ON_TRIP', 'OFF_DUTY', 'SUSPENDED'];
export const TRIP_STATUSES = ['DRAFT', 'DISPATCHED', 'COMPLETED', 'CANCELLED'];
export const MAINTENANCE_STATUSES = ['ACTIVE', 'CLOSED'];
export const EXPENSE_TYPES = ['TOLL', 'MAINTENANCE', 'INSURANCE', 'REPAIR', 'PARKING', 'PERMIT', 'OTHER'];
export const VEHICLE_TYPES = ['VAN', 'TRUCK', 'BUS', 'CAR', 'BIKE', 'OTHER'];
export const LICENSE_CATEGORIES = ['LMV', 'HMV', 'HPMV', 'PSV', 'HMVTR'];
export const REGIONS = ['North', 'South', 'East', 'West', 'Central'];

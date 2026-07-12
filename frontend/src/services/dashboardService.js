import api from './api';

function currentRoles() {
  try {
    return JSON.parse(localStorage.getItem('transitops_user') || 'null')?.roles || [];
  } catch {
    return [];
  }
}

export const dashboardService = {
  getSummary: async () => {
    const roles = currentRoles();
    const canViewReports = roles.some((role) =>
      ['ADMIN', 'FLEET_MANAGER', 'FINANCIAL_ANALYST'].includes(role),
    );

    const [dashboard, report, trips] = await Promise.all([
      api.get('/dashboard'),
      canViewReports ? api.get('/reports/fleet') : Promise.resolve(null),
      api.get('/trips?size=5&sort=createdAt,desc'),
    ]);

    const monthlyCostTrend = report?.monthlyCostTrend || [];

    return {
      ...dashboard,
      fleetUtilization: Number(dashboard.fleetUtilizationPercent || 0),
      totalOperationalCost: Number(report?.totalOperationalCost || 0),
      vehicleStatusBreakdown: dashboard.vehicleStatusDistribution || {},
      tripStatusBreakdown: dashboard.tripStatusDistribution || {},
      driverStatusBreakdown: dashboard.driverStatusDistribution || {},
      monthlyOpCost: monthlyCostTrend.map((point) => ({
        month: point.month,
        cost: Number(point.totalCost || 0),
      })),
      monthlyFuelCost: monthlyCostTrend.map((point) => ({
        month: point.month,
        cost: Number(point.fuelCost || 0),
      })),
      recentTrips: (trips.content || []).map((trip) => ({
        ...trip,
        startDate: trip.dispatchedAt || trip.createdAt,
      })),
      canViewFinancials: canViewReports,
    };
  },
};

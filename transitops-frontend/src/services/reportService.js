import api from './api';

const mapVehicle = (vehicle) => ({
  ...vehicle,
  vehicleName: vehicle.nameModel,
  totalRevenue: Number(vehicle.revenue || 0),
  totalFuelCost: Number(vehicle.fuelCost || 0),
  totalMaintCost: Number(vehicle.maintenanceCost || 0),
  totalDistance: Number(vehicle.completedDistanceKm || 0),
  fuelEfficiency: Number(vehicle.fuelEfficiencyKmPerLiter || 0),
  roi: Number(vehicle.roiPercent || 0),
});

export const reportService = {
  getAnalytics: () =>
    api.get('/reports/fleet').then((response) => ({
      ...response,
      totalDistanceKm: Number(response.totalDistanceKm || 0),
      totalFuelLiters: Number(response.totalFuelLiters || 0),
      totalOperationalCost: Number(response.totalOperationalCost || 0),
      totalRevenue: Number(response.totalRevenue || 0),
      totalFuelCost: Number(response.totalFuelCost || 0),
      totalMaintCost: Number(response.totalMaintenanceCost || 0),
      avgFuelEfficiency: Number(response.averageFuelEfficiencyKmPerLiter || 0),
      vehicleAnalytics: (response.vehicles || []).map(mapVehicle),
    })),
  downloadCsv: () => api.get('/reports/export.csv', { responseType: 'blob' }),
  downloadPdf: () => api.get('/reports/export.pdf', { responseType: 'blob' }),
};

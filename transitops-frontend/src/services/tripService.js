import api from './api';

const fromApi = (trip) => ({
  ...trip,
  cargoWeight: Number(trip.cargoWeightKg),
  plannedDistance: Number(trip.plannedDistanceKm),
  actualDistance: Number(trip.actualDistanceKm || 0),
  vehicleName: trip.vehicleNameModel,
  startDate: trip.dispatchedAt || trip.createdAt,
});

const draftPayload = (data) => ({
  source: data.source,
  destination: data.destination,
  vehicleId: Number(data.vehicleId),
  driverId: Number(data.driverId),
  cargoWeightKg: Number(data.cargoWeight),
  plannedDistanceKm: Number(data.plannedDistance),
  notes: data.notes || null,
});

export const tripService = {
  getAll: () => api.get('/trips?size=200').then((response) => response.content.map(fromApi)),
  getById: (id) => api.get(`/trips/${id}`).then(fromApi),
  create: (data) => api.post('/trips', draftPayload(data)).then(fromApi),
  update: (id, data) => api.put(`/trips/${id}`, draftPayload(data)).then(fromApi),
  dispatch: (id) => api.post(`/trips/${id}/dispatch`).then(fromApi),
  complete: (id, data) => api.post(`/trips/${id}/complete`, {
    finalOdometerKm: Number(data.finalOdometerKm),
    fuelConsumedLiters: Number(data.fuelConsumedLiters || 0),
    fuelCost: Number(data.fuelCost || 0),
    revenue: Number(data.revenue || 0),
    notes: data.notes || null,
  }).then(fromApi),
  cancel: ({ id, reason }) => api.post(`/trips/${id}/cancel`, { reason }).then(fromApi),
  delete: (id) => api.delete(`/trips/${id}`),
};

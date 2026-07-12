import api from './api';

const fromApi = (log) => ({
  ...log,
  liters: Number(log.liters || 0),
  cost: Number(log.cost || 0),
  fuelDate: log.logDate,
  odometerReading: Number(log.odometerKm || 0),
  fuelStation: log.notes || '',
});

const createPayload = (data) => ({
  vehicleId: Number(data.vehicleId),
  tripId: data.tripId ? Number(data.tripId) : null,
  liters: Number(data.liters),
  cost: Number(data.cost),
  logDate: data.fuelDate,
  odometerKm: Number(data.odometerReading || 0),
  notes: data.fuelStation || null,
});

const updatePayload = (data) => ({
  liters: Number(data.liters),
  cost: Number(data.cost),
  logDate: data.fuelDate,
  odometerKm: Number(data.odometerReading || 0),
  notes: data.fuelStation || null,
});

export const fuelService = {
  getAll: () => api.get('/fuel-logs?size=200').then((response) => response.content.map(fromApi)),
  create: (data) => api.post('/fuel-logs', createPayload(data)).then(fromApi),
  update: (id, data) => api.put(`/fuel-logs/${id}`, updatePayload(data)).then(fromApi),
  delete: (id) => api.delete(`/fuel-logs/${id}`),
};

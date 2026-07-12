import api from './api';

const fromApi = (vehicle) => ({
  ...vehicle,
  vehicleName: vehicle.nameModel,
  maximumLoadCapacity: Number(vehicle.maxLoadCapacityKg),
  odometer: Number(vehicle.odometerKm),
  acquisitionCost: Number(vehicle.acquisitionCost),
});

const createPayload = (data) => ({
  registrationNumber: data.registrationNumber,
  nameModel: data.nameModel,
  type: data.type,
  region: data.region,
  maxLoadCapacityKg: Number(data.maximumLoadCapacity),
  odometerKm: Number(data.odometer || 0),
  acquisitionCost: Number(data.acquisitionCost),
});

const updatePayload = (data) => ({
  nameModel: data.nameModel,
  type: data.type,
  region: data.region,
  maxLoadCapacityKg: Number(data.maximumLoadCapacity),
  acquisitionCost: Number(data.acquisitionCost),
});

export const vehicleService = {
  getAll: () => api.get('/vehicles?size=200').then((response) => response.content.map(fromApi)),
  getById: (id) => api.get(`/vehicles/${id}`).then(fromApi),
  getAvailable: () => api.get('/vehicles/available').then((response) => response.map(fromApi)),
  create: (data) => api.post('/vehicles', createPayload(data)).then(fromApi),
  update: async (id, data) => {
    const updated = await api.put(`/vehicles/${id}`, updatePayload(data));
    if (data.status && data.status !== updated.status) {
      await api.patch(`/vehicles/${id}/status`, { status: data.status });
    }
    return fromApi(await api.get(`/vehicles/${id}`));
  },
  changeStatus: (id, status) => api.patch(`/vehicles/${id}/status`, { status }).then(fromApi),
  retire: (id) => api.post(`/vehicles/${id}/retire`).then(fromApi),
  delete: (id) => api.delete(`/vehicles/${id}`),
};

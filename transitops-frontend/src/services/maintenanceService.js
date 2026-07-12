import api from './api';

const fromApi = (record) => ({
  ...record,
  maintenanceType: record.serviceType,
  cost: Number(record.cost || 0),
  completionDate: record.endDate,
});

const createPayload = (data) => ({
  vehicleId: Number(data.vehicleId),
  serviceType: data.maintenanceType,
  description: data.description || null,
  startDate: data.startDate,
  estimatedOrInitialCost: Number(data.cost || 0),
  odometerAtService: data.odometerAtService === '' || data.odometerAtService == null
    ? null
    : Number(data.odometerAtService),
});

export const maintenanceService = {
  getAll: () => api.get('/maintenance?size=200').then((response) => response.content.map(fromApi)),
  getById: (id) => api.get(`/maintenance/${id}`).then(fromApi),
  create: (data) => api.post('/maintenance', createPayload(data)).then(fromApi),
  close: (id, data) => api.post(`/maintenance/${id}/close`, {
    endDate: data.endDate,
    finalCost: Number(data.finalCost),
    closingNotes: data.closingNotes || null,
  }).then(fromApi),
  delete: (id) => api.delete(`/maintenance/${id}`),
};

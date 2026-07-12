import api from './api';

const fromApi = (expense) => ({
  ...expense,
  amount: Number(expense.amount || 0),
  expenseType: expense.type,
});

const createPayload = (data) => ({
  vehicleId: Number(data.vehicleId),
  tripId: data.tripId ? Number(data.tripId) : null,
  type: data.expenseType,
  amount: Number(data.amount),
  expenseDate: data.expenseDate,
  description: data.description,
});

const updatePayload = (data) => ({
  type: data.expenseType,
  amount: Number(data.amount),
  expenseDate: data.expenseDate,
  description: data.description,
});

export const expenseService = {
  getAll: () => api.get('/expenses?size=200').then((response) => response.content.map(fromApi)),
  create: (data) => api.post('/expenses', createPayload(data)).then(fromApi),
  update: (id, data) => api.put(`/expenses/${id}`, updatePayload(data)).then(fromApi),
  delete: (id) => api.delete(`/expenses/${id}`),
};

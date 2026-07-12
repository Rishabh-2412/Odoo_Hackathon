import api from './api';

export const authService = {
  login: ({ email, password, deviceInfo }) => api.post('/auth/login', { email, password, deviceInfo }),
  signup: ({ name, email, password }) => api.post('/auth/signup', { name, email, password }),
  me: () => api.get('/auth/me'),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken, deviceInfo: navigator.userAgent }),
  logout: () => {
    const refreshToken = localStorage.getItem('transitops_refresh_token');
    return refreshToken
      ? api.post('/auth/logout', { refreshToken, allDevices: false })
      : Promise.resolve();
  },
};

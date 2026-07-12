import axios from 'axios';
import { toast } from 'sonner';
import { API_BASE_URL } from '../config';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

const PUBLIC_AUTH_PATHS = ['/auth/login', '/auth/signup', '/auth/refresh'];
let refreshRequest = null;

function isPublicAuthRequest(url = '') {
  return PUBLIC_AUTH_PATHS.some((path) => url.includes(path));
}

function clearSession() {
  ['transitops_token', 'transitops_refresh_token', 'transitops_user'].forEach((key) =>
    localStorage.removeItem(key),
  );
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('transitops_token');
  if (token && !isPublicAuthRequest(config.url)) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const original = error.config || {};
    const status = error.response?.status;
    const data = error.response?.data;
    const refreshToken = localStorage.getItem('transitops_refresh_token');

    const canRefresh =
      status === 401 &&
      !original._retry &&
      refreshToken &&
      !isPublicAuthRequest(original.url);

    if (canRefresh) {
      original._retry = true;
      try {
        refreshRequest ??= axios
          .post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
            deviceInfo: navigator.userAgent,
          })
          .finally(() => {
            refreshRequest = null;
          });

        const { data: refreshed } = await refreshRequest;
        localStorage.setItem('transitops_token', refreshed.accessToken);
        localStorage.setItem('transitops_refresh_token', refreshed.refreshToken);
        localStorage.setItem('transitops_user', JSON.stringify(refreshed.user));
        original.headers = original.headers || {};
        original.headers.Authorization = `Bearer ${refreshed.accessToken}`;
        return api(original);
      } catch {
        clearSession();
      }
    }

    if (status === 401 && !isPublicAuthRequest(original.url)) {
      clearSession();
      if (window.location.pathname !== '/login') window.location.assign('/login');
    }
    if (status === 403) toast.error('You do not have permission to perform this action.');
    if (status >= 500) toast.error('Server error. Please try again later.');

    return Promise.reject({
      status,
      fieldErrors: data?.validationErrors || data?.fieldErrors || {},
      message: data?.message || 'Request failed',
      response: error.response,
    });
  },
);

export default api;

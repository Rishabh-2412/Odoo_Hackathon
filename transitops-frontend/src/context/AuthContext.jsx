/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useState } from 'react';
import { authService } from '../services/authService';
import { ROLE_NAV_ACCESS } from '../config';

const AuthContext = createContext(null);
const STORAGE_TOKEN = 'transitops_token';
const STORAGE_REFRESH = 'transitops_refresh_token';
const STORAGE_USER = 'transitops_user';

function loadStoredSession() {
  try {
    const token = localStorage.getItem(STORAGE_TOKEN);
    const user = JSON.parse(localStorage.getItem(STORAGE_USER) || 'null');
    return token && user ? { token, user } : null;
  } catch {
    return null;
  }
}

function clearStoredSession() {
  localStorage.removeItem(STORAGE_TOKEN);
  localStorage.removeItem(STORAGE_REFRESH);
  localStorage.removeItem(STORAGE_USER);
}

export function AuthProvider({ children }) {
  const stored = loadStoredSession();
  const [user, setUser] = useState(stored?.user || null);
  const [token, setToken] = useState(stored?.token || null);
  const [loading, setLoading] = useState(false);

  const login = useCallback(async ({ email, password }) => {
    setLoading(true);
    try {
      const response = await authService.login({
        email,
        password,
        deviceInfo: navigator.userAgent,
      });

      localStorage.setItem(STORAGE_TOKEN, response.accessToken);
      localStorage.setItem(STORAGE_REFRESH, response.refreshToken);
      localStorage.setItem(STORAGE_USER, JSON.stringify(response.user));
      setToken(response.accessToken);
      setUser(response.user);
      return response;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      clearStoredSession();
      setToken(null);
      setUser(null);
    }
  }, []);

  const hasRole = useCallback(
    (...roles) => Boolean(user?.roles?.some((role) => roles.includes(role))),
    [user],
  );

  const canAccess = useCallback(
    (module) => Boolean(user?.roles?.some((role) => (ROLE_NAV_ACCESS[role] || []).includes(module))),
    [user],
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: Boolean(token && user),
        loading,
        login,
        logout,
        hasRole,
        canAccess,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}

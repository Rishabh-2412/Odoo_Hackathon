import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';

import LoginPage       from './pages/LoginPage';
import SignUpPage      from './pages/SignUpPage';
import AppLayout       from './layouts/AppLayout';
import DashboardPage   from './pages/DashboardPage';
import VehiclesPage    from './pages/VehiclesPage';
import DriversPage     from './pages/DriversPage';
import TripsPage       from './pages/TripsPage';
import MaintenancePage from './pages/MaintenancePage';
import FuelLogsPage    from './pages/FuelLogsPage';
import ExpensesPage    from './pages/ExpensesPage';
import ReportsPage     from './pages/ReportsPage';
import UsersRolesPage  from './pages/UsersRolesPage';
import AccessDeniedPage from './pages/AccessDeniedPage';
import NotFoundPage     from './pages/NotFoundPage';

import ProtectedRoute   from './components/ProtectedRoute';
import RoleGuard       from './components/RoleGuard';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignUpPage />} />

          {/* Protected Routes */}
          <Route path="/" element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/dashboard" replace />} />
            
            <Route path="dashboard" element={
              <RoleGuard module="dashboard">
                <DashboardPage />
              </RoleGuard>
            } />
            
            <Route path="vehicles" element={
              <RoleGuard module="vehicles">
                <VehiclesPage />
              </RoleGuard>
            } />
            
            <Route path="drivers" element={
              <RoleGuard module="drivers">
                <DriversPage />
              </RoleGuard>
            } />
            
            <Route path="trips" element={
              <RoleGuard module="trips">
                <TripsPage />
              </RoleGuard>
            } />
            
            <Route path="maintenance" element={
              <RoleGuard module="maintenance">
                <MaintenancePage />
              </RoleGuard>
            } />
            
            <Route path="fuel-logs" element={
              <RoleGuard module="fuel-logs">
                <FuelLogsPage />
              </RoleGuard>
            } />
            
            <Route path="expenses" element={
              <RoleGuard module="expenses">
                <ExpensesPage />
              </RoleGuard>
            } />
            
            <Route path="reports" element={
              <RoleGuard module="reports">
                <ReportsPage />
              </RoleGuard>
            } />
            
            <Route path="users-roles" element={
              <RoleGuard module="users-roles">
                <UsersRolesPage />
              </RoleGuard>
            } />

            <Route path="access-denied" element={<AccessDeniedPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
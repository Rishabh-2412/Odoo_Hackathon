import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "../components/guards/ProtectedRoute";
import LoginPage from "../pages/auth/LoginPage";
import DashboardPage from "../pages/dashboard/DashboardPage";
import AccessDeniedPage from "../pages/errors/AccessDeniedPage";
import NotFoundPage from "../pages/errors/NotFoundPage";

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route
          path="/access-denied"
          element={<AccessDeniedPage />}
        />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default AppRoutes;
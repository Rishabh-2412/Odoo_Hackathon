import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RoleGuard({ module, children }) {
  const { canAccess } = useAuth();
  if (!canAccess(module)) return <Navigate to="/access-denied" replace />;
  return children;
}
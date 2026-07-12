import { Link } from 'react-router-dom';
import { ShieldOff, Home } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function AccessDeniedPage() {
  const { user } = useAuth();
  return (
    <div className="min-h-screen bg-[#0d0d0d] flex items-center justify-center p-8">
      <div className="text-center max-w-md">
        <div className="inline-flex p-5 rounded-full bg-red-900/20 mb-6">
          <ShieldOff className="h-12 w-12 text-red-400" />
        </div>
        <h1 className="text-3xl font-bold text-[#f0f0f0] mb-2">Access Denied</h1>
        <p className="text-[#6b7280] mb-2">You don't have permission to view this page.</p>
        {user && <p className="text-xs text-[#4b5563] mb-8">Your role: <span className="text-[#9ca3af]">{(user.roles || []).join(', ').replace(/_/g,' ')}</span></p>}
        <Link to="/dashboard" className="btn-amber inline-flex">
          <Home className="h-4 w-4" /> Back to Dashboard
        </Link>
      </div>
    </div>
  );
}
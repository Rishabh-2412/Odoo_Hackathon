import { Link } from 'react-router-dom';
import { Compass, Home } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-[#0d0d0d] flex items-center justify-center p-8">
      <div className="text-center max-w-md">
        <div className="inline-flex p-5 rounded-full bg-[#141414] mb-6">
          <Compass className="h-12 w-12 text-[#d97706]" />
        </div>
        <h1 className="text-7xl font-black text-[#1f1f1f] mb-4">404</h1>
        <h2 className="text-xl font-bold text-[#f0f0f0] mb-2">Page Not Found</h2>
        <p className="text-[#6b7280] mb-8">The page you're looking for doesn't exist or has been moved.</p>
        <Link to="/dashboard" className="btn-amber inline-flex">
          <Home className="h-4 w-4" /> Back to Dashboard
        </Link>
      </div>
    </div>
  );
}
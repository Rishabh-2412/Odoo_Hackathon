import { ChevronRight, Home } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';

const LABELS = {
  dashboard:   'Dashboard',
  vehicles:    'Vehicles',
  drivers:     'Drivers',
  trips:       'Trips',
  maintenance: 'Maintenance',
  'fuel-logs': 'Fuel Logs',
  expenses:    'Expenses',
  reports:     'Reports',
  'users-roles':'Users & Roles',
  'access-denied':'Access Denied',
};

export default function Breadcrumbs() {
  const { pathname } = useLocation();
  const parts = pathname.split('/').filter(Boolean);

  return (
    <nav className="flex items-center gap-1.5 text-xs text-[#6b7280]">
      <Link to="/dashboard" className="hover:text-[#f0f0f0] transition-colors">
        <Home className="h-3.5 w-3.5" />
      </Link>
      {parts.map((part, i) => (
        <span key={part} className="flex items-center gap-1.5">
          <ChevronRight className="h-3 w-3" />
          {i === parts.length - 1
            ? <span className="text-[#d1d5db] font-medium">{LABELS[part] || part}</span>
            : <Link to={`/${parts.slice(0,i+1).join('/')}`} className="hover:text-[#f0f0f0] transition-colors">{LABELS[part] || part}</Link>
          }
        </span>
      ))}
    </nav>
  );
}

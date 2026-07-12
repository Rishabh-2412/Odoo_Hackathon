import { createElement } from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Truck, Users, Route, Wrench,
  Fuel, ReceiptText, BarChart3, ShieldCheck, X, Bus
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const ALL_NAV = [
  { key:'dashboard',   label:'Dashboard',     icon:LayoutDashboard, path:'/dashboard'   },
  { key:'vehicles',    label:'Fleet',          icon:Truck,           path:'/vehicles'    },
  { key:'drivers',     label:'Drivers',        icon:Users,           path:'/drivers'     },
  { key:'trips',       label:'Trips',          icon:Route,           path:'/trips'       },
  { key:'maintenance', label:'Maintenance',    icon:Wrench,          path:'/maintenance' },
  { key:'fuel-logs',   label:'Fuel Logs',      icon:Fuel,            path:'/fuel-logs'   },
  { key:'expenses',    label:'Expenses',       icon:ReceiptText,     path:'/expenses'    },
  { key:'reports',     label:'Reports',        icon:BarChart3,       path:'/reports'     },
  { key:'users-roles', label:'Users & Roles',  icon:ShieldCheck,     path:'/users-roles' },
];

export default function Sidebar({ isOpen, onClose }) {
  const { user, canAccess } = useAuth();
  const visibleNav = ALL_NAV.filter(n => canAccess(n.key));

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && <div className="fixed inset-0 z-30 bg-black/60 lg:hidden" onClick={onClose} />}

      <aside className={`sidebar w-64 ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}`}>
        {/* Logo */}
        <div className="sidebar-logo">
          <div className="w-8 h-8 rounded-lg bg-[#d97706] flex items-center justify-center shrink-0">
            <Bus className="h-5 w-5 text-white" />
          </div>
          <div className="min-w-0">
            <p className="text-sm font-bold text-[#f0f0f0] leading-tight">TransitOps</p>
            <p className="text-[10px] text-[#6b7280] leading-tight truncate">Smart Transport Platform</p>
          </div>
          <button className="ml-auto lg:hidden text-[#6b7280] hover:text-[#f0f0f0]" onClick={onClose}>
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 py-4 overflow-y-auto">
          <p className="section-title px-6 pt-2">Navigation</p>
          <div className="space-y-0.5 px-2">
            {visibleNav.map(({ key, label, icon, path }) => (
              <NavLink
                key={key}
                to={path}
                onClick={onClose}
                className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
              >
                {createElement(icon, { className: 'h-4 w-4 shrink-0' })}
                <span className="truncate">{label}</span>
              </NavLink>
            ))}
          </div>
        </nav>

        {/* User footer */}
        {user && (
          <div className="px-4 py-4 border-t border-[#2a2a2a]">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-[#d97706]/20 border border-[#d97706]/30 flex items-center justify-center text-xs font-bold text-[#d97706]">
                {user.name.charAt(0)}
              </div>
              <div className="min-w-0">
                <p className="text-xs font-semibold text-[#d1d5db] truncate">{user.name}</p>
                <p className="text-[10px] text-[#6b7280] truncate">{(user.roles || []).join(', ').replace(/_/g,' ')}</p>
              </div>
            </div>
          </div>
        )}
      </aside>
    </>
  );
}

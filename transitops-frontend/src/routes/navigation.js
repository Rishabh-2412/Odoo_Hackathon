import {
  BarChart3,
  Car,
  ClipboardList,
  Contact,
  FileBarChart,
  LayoutDashboard,
  Receipt,
  ShieldCheck,
  Wrench,
} from 'lucide-react';

const allRoles = ['ADMIN', 'FLEET_MANAGER', 'DRIVER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST'];

export const navItems = [
  { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: allRoles },
  { label: 'Vehicles', path: '/vehicles', icon: Car, roles: allRoles },
  { label: 'Drivers', path: '/drivers', icon: Contact, roles: ['ADMIN', 'FLEET_MANAGER', 'DRIVER', 'SAFETY_OFFICER'] },
  { label: 'Trips', path: '/trips', icon: ClipboardList, roles: ['ADMIN', 'FLEET_MANAGER', 'DRIVER'] },
  { label: 'Maintenance', path: '/maintenance', icon: Wrench, roles: ['ADMIN', 'FLEET_MANAGER'] },
  { label: 'Fuel Logs', path: '/fuel-logs', icon: BarChart3, roles: ['ADMIN', 'FLEET_MANAGER', 'FINANCIAL_ANALYST'] },
  { label: 'Expenses', path: '/expenses', icon: Receipt, roles: ['ADMIN', 'FLEET_MANAGER', 'FINANCIAL_ANALYST'] },
  { label: 'Reports', path: '/reports', icon: FileBarChart, roles: ['ADMIN', 'FLEET_MANAGER', 'FINANCIAL_ANALYST'] },
  { label: 'Users & Roles', path: '/users-roles', icon: ShieldCheck, roles: ['ADMIN'] },
];

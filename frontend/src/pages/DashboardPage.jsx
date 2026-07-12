import { Truck, Users, Route, Wrench, DollarSign, Activity, TrendingUp, Clock } from 'lucide-react';
import { BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { useDashboard } from '../hooks/useDashboard';
import { useVehicles } from '../hooks/useVehicles';
import KPICard from '../components/KpiCard';
import ChartCard from '../components/ChartCard';
import StatusBadge from '../components/StatusBadge';
import LoadingSkeleton, { SkeletonCard } from '../components/LoadingSkeleton';
import PageHeader from '../components/PageHeader';
import { formatCurrency, formatDate } from '../lib/utils';
import { useAuth } from '../context/AuthContext';

const DONUT_COLORS = { AVAILABLE:'#10b981', ON_TRIP:'#3b82f6', IN_SHOP:'#f59e0b', RETIRED:'#ef4444' };
const CUSTOM_TOOLTIP = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg p-3 text-xs">
      <p className="text-[#9ca3af] mb-1">{label}</p>
      {payload.map((p,i) => <p key={i} style={{color:p.color}} className="font-semibold">{p.name}: {p.value?.toLocaleString()}</p>)}
    </div>
  );
};

export default function DashboardPage() {
  const { data: summary, isLoading, error } = useDashboard();
  const { data: vehicles } = useVehicles();
  const { user, hasRole } = useAuth();

  if (isLoading) return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" description="Fleet operations overview" />
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {Array.from({length:8}).map((_,i) => <SkeletonCard key={i} />)}
      </div>
    </div>
  );

  if (error) return <div className="text-red-400 p-8">Failed to load dashboard data.</div>;

  const {
    activeVehicles, availableVehicles, vehiclesInMaintenance,
    activeTrips, pendingTrips, driversOnDuty, fleetUtilization, totalOperationalCost,
    vehicleStatusBreakdown, monthlyOpCost, monthlyFuelCost, recentTrips, canViewFinancials
  } = summary;

  const isDriverOnly = hasRole('DRIVER') && !hasRole('ADMIN', 'FLEET_MANAGER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST');
  const dashboardDescription = isDriverOnly
    ? 'Driver workspace for fleet availability and trip operations'
    : `Real-time operations overview for ${(user?.roles || []).join(', ').replace(/_/g, ' ')}`;

  const donutData = Object.entries(vehicleStatusBreakdown).map(([name, value]) => ({ name, value }));

  const statusBar = [
    { label:'Available', count: vehicleStatusBreakdown.AVAILABLE, total: activeVehicles, color:'bg-emerald-500' },
    { label:'On Trip',   count: vehicleStatusBreakdown.ON_TRIP,   total: activeVehicles, color:'bg-blue-500'    },
    { label:'In Shop',   count: vehicleStatusBreakdown.IN_SHOP,   total: activeVehicles, color:'bg-amber-500'   },
    { label:'Retired',   count: vehicleStatusBreakdown.RETIRED,   total: (vehicles?.length||1), color:'bg-red-500' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" description={dashboardDescription} />

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <KPICard label="Active Vehicles"     value={activeVehicles}      accent="blue"  icon={Truck}       subtitle={`${availableVehicles} available`} />
        <KPICard label="In Maintenance"      value={vehiclesInMaintenance}accent="amber" icon={Wrench}      subtitle="vehicles in shop" />
        <KPICard label="Active Trips"        value={activeTrips}          accent="green" icon={Route}       subtitle={`${pendingTrips} pending`} />
        <KPICard label="Drivers on Duty"     value={driversOnDuty}        accent="blue"  icon={Users}       subtitle="currently on trip" />
        <KPICard label="Fleet Utilization"   value={`${fleetUtilization}%`}accent="green" icon={Activity}  subtitle="active fleet %" />
        <KPICard label="Pending Trips"       value={pendingTrips}         accent="amber" icon={Clock}       subtitle="awaiting dispatch" />
        {canViewFinancials && (
          <KPICard label="Operational Cost" value={formatCurrency(totalOperationalCost)} accent="red" icon={DollarSign} subtitle="fuel + maintenance + expenses" />
        )}
        <KPICard label="Utilization Trend"   value={`${fleetUtilization}%`}accent="gray" icon={TrendingUp}  subtitle="this month" />
      </div>

      {/* Charts row 1 */}
      <div className="grid lg:grid-cols-3 gap-6">
        <ChartCard title="Fleet Status" subtitle="Current vehicle distribution">
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={donutData} cx="50%" cy="50%" innerRadius={55} outerRadius={85} paddingAngle={3} dataKey="value">
                {donutData.map(e => <Cell key={e.name} fill={DONUT_COLORS[e.name]} />)}
              </Pie>
              <Tooltip content={<CUSTOM_TOOLTIP />} />
              <Legend formatter={v=><span className="text-xs text-[#9ca3af]">{v}</span>} />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>

        {canViewFinancials && (
          <ChartCard title="Monthly Operational Cost" subtitle="Last 6 months" className="lg:col-span-2">
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={monthlyOpCost} barSize={28}>
                <XAxis dataKey="month" tick={{fontSize:11, fill:'#6b7280'}} axisLine={false} tickLine={false} />
                <YAxis tick={{fontSize:11, fill:'#6b7280'}} axisLine={false} tickLine={false} tickFormatter={v=>`₹${(v/1000).toFixed(0)}k`} />
                <Tooltip content={<CUSTOM_TOOLTIP />} />
                <Bar dataKey="cost" fill="#d97706" radius={[4,4,0,0]} name="Cost (₹)" />
              </BarChart>
            </ResponsiveContainer>
          </ChartCard>
        )}
      </div>

      {/* Charts row 2 + vehicle status panel */}
      <div className="grid lg:grid-cols-3 gap-6">
        {canViewFinancials && (
          <ChartCard title="Monthly Fuel Cost" subtitle="Fuel expenditure trend" className="lg:col-span-2">
            <ResponsiveContainer width="100%" height={220}>
              <LineChart data={monthlyFuelCost}>
                <XAxis dataKey="month" tick={{fontSize:11, fill:'#6b7280'}} axisLine={false} tickLine={false} />
                <YAxis tick={{fontSize:11, fill:'#6b7280'}} axisLine={false} tickLine={false} tickFormatter={v=>`₹${(v/1000).toFixed(0)}k`} />
                <Tooltip content={<CUSTOM_TOOLTIP />} />
                <Line type="monotone" dataKey="cost" stroke="#3b82f6" strokeWidth={2.5} dot={{fill:'#3b82f6',r:4}} name="Fuel Cost (₹)" />
              </LineChart>
            </ResponsiveContainer>
          </ChartCard>
        )}

        {/* Vehicle status meter */}
        <div className="panel p-5 space-y-4">
          <h3 className="text-sm font-semibold text-[#f0f0f0]">Fleet Status Meters</h3>
          {statusBar.map(s => (
            <div key={s.label}>
              <div className="flex items-center justify-between mb-1.5">
                <span className="text-xs text-[#9ca3af]">{s.label}</span>
                <span className="text-xs font-semibold text-[#d1d5db]">{s.count} <span className="text-[#4b5563]">/ {s.total}</span></span>
              </div>
              <div className="h-2 bg-[#1f1f1f] rounded-full overflow-hidden">
                <div className={`h-full ${s.color} rounded-full transition-all duration-500`} style={{width:`${(s.count/Math.max(s.total,1))*100}%`}} />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Trips */}
      <div className="panel overflow-hidden">
        <div className="px-5 py-4 border-b border-[#2a2a2a] flex items-center justify-between">
          <h3 className="text-sm font-semibold text-[#f0f0f0]">Recent Trips</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="dark-table">
            <thead>
              <tr>
                <th>ID</th><th>Route</th><th>Start Date</th><th>Revenue</th><th>Status</th>
              </tr>
            </thead>
            <tbody>
              {recentTrips?.map(t => (
                <tr key={t.id}>
                  <td className="font-mono text-xs text-[#6b7280]">#{t.id}</td>
                  <td><span className="font-medium text-[#d1d5db]">{t.source}</span> → {t.destination}</td>
                  <td className="text-[#9ca3af]">{formatDate(t.startDate)}</td>
                  <td className="text-emerald-400 font-semibold">{t.revenue ? formatCurrency(t.revenue) : '—'}</td>
                  <td><StatusBadge status={t.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
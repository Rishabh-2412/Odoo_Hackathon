import { useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { useReports } from '../hooks/useReports';
import PageHeader from '../components/PageHeader';
import KPICard from '../components/KpiCard';
import ChartCard from '../components/ChartCard';
import { SkeletonCard } from '../components/LoadingSkeleton';
import { formatCurrency, exportToCSV } from '../lib/utils';
import { Download, Printer, TrendingUp, DollarSign, Fuel, Wrench } from 'lucide-react';

const CUSTOM_TOOLTIP = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg p-3 text-xs">
      <p className="text-[#9ca3af] mb-1">{label}</p>
      {payload.map((p,i) => <p key={i} style={{color:p.color}} className="font-semibold">{p.name}: {p.value?.toLocaleString?.()}</p>)}
    </div>
  );
};

const ROI_COLOR = (roi) => roi >= 10 ? '#10b981' : roi >= 0 ? '#f59e0b' : '#ef4444';

export default function ReportsPage() {
  const { data, isLoading } = useReports();
  const [regionFilter, setRegionFilter] = useState('');
  const [vehicleFilter, setVehicleFilter] = useState('');

  if (isLoading) return (
    <div className="space-y-6">
      <PageHeader title="Reports & Analytics"/>
      <div className="grid grid-cols-4 gap-4">{Array.from({length:4}).map((_,i)=><SkeletonCard key={i}/>)}</div>
    </div>
  );

  const analytics = data?.vehicleAnalytics || [];
  const filtered = analytics.filter(v =>
    (!regionFilter  || v.region === regionFilter) &&
    (!vehicleFilter || v.vehicleId === Number(vehicleFilter))
  );

  const roiSorted = [...filtered].sort((a,b)=>b.roi-a.roi);

  return (
    <div className="space-y-6 print:space-y-4">
      <PageHeader
        title="Reports & Analytics"
        description="Vehicle ROI, fleet utilization, cost breakdown"
        actions={
          <div className="flex gap-2 no-print">
            <button onClick={() => window.print()} className="btn-ghost"><Printer className="h-4 w-4"/>Print</button>
            <button onClick={() => exportToCSV(analytics,'analytics')} className="btn-ghost"><Download className="h-4 w-4"/>Export</button>
          </div>
        }
      />

      {/* Filters */}
      <div className="flex gap-3 no-print">
        <select className="input-dark w-40" value={regionFilter} onChange={e=>setRegionFilter(e.target.value)}>
          <option value="">All Regions</option>
          {['North','South','East','West','Central'].map(r=><option key={r}>{r}</option>)}
        </select>
        <select className="input-dark w-60" value={vehicleFilter} onChange={e=>setVehicleFilter(e.target.value)}>
          <option value="">All Vehicles</option>
          {analytics.map(v=><option key={v.vehicleId} value={v.vehicleId}>{v.registrationNumber} — {v.vehicleName}</option>)}
        </select>
      </div>

      {/* KPI summary cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <KPICard label="Total Revenue"       value={formatCurrency(data?.totalRevenue||0)}    accent="green" icon={TrendingUp} />
        <KPICard label="Total Fuel Cost"     value={formatCurrency(data?.totalFuelCost||0)}    accent="blue"  icon={Fuel}      />
        <KPICard label="Total Maint. Cost"   value={formatCurrency(data?.totalMaintCost||0)}   accent="amber" icon={Wrench}    />
        <KPICard label="Avg Fuel Efficiency" value={`${data?.avgFuelEfficiency||0} km/L`}      accent="gray"  icon={DollarSign}/>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Revenue by vehicle */}
        <ChartCard title="Revenue by Vehicle" subtitle="Completed trips revenue">
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={roiSorted.slice(0,6)} layout="vertical" barSize={18}>
              <XAxis type="number" tick={{fontSize:10,fill:'#6b7280'}} axisLine={false} tickLine={false} tickFormatter={v=>`₹${(v/1000).toFixed(0)}k`}/>
              <YAxis dataKey="registrationNumber" type="category" tick={{fontSize:10,fill:'#9ca3af'}} axisLine={false} tickLine={false} width={90}/>
              <Tooltip content={<CUSTOM_TOOLTIP/>}/>
              <Bar dataKey="totalRevenue" name="Revenue (₹)" radius={[0,4,4,0]}>
                {roiSorted.slice(0,6).map((v,i)=><Cell key={i} fill="#d97706"/>)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* ROI ranking */}
        <div className="panel p-5">
          <h3 className="text-sm font-semibold text-[#f0f0f0] mb-4">Vehicle ROI Ranking</h3>
          <div className="space-y-3">
            {roiSorted.slice(0,7).map((v,i) => (
              <div key={v.vehicleId} className="flex items-center gap-3">
                <span className="text-xs font-bold text-[#4b5563] w-5">#{i+1}</span>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-medium text-[#d1d5db] truncate">{v.registrationNumber}</span>
                    <span className="text-xs font-bold ml-2" style={{color:ROI_COLOR(v.roi)}}>{v.roi.toFixed(1)}%</span>
                  </div>
                  <div className="h-1.5 bg-[#1f1f1f] rounded-full overflow-hidden">
                    <div className="h-full rounded-full transition-all" style={{width:`${Math.min(Math.max(v.roi,0),100)}%`, background:ROI_COLOR(v.roi)}}/>
                  </div>
                </div>
              </div>
            ))}
          </div>
          <p className="text-[10px] text-[#4b5563] mt-4">ROI = (Revenue − Maint − Fuel) / Acquisition Cost × 100</p>
        </div>
      </div>

      {/* Detailed table */}
      <div className="panel overflow-hidden">
        <div className="px-5 py-4 border-b border-[#2a2a2a]">
          <h3 className="text-sm font-semibold text-[#f0f0f0]">Vehicle Analytics Detail</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="dark-table">
            <thead>
              <tr>
                <th>Vehicle</th><th>Revenue</th><th>Fuel Cost</th>
                <th>Maint. Cost</th><th>Distance</th><th>Fuel Eff.</th><th>ROI</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(v=>(
                <tr key={v.vehicleId}>
                  <td>
                    <p className="font-medium text-[#d1d5db] text-sm">{v.vehicleName}</p>
                    <p className="text-xs font-mono text-[#d97706]">{v.registrationNumber}</p>
                  </td>
                  <td className="text-emerald-400 font-semibold">{formatCurrency(v.totalRevenue)}</td>
                  <td className="text-blue-400">{formatCurrency(v.totalFuelCost)}</td>
                  <td className="text-amber-400">{formatCurrency(v.totalMaintCost)}</td>
                  <td>{v.totalDistance.toLocaleString()} km</td>
                  <td>{v.fuelEfficiency > 0 ? `${v.fuelEfficiency} km/L` : '—'}</td>
                  <td><span className="font-bold" style={{color:ROI_COLOR(v.roi)}}>{v.roi.toFixed(1)}%</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
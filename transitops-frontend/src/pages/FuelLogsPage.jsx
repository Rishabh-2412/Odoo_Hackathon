import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { useFuelLogs, useCreateFuelLog, useUpdateFuelLog, useDeleteFuelLog } from '../hooks/useFuelLogs';
import { useVehicles } from '../hooks/useVehicles';
import DataTable from '../components/DataTable';
import PageHeader from '../components/PageHeader';
import FormDialog from '../components/FormDialog';
import ConfirmDialog from '../components/ConfirmDialog';
import { fuelLogSchema } from '../schemas';
import { formatCurrency, formatDate, exportToCSV } from '../lib/utils';
import { Download } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const FIELD = ({ label, error, children }) => (
  <div>
    <label className="label-dark">{label}</label>
    {children}
    {error && <p className="text-xs text-red-400 mt-1">{error.message}</p>}
  </div>
);

function FuelLogForm({ vehicles, defaultValues, onSubmit, isEdit = false }) {
  const { register, handleSubmit, formState:{errors} } = useForm({
    resolver: zodResolver(fuelLogSchema),
    defaultValues: defaultValues || {},
  });
  return (
    <form id="fuel-form" onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
      <FIELD label="Vehicle *" error={errors.vehicleId}>
        {isEdit ? (
          <>
            <input type="hidden" {...register('vehicleId')} />
            <div className="input-dark bg-[#141414] text-[#9ca3af] cursor-not-allowed">
              {vehicles?.find(v => v.id === Number(defaultValues?.vehicleId))?.registrationNumber || `Vehicle #${defaultValues?.vehicleId}`}
            </div>
          </>
        ) : (
          <select {...register('vehicleId')} className="input-dark">
            <option value="">Select vehicle</option>
            {vehicles?.map(v=><option key={v.id} value={v.id}>{v.registrationNumber} — {v.vehicleName}</option>)}
          </select>
        )}
      </FIELD>
      <FIELD label="Station / Notes" error={errors.fuelStation}>
        <input {...register('fuelStation')} className="input-dark" placeholder="Indian Oil - NH48"/>
      </FIELD>
      <FIELD label="Liters *" error={errors.liters}>
        <input {...register('liters')} type="number" step="0.1" className="input-dark" placeholder="50"/>
      </FIELD>
      <FIELD label="Cost (₹) *" error={errors.cost}>
        <input {...register('cost')} type="number" className="input-dark" placeholder="5500"/>
      </FIELD>
      <FIELD label="Odometer Reading (km) *" error={errors.odometerReading}>
        <input {...register('odometerReading')} type="number" className="input-dark" placeholder="85420"/>
      </FIELD>
      <FIELD label="Fuel Date *" error={errors.fuelDate}>
        <input {...register('fuelDate')} type="date" className="input-dark"/>
      </FIELD>
    </form>
  );
}

export default function FuelLogsPage() {
  const { hasRole } = useAuth();
  const canCreate = hasRole('ADMIN', 'FLEET_MANAGER', 'FINANCIAL_ANALYST');
  const canEdit = hasRole('ADMIN', 'FINANCIAL_ANALYST');
  const { data, isLoading, error } = useFuelLogs();
  const { data: vehicles } = useVehicles();
  const createMutation = useCreateFuelLog();
  const updateMutation = useUpdateFuelLog();
  const deleteMutation = useDeleteFuelLog();

  const [dialog,  setDialog]  = useState(null);
  const [editing, setEditing] = useState(null);
  const [deleting,setDeleting]= useState(null);
  const [search,  setSearch]  = useState('');

  const totalFuel = (data||[]).reduce((s,f)=>s+f.cost,0);
  const totalLiters= (data||[]).reduce((s,f)=>s+f.liters,0);

  const columns = [
    { key:'vehicleId',      label:'Vehicle',   render: (v) => { const veh=vehicles?.find(x=>x.id===v); return veh?`${veh.registrationNumber}`:`#${v}`; } },
    { key:'fuelStation',    label:'Station',   render: v => <span className="text-xs">{v}</span> },
    { key:'liters',         label:'Liters',    render: v => `${v} L` },
    { key:'cost',           label:'Cost',      render: v => formatCurrency(v) },
    { key:'odometerReading',label:'Odometer',  render: v => `${v?.toLocaleString()} km` },
    { key:'fuelDate',       label:'Date',      render: v => formatDate(v) },
    { key:'cost',           label:'Rate (₹/L)',render: (v,r) => r.liters>0 ? `₹${(v/r.liters).toFixed(1)}/L` : '—', sortable:false },
  ];

  const handleSubmit = async (data) => {
    if(dialog==='edit') await updateMutation.mutateAsync({id:editing.id,data});
    else                await createMutation.mutateAsync(data);
    setDialog(null); setEditing(null);
  };

  return (
    <div className="space-y-5">
      <PageHeader
        title="Fuel Logs"
        description="Track all fuel fill-ups and consumption"
        actions={
          <div className="flex gap-2">
            <button onClick={()=>exportToCSV(data,'fuel-logs')} className="btn-ghost"><Download className="h-4 w-4"/>Export</button>
            {canCreate && <button onClick={()=>setDialog('create')} className="btn-amber"><Plus className="h-4 w-4"/>Log Fuel</button>}
          </div>
        }
      />

      {/* Summary bar */}
      <div className="grid grid-cols-3 gap-4">
        <div className="panel p-4"><p className="label-dark">Total Records</p><p className="text-2xl font-bold text-[#f59e0b]">{data?.length||0}</p></div>
        <div className="panel p-4"><p className="label-dark">Total Liters</p><p className="text-2xl font-bold text-blue-400">{totalLiters.toLocaleString()} L</p></div>
        <div className="panel p-4"><p className="label-dark">Total Fuel Cost</p><p className="text-2xl font-bold text-red-400">{formatCurrency(totalFuel)}</p></div>
      </div>

      <input className="search-input" placeholder="Search station…" value={search} onChange={e=>setSearch(e.target.value)}/>

      <div className="panel overflow-hidden">
        <DataTable
          columns={columns}
          data={data||[]}
          loading={isLoading}
          error={error}
          searchKeys={['fuelStation']}
          searchValue={search}
          emptyTitle="No fuel logs"
          emptyDescription="Log your first fuel fill-up."
          actions={canEdit ? row=>(
            <div className="flex gap-2">
              <button onClick={()=>{setEditing(row);setDialog('edit');}} className="p-1.5 rounded text-[#6b7280] hover:text-[#d97706] hover:bg-[#1f1f1f]"><Pencil className="h-3.5 w-3.5"/></button>
              <button onClick={()=>setDeleting(row)} className="p-1.5 rounded text-[#6b7280] hover:text-red-400 hover:bg-red-900/10"><Trash2 className="h-3.5 w-3.5"/></button>
            </div>
          ) : undefined}
        />
      </div>

      <FormDialog open={!!dialog} title={dialog==='edit'?'Edit Fuel Log':'Log Fuel Fill-up'} onClose={()=>{setDialog(null);setEditing(null);}}>
        <FuelLogForm vehicles={vehicles} defaultValues={editing} onSubmit={handleSubmit} isEdit={dialog === 'edit'}/>
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={()=>{setDialog(null);setEditing(null);}}>Cancel</button>
          <button type="submit" form="fuel-form" className="btn-amber" disabled={createMutation.isPending||updateMutation.isPending}>
            {createMutation.isPending||updateMutation.isPending?'Saving…':'Save Log'}
          </button>
        </div>
      </FormDialog>

      <ConfirmDialog
        open={!!deleting}
        title="Delete Fuel Log"
        description="Remove this fuel log permanently?"
        confirmLabel="Delete"
        onConfirm={async()=>{await deleteMutation.mutateAsync(deleting.id);setDeleting(null);}}
        onCancel={()=>setDeleting(null)}
        loading={deleteMutation.isPending}
      />
    </div>
  );
}
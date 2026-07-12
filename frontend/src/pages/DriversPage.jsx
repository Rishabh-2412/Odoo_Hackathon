import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Plus, Pencil, Trash2, Download, AlertTriangle } from 'lucide-react';
import { useDrivers, useCreateDriver, useUpdateDriver, useDeleteDriver } from '../hooks/useDrivers';
import { useAuth } from '../context/AuthContext';
import DataTable from '../components/DataTable';
import StatusBadge from '../components/StatusBadge';
import PageHeader from '../components/PageHeader';
import FormDialog from '../components/FormDialog';
import ConfirmDialog from '../components/ConfirmDialog';
import { driverSchema } from '../schemas';
import { exportToCSV, getLicenseStatus, getDaysUntilExpiry, formatDate } from '../lib/utils';
import { LICENSE_CATEGORIES, REGIONS, DRIVER_STATUSES } from '../config';

const FIELD = ({ label, error, children }) => (
  <div>
    <label className="label-dark">{label}</label>
    {children}
    {error && <p className="text-xs text-red-400 mt-1">{error.message}</p>}
  </div>
);

function LicenseIndicator({ expiryDate }) {
  const status = getLicenseStatus(expiryDate);
  const days   = getDaysUntilExpiry(expiryDate);
  if (status === 'expired') return <span className="text-xs text-red-400 font-semibold flex items-center gap-1"><AlertTriangle className="h-3 w-3"/> Expired</span>;
  if (status === 'expiring') return <span className="text-xs text-amber-400 font-semibold">{days}d left</span>;
  return <span className="text-xs text-emerald-400">{formatDate(expiryDate)}</span>;
}

function DriverForm({ defaultValues, onSubmit, serverErrors = {}, isEdit = false }) {
  const { register, handleSubmit, formState:{errors} } = useForm({
    resolver: zodResolver(driverSchema),
    defaultValues: defaultValues || { status:'AVAILABLE', safetyScore:80 },
  });
  const allErrors = { ...errors };
  Object.keys(serverErrors).forEach(k => { if(!allErrors[k]) allErrors[k] = { message: serverErrors[k] }; });

  return (
    <form id="driver-form" onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
      <FIELD label="Full Name *" error={allErrors.name}>
        <input {...register('name')} className="input-dark" placeholder="Rajan Kumar" />
      </FIELD>
      <FIELD label="Contact Number *" error={allErrors.contactNumber}>
        <input {...register('contactNumber')} className="input-dark" placeholder="9876543210" />
      </FIELD>
      <FIELD label="Email" error={allErrors.email}>
        <input {...register('email')} type="email" className="input-dark" placeholder="driver@example.com" />
      </FIELD>
      <FIELD label="License Number *" error={allErrors.licenseNumber}>
        <input {...register('licenseNumber')} className="input-dark" placeholder="TN0120191234" readOnly={isEdit} />
      </FIELD>
      <FIELD label="License Category *" error={allErrors.licenseCategory}>
        <select {...register('licenseCategory')} className="input-dark">
          <option value="">Select</option>
          {LICENSE_CATEGORIES.map(c => <option key={c}>{c}</option>)}
        </select>
      </FIELD>
      <FIELD label="License Expiry Date *" error={allErrors.licenseExpiryDate}>
        <input {...register('licenseExpiryDate')} type="date" className="input-dark" />
      </FIELD>
      <FIELD label="Safety Score (0–100) *" error={allErrors.safetyScore}>
        <input {...register('safetyScore')} type="number" min="0" max="100" className="input-dark" />
      </FIELD>
      <FIELD label="Region *" error={allErrors.region}>
        <select {...register('region')} className="input-dark">
          <option value="">Select</option>
          {REGIONS.map(r => <option key={r}>{r}</option>)}
        </select>
      </FIELD>
      {isEdit ? (
        <FIELD label="Status" error={allErrors.status}>
          <select {...register('status')} className="input-dark">
            <option value="AVAILABLE">AVAILABLE</option>
            <option value="OFF_DUTY">OFF_DUTY</option>
            <option value="SUSPENDED">SUSPENDED</option>
          </select>
          <p className="text-xs text-[#6b7280] mt-1">
            ON_TRIP is controlled automatically by trip dispatch and completion.
          </p>
        </FIELD>
      ) : (
        <div className="col-span-2 rounded border border-[#2a2a2a] bg-[#171717] px-3 py-2 text-xs text-[#9ca3af]">
          A new driver is created as <strong className="text-emerald-400">AVAILABLE</strong>. An expired licence makes the driver <strong className="text-amber-400">OFF_DUTY</strong> automatically.
        </div>
      )}
    </form>
  );
}

export default function DriversPage() {
  const { hasRole } = useAuth();
  const canManage = hasRole('ADMIN', 'SAFETY_OFFICER');
  const canDelete = hasRole('ADMIN');

  const { data, isLoading, error } = useDrivers();
  const createMutation = useCreateDriver();
  const updateMutation = useUpdateDriver();
  const deleteMutation = useDeleteDriver();

  const [dialog,  setDialog]  = useState(null);
  const [editing, setEditing] = useState(null);
  const [deleting,setDeleting]= useState(null);
  const [serverErrors, setServerErrors] = useState({});
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const filtered = (data || []).filter(v => !statusFilter || v.status === statusFilter);

  const openCreate = () => { setEditing(null); setServerErrors({}); setDialog('create'); };
  const openEdit   = (v) => { setEditing(v);  setServerErrors({}); setDialog('edit');   };
  const closeDialog= () => setDialog(null);

  const handleSubmit = async (data) => {
    setServerErrors({});
    try {
      if (dialog === 'edit') await updateMutation.mutateAsync({ id: editing.id, data });
      else                   await createMutation.mutateAsync(data);
      closeDialog();
    } catch (err) {
      if (err.fieldErrors) setServerErrors(err.fieldErrors);
    }
  };

  const columns = [
    { key:'name',              label:'Driver',      render: (v,r) => (
      <div>
        <p className="font-medium text-[#d1d5db]">{v}</p>
        <p className="text-xs text-[#6b7280]">{r.contactNumber}</p>
      </div>
    )},
    { key:'licenseNumber',     label:'License',     render: v => <span className="font-mono text-xs">{v}</span> },
    { key:'licenseCategory',   label:'Category',    render: v => <span className="text-xs text-[#9ca3af]">{v}</span> },
    { key:'licenseExpiryDate', label:'Expiry',      render: (v) => <LicenseIndicator expiryDate={v} /> },
    { key:'safetyScore',       label:'Safety Score',render: v => (
      <span className={`font-bold text-sm ${v<60?'text-red-400':v<75?'text-amber-400':'text-emerald-400'}`}>{v}</span>
    )},
    { key:'region',            label:'Region',      render: v => <span className="text-xs text-[#9ca3af]">{v}</span> },
    { key:'status',            label:'Status',      render: v => <StatusBadge status={v} /> },
  ];

  return (
    <div className="space-y-5">
      <PageHeader
        title="Drivers"
        description={`${data?.length || 0} registered drivers`}
        actions={
          <div className="flex gap-2">
            <button onClick={() => exportToCSV(data,'drivers')} className="btn-ghost">
              <Download className="h-4 w-4" /> Export
            </button>
            {canManage && (
              <button onClick={openCreate} className="btn-amber">
                <Plus className="h-4 w-4" /> Add Driver
              </button>
            )}
          </div>
        }
      />

      <div className="flex flex-wrap gap-3">
        <input className="search-input" placeholder="Search name, license…" value={search} onChange={e=>setSearch(e.target.value)} />
        <select className="input-dark w-40" value={statusFilter} onChange={e=>setStatusFilter(e.target.value)}>
          <option value="">All Statuses</option>
          {DRIVER_STATUSES.map(s=><option key={s}>{s}</option>)}
        </select>
      </div>

      <div className="panel overflow-hidden">
        <DataTable
          columns={columns}
          data={filtered}
          loading={isLoading}
          error={error}
          searchKeys={['name','licenseNumber']}
          searchValue={search}
          emptyTitle="No drivers found"
          emptyDescription="Register your first driver."
          actions={canManage ? row => (
            <div className="flex items-center justify-end gap-2">
              {row.status !== 'ON_TRIP' && (
                <button onClick={() => openEdit(row)} title="Edit driver" className="p-1.5 rounded text-[#6b7280] hover:text-[#d97706] hover:bg-[#1f1f1f]">
                  <Pencil className="h-3.5 w-3.5" />
                </button>
              )}
              {canDelete && (
                <button onClick={() => setDeleting(row)} className="p-1.5 rounded text-[#6b7280] hover:text-red-400 hover:bg-red-900/10">
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              )}
            </div>
          ) : undefined}
        />
      </div>

      <FormDialog open={!!dialog} title={dialog==='edit'?`Edit Driver — ${editing?.name}`:'Add New Driver'} onClose={closeDialog}>
        <DriverForm defaultValues={editing} onSubmit={handleSubmit} serverErrors={serverErrors} isEdit={dialog === 'edit'} />
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={closeDialog}>Cancel</button>
          <button type="submit" form="driver-form" className="btn-amber" disabled={createMutation.isPending||updateMutation.isPending}>
            {createMutation.isPending||updateMutation.isPending?'Saving…':dialog==='edit'?'Update Driver':'Add Driver'}
          </button>
        </div>
      </FormDialog>

      <ConfirmDialog
        open={!!deleting}
        title="Delete Driver"
        description={`Remove "${deleting?.name}" from the system?`}
        confirmLabel="Delete Driver"
        onConfirm={async()=>{ await deleteMutation.mutateAsync(deleting.id); setDeleting(null); }}
        onCancel={()=>setDeleting(null)}
        loading={deleteMutation.isPending}
      />
    </div>
  );
}
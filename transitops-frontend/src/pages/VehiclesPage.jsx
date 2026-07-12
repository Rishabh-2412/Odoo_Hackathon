import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Plus, Pencil, Trash2, Download } from 'lucide-react';
import { useVehicles, useCreateVehicle, useUpdateVehicle, useDeleteVehicle } from '../hooks/useVehicles';
import { useAuth } from '../context/AuthContext';
import DataTable from '../components/DataTable';
import StatusBadge from '../components/StatusBadge';
import PageHeader from '../components/PageHeader';
import FormDialog from '../components/FormDialog';
import ConfirmDialog from '../components/ConfirmDialog';
import { vehicleSchema } from '../schemas';
import { formatCurrency, exportToCSV } from '../lib/utils';
import { VEHICLE_TYPES, REGIONS, VEHICLE_STATUSES } from '../config';

const FIELD = ({ label, error, children }) => (
  <div>
    <label className="label-dark">{label}</label>
    {children}
    {error && <p className="text-xs text-red-400 mt-1">{error.message}</p>}
  </div>
);

function VehicleForm({ defaultValues, onSubmit, serverErrors = {}, isEdit = false }) {
  const { register, handleSubmit, formState:{errors} } = useForm({
    resolver: zodResolver(vehicleSchema),
    defaultValues: defaultValues || { status:'AVAILABLE' },
  });
  const allErrors = { ...errors };
  Object.keys(serverErrors).forEach(k => { if(!allErrors[k]) allErrors[k] = { message: serverErrors[k] }; });

  return (
    <form id="vehicle-form" onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
      <FIELD label="Registration Number *" error={allErrors.registrationNumber}>
        <input {...register('registrationNumber')} className="input-dark" placeholder="TN01AB1234" readOnly={isEdit} />
      </FIELD>
      <FIELD label="Vehicle Name / Model *" error={allErrors.nameModel}>
        <input {...register('nameModel')} className="input-dark" placeholder="Tata Prima 4028" />
      </FIELD>
      <FIELD label="Type *" error={allErrors.type}>
        <select {...register('type')} className="input-dark">
          <option value="">Select type</option>
          {VEHICLE_TYPES.map(t => <option key={t}>{t}</option>)}
        </select>
      </FIELD>
      <FIELD label="Max Load Capacity (kg) *" error={allErrors.maximumLoadCapacity}>
        <input {...register('maximumLoadCapacity')} type="number" className="input-dark" placeholder="15000" />
      </FIELD>
      <FIELD label="Odometer (km)" error={allErrors.odometer}>
        <input {...register('odometer')} type="number" className="input-dark" placeholder="50000" />
      </FIELD>
      <FIELD label="Acquisition Cost (₹)" error={allErrors.acquisitionCost}>
        <input {...register('acquisitionCost')} type="number" className="input-dark" placeholder="2500000" />
      </FIELD>
      <FIELD label="Region *" error={allErrors.region}>
        <select {...register('region')} className="input-dark">
          <option value="">Select region</option>
          {REGIONS.map(r => <option key={r}>{r}</option>)}
        </select>
      </FIELD>
      {isEdit ? (
        <FIELD label="Status" error={allErrors.status}>
          <select {...register('status')} className="input-dark">
            {defaultValues?.status === 'IN_SHOP' && <option value="IN_SHOP">IN_SHOP</option>}
            <option value="AVAILABLE">AVAILABLE</option>
            <option value="RETIRED">RETIRED</option>
          </select>
          <p className="text-xs text-[#6b7280] mt-1">
            ON_TRIP is set by trip dispatch. IN_SHOP is set by active maintenance.
          </p>
        </FIELD>
      ) : (
        <div className="col-span-2 rounded border border-[#2a2a2a] bg-[#171717] px-3 py-2 text-xs text-[#9ca3af]">
          New vehicles are created as <strong className="text-emerald-400">AVAILABLE</strong>. ON_TRIP and IN_SHOP are changed automatically by trip and maintenance workflows.
        </div>
      )}
    </form>
  );
}

export default function VehiclesPage() {
  const { hasRole } = useAuth();
  const canManage = hasRole('ADMIN', 'FLEET_MANAGER');
  const canDelete = hasRole('ADMIN');

  const { data, isLoading, error } = useVehicles();
  const createMutation = useCreateVehicle();
  const updateMutation = useUpdateVehicle();
  const deleteMutation = useDeleteVehicle();

  const [dialog,  setDialog]  = useState(null); // null | 'create' | 'edit'
  const [editing, setEditing] = useState(null);
  const [deleting,setDeleting]= useState(null);
  const [serverErrors, setServerErrors] = useState({});
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');

  const filtered = (data || []).filter(v =>
    (!statusFilter || v.status === statusFilter) &&
    (!typeFilter   || v.type   === typeFilter)
  );

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

  const handleDelete = async () => {
    await deleteMutation.mutateAsync(deleting.id);
    setDeleting(null);
  };

  const columns = [
    { key:'registrationNumber', label:'Reg. No.',    render: v => <span className="font-mono text-xs font-semibold text-[#d97706]">{v}</span> },
    { key:'nameModel',          label:'Vehicle'                              },
    { key:'type',               label:'Type',        render: v => <span className="text-xs text-[#9ca3af]">{v}</span> },
    { key:'maximumLoadCapacity',label:'Max Load',    render: v => `${(v/1000).toFixed(1)} t`                          },
    { key:'odometer',           label:'Odometer',   render: v => `${v?.toLocaleString()} km`                         },
    { key:'acquisitionCost',    label:'Cost',       render: v => formatCurrency(v)                                   },
    { key:'region',             label:'Region',     render: v => <span className="text-xs text-[#9ca3af]">{v}</span> },
    { key:'status',             label:'Status',     render: v => <StatusBadge status={v} />                          },
  ];

  return (
    <div className="space-y-5">
      <PageHeader
        title="Fleet Registry"
        description={`${data?.length || 0} vehicles total`}
        actions={
          <div className="flex gap-2">
            <button onClick={() => exportToCSV(data, 'vehicles')} className="btn-ghost">
              <Download className="h-4 w-4" /> Export
            </button>
            {canManage && (
              <button onClick={openCreate} className="btn-amber">
                <Plus className="h-4 w-4" /> Add Vehicle
              </button>
            )}
          </div>
        }
      />

      {/* Filters */}
      <div className="flex flex-wrap gap-3">
        <input
          className="search-input"
          placeholder="Search registration, name…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <select className="input-dark w-40" value={statusFilter} onChange={e=>setStatusFilter(e.target.value)}>
          <option value="">All Statuses</option>
          {VEHICLE_STATUSES.map(s => <option key={s}>{s}</option>)}
        </select>
        <select className="input-dark w-36" value={typeFilter} onChange={e=>setTypeFilter(e.target.value)}>
          <option value="">All Types</option>
          {VEHICLE_TYPES.map(t => <option key={t}>{t}</option>)}
        </select>
      </div>

      <div className="panel overflow-hidden">
        <DataTable
          columns={columns}
          data={filtered}
          loading={isLoading}
          error={error}
          searchKeys={['registrationNumber','nameModel']}
          searchValue={search}
          emptyTitle="No vehicles found"
          emptyDescription="Add your first vehicle to the fleet registry."
          actions={canManage ? row => (
            <div className="flex items-center justify-end gap-2">
              {row.status !== 'ON_TRIP' && (
                <button onClick={() => openEdit(row)} title="Edit vehicle" className="p-1.5 rounded text-[#6b7280] hover:text-[#d97706] hover:bg-[#1f1f1f]">
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

      {/* Create/Edit dialog */}
      <FormDialog
        open={!!dialog}
        title={dialog === 'edit' ? `Edit Vehicle — ${editing?.nameModel}` : 'Add New Vehicle'}
        description={dialog === 'edit' ? 'Update fleet vehicle details.' : 'Register a new vehicle in the fleet.'}
        onClose={closeDialog}
      >
        <VehicleForm
          defaultValues={editing}
          onSubmit={handleSubmit}
          serverErrors={serverErrors}
          isEdit={dialog === 'edit'}
        />
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={closeDialog}>Cancel</button>
          <button type="submit" form="vehicle-form" className="btn-amber" disabled={createMutation.isPending || updateMutation.isPending}>
            {createMutation.isPending || updateMutation.isPending ? 'Saving…' : dialog==='edit' ? 'Update Vehicle' : 'Add Vehicle'}
          </button>
        </div>
      </FormDialog>

      {/* Delete confirm */}
      <ConfirmDialog
        open={!!deleting}
        title="Delete Vehicle"
        description={`Are you sure you want to permanently delete "${deleting?.nameModel}"? This cannot be undone.`}
        confirmLabel="Delete Vehicle"
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
        loading={deleteMutation.isPending}
      />
    </div>
  );
}
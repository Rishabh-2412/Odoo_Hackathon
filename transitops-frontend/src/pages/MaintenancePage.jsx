import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowRight, Check, Plus, Trash2 } from 'lucide-react';
import {
  useCloseMaintenance,
  useCreateMaintenance,
  useDeleteMaintenance,
  useMaintenance,
} from '../hooks/useMaintenance';
import { useVehicles } from '../hooks/useVehicles';
import DataTable from '../components/DataTable';
import StatusBadge from '../components/StatusBadge';
import PageHeader from '../components/PageHeader';
import FormDialog from '../components/FormDialog';
import ConfirmDialog from '../components/ConfirmDialog';
import { maintenanceSchema } from '../schemas';
import { formatCurrency, formatDate } from '../lib/utils';
import { useAuth } from '../context/AuthContext';

const MAINTENANCE_TYPES = [
  'OIL_SERVICE',
  'TYRE_REPLACEMENT',
  'BRAKE_SERVICE',
  'ENGINE_OVERHAUL',
  'BODY_REPAIR',
  'ELECTRICAL',
  'AC_SERVICE',
  'GENERAL',
];

const Field = ({ label, error, children, span }) => (
  <div className={span ? 'col-span-2' : ''}>
    <label className="label-dark">{label}</label>
    {children}
    {error && <p className="text-xs text-red-400 mt-1">{error.message}</p>}
  </div>
);

function MaintenanceForm({ vehicles, onSubmit }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(maintenanceSchema) });

  return (
    <form id="maintenance-form" onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
      <Field label="Vehicle *" error={errors.vehicleId}>
        <select {...register('vehicleId')} className="input-dark">
          <option value="">Select vehicle</option>
          {vehicles
            ?.filter((vehicle) => vehicle.status === 'AVAILABLE')
            .map((vehicle) => (
              <option key={vehicle.id} value={vehicle.id}>
                {vehicle.registrationNumber} — {vehicle.vehicleName} ({vehicle.status})
              </option>
            ))}
        </select>
      </Field>
      <Field label="Service Type *" error={errors.maintenanceType}>
        <select {...register('maintenanceType')} className="input-dark">
          <option value="">Select type</option>
          {MAINTENANCE_TYPES.map((type) => (
            <option key={type} value={type}>{type.replace(/_/g, ' ')}</option>
          ))}
        </select>
      </Field>
      <Field label="Description" error={errors.description} span>
        <textarea {...register('description')} rows={3} className="input-dark resize-none" placeholder="Describe the maintenance work" />
      </Field>
      <Field label="Start Date *" error={errors.startDate}>
        <input {...register('startDate')} type="date" className="input-dark" />
      </Field>
      <Field label="Estimated Cost (₹)" error={errors.cost}>
        <input {...register('cost')} type="number" step="0.01" className="input-dark" placeholder="2500" />
      </Field>
      <Field label="Odometer at Service (km)" error={errors.odometerAtService}>
        <input {...register('odometerAtService')} type="number" step="0.01" className="input-dark" placeholder="1600" />
      </Field>
    </form>
  );
}

function CloseMaintenanceForm({ record, onSubmit }) {
  const { register, handleSubmit } = useForm({
    defaultValues: {
      endDate: new Date().toISOString().slice(0, 10),
      finalCost: record?.cost || 0,
      closingNotes: '',
    },
  });

  return (
    <form id="close-maintenance-form" onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
      <Field label="End Date *">
        <input {...register('endDate', { required: true })} type="date" className="input-dark" />
      </Field>
      <Field label="Final Cost (₹) *">
        <input {...register('finalCost', { required: true, min: 0 })} type="number" step="0.01" className="input-dark" />
      </Field>
      <Field label="Closing Notes" span>
        <textarea {...register('closingNotes')} className="input-dark min-h-24" placeholder="Work completed and vehicle inspected" />
      </Field>
    </form>
  );
}

export default function MaintenancePage() {
  const { hasRole } = useAuth();
  const canManage = hasRole('ADMIN', 'FLEET_MANAGER');
  const { data, isLoading, error } = useMaintenance();
  const { data: vehicles } = useVehicles();
  const createMutation = useCreateMaintenance();
  const closeMutation = useCloseMaintenance();
  const deleteMutation = useDeleteMaintenance();

  const [createOpen, setCreateOpen] = useState(false);
  const [closing, setClosing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const filtered = (data || []).filter((record) => !statusFilter || record.status === statusFilter);
  const getVehicleName = (vehicleId) => {
    const vehicle = vehicles?.find((item) => item.id === vehicleId);
    return vehicle ? `${vehicle.registrationNumber} — ${vehicle.vehicleName}` : `Vehicle #${vehicleId}`;
  };

  const columns = [
    { key: 'id', label: '#', render: (value) => <span className="font-mono text-xs text-[#6b7280]">#{value}</span> },
    { key: 'vehicleId', label: 'Vehicle', render: (value) => <span className="font-medium text-[#d1d5db] text-sm">{getVehicleName(value)}</span> },
    { key: 'maintenanceType', label: 'Type', render: (value) => <span className="text-xs text-[#9ca3af]">{value?.replace(/_/g, ' ')}</span> },
    { key: 'description', label: 'Description', render: (value) => <span className="text-xs text-[#9ca3af] line-clamp-1">{value || '—'}</span> },
    { key: 'startDate', label: 'Start', render: formatDate },
    { key: 'completionDate', label: 'Closed', render: (value) => value ? formatDate(value) : '—' },
    { key: 'cost', label: 'Cost', render: formatCurrency },
    { key: 'status', label: 'Status', render: (value) => <StatusBadge status={value} /> },
  ];

  return (
    <div className="space-y-5">
      <PageHeader
        title="Maintenance"
        description="Opening maintenance moves a vehicle to IN_SHOP; closing restores AVAILABLE."
        actions={canManage && (
          <button onClick={() => setCreateOpen(true)} className="btn-amber">
            <Plus className="h-4 w-4" /> Log Service Record
          </button>
        )}
      />

      <div className="panel px-5 py-3 flex items-center gap-3 flex-wrap">
        <span className="badge-green text-xs">AVAILABLE</span>
        <ArrowRight className="h-4 w-4 text-[#4b5563]" />
        <span className="text-xs text-[#6b7280]">Create Maintenance</span>
        <ArrowRight className="h-4 w-4 text-[#4b5563]" />
        <span className="badge-amber text-xs">IN_SHOP</span>
        <ArrowRight className="h-4 w-4 text-[#4b5563]" />
        <span className="text-xs text-[#6b7280]">Close Maintenance</span>
        <ArrowRight className="h-4 w-4 text-[#4b5563]" />
        <span className="badge-green text-xs">AVAILABLE</span>
      </div>

      <div className="flex gap-3">
        <input className="search-input" placeholder="Search service or description" value={search} onChange={(event) => setSearch(event.target.value)} />
        <select className="input-dark w-40" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
          <option value="">All Statuses</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="CLOSED">CLOSED</option>
        </select>
      </div>

      <div className="panel overflow-hidden">
        <DataTable
          columns={columns}
          data={filtered}
          loading={isLoading}
          error={error}
          searchKeys={['maintenanceType', 'description', 'vehicleRegistrationNumber', 'vehicleNameModel']}
          searchValue={search}
          emptyTitle="No maintenance records"
          emptyDescription="Log your first service record."
          actions={canManage ? (row) => (
            <div className="flex gap-2">
              {row.status === 'ACTIVE' && (
                <button onClick={() => setClosing(row)} className="btn-ghost py-1 px-2 text-xs text-emerald-400 border-emerald-900/40">
                  <Check className="h-3.5 w-3.5" /> Close
                </button>
              )}
              {row.status === 'CLOSED' && (
                <button onClick={() => setDeleting(row)} className="p-1.5 rounded text-[#6b7280] hover:text-red-400 hover:bg-red-900/10">
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              )}
            </div>
          ) : undefined}
        />
      </div>

      <FormDialog open={createOpen} title="Log Service Record" description="This immediately changes the vehicle status to IN_SHOP." onClose={() => setCreateOpen(false)}>
        <MaintenanceForm
          vehicles={vehicles}
          onSubmit={async (formData) => {
            await createMutation.mutateAsync(formData);
            setCreateOpen(false);
          }}
        />
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={() => setCreateOpen(false)}>Cancel</button>
          <button type="submit" form="maintenance-form" className="btn-amber" disabled={createMutation.isPending}>
            {createMutation.isPending ? 'Logging…' : 'Log Record'}
          </button>
        </div>
      </FormDialog>

      <FormDialog open={Boolean(closing)} title={`Close Maintenance #${closing?.id}`} description="Enter the final service details." onClose={() => setClosing(null)}>
        <CloseMaintenanceForm
          record={closing}
          onSubmit={async (formData) => {
            await closeMutation.mutateAsync({ id: closing.id, data: formData });
            setClosing(null);
          }}
        />
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={() => setClosing(null)}>Cancel</button>
          <button type="submit" form="close-maintenance-form" className="btn-amber" disabled={closeMutation.isPending}>
            {closeMutation.isPending ? 'Closing…' : 'Close Maintenance'}
          </button>
        </div>
      </FormDialog>

      <ConfirmDialog
        open={Boolean(deleting)}
        title="Delete Closed Maintenance Record"
        description={`Delete maintenance record #${deleting?.id}?`}
        confirmLabel="Delete Record"
        onConfirm={async () => {
          await deleteMutation.mutateAsync(deleting.id);
          setDeleting(null);
        }}
        onCancel={() => setDeleting(null)}
        loading={deleteMutation.isPending}
      />
    </div>
  );
}

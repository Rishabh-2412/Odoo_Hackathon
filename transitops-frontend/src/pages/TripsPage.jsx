import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { AlertTriangle, Check, Plus } from 'lucide-react';
import {
  useCancelTrip,
  useCompleteTrip,
  useCreateTrip,
  useDispatchTrip,
  useTrips,
} from '../hooks/useTrips';
import { useAvailableVehicles } from '../hooks/useVehicles';
import { useAvailableDrivers } from '../hooks/useDrivers';
import DataTable from '../components/DataTable';
import StatusBadge from '../components/StatusBadge';
import PageHeader from '../components/PageHeader';
import FormDialog from '../components/FormDialog';
import { tripCompleteSchema, tripSchema } from '../schemas';
import { formatCurrency, formatDate } from '../lib/utils';
import { useAuth } from '../context/AuthContext';

const Field = ({ label, error, children, span }) => (
  <div className={span ? 'col-span-2' : ''}>
    <label className="label-dark">{label}</label>
    {children}
    {error && <p className="text-xs text-red-400 mt-1">{error.message}</p>}
  </div>
);

function TripForm({ onSubmit, availableVehicles, availableDrivers }) {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({ resolver: zodResolver(tripSchema) });

  const selectedVehicleId = Number(watch('vehicleId'));
  const cargoWeight = Number(watch('cargoWeight') || 0);
  const selectedVehicle = availableVehicles?.find((vehicle) => vehicle.id === selectedVehicleId);
  const capacityExceeded = selectedVehicle && cargoWeight > selectedVehicle.maximumLoadCapacity;

  return (
    <form id="trip-form" onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
      <Field label="Source *" error={errors.source}>
        <input {...register('source')} className="input-dark" placeholder="Chennai" />
      </Field>
      <Field label="Destination *" error={errors.destination}>
        <input {...register('destination')} className="input-dark" placeholder="Bengaluru" />
      </Field>

      <Field label="Available Vehicle *" error={errors.vehicleId}>
        <select {...register('vehicleId')} className="input-dark">
          <option value="">Select vehicle</option>
          {availableVehicles?.map((vehicle) => (
            <option key={vehicle.id} value={vehicle.id}>
              {vehicle.registrationNumber} — {vehicle.vehicleName} ({vehicle.maximumLoadCapacity} kg)
            </option>
          ))}
        </select>
      </Field>

      <Field label="Available Driver *" error={errors.driverId}>
        <select {...register('driverId')} className="input-dark">
          <option value="">Select driver</option>
          {availableDrivers?.map((driver) => (
            <option key={driver.id} value={driver.id}>
              {driver.name} — {driver.licenseCategory}
            </option>
          ))}
        </select>
      </Field>

      {selectedVehicle && (
        <div className={`col-span-2 p-4 rounded-xl border ${capacityExceeded ? 'border-red-700 bg-red-900/10' : 'border-[#2a2a2a] bg-[#1a1a1a]'}`}>
          <div className="grid grid-cols-3 gap-4 text-xs">
            <div><p className="label-dark">Registration</p><p className="font-mono text-[#d97706]">{selectedVehicle.registrationNumber}</p></div>
            <div><p className="label-dark">Maximum Capacity</p><p className="text-[#d1d5db]">{selectedVehicle.maximumLoadCapacity.toLocaleString()} kg</p></div>
            <div><p className="label-dark">Odometer</p><p className="text-[#d1d5db]">{selectedVehicle.odometer.toLocaleString()} km</p></div>
          </div>
          {capacityExceeded && (
            <div className="flex items-center gap-2 mt-3 text-xs text-red-400 font-semibold">
              <AlertTriangle className="h-4 w-4" />
              Cargo exceeds capacity by {(cargoWeight - selectedVehicle.maximumLoadCapacity).toLocaleString()} kg.
            </div>
          )}
        </div>
      )}

      <Field label="Cargo Weight (kg) *" error={errors.cargoWeight}>
        <input {...register('cargoWeight')} type="number" step="0.01" className="input-dark" placeholder="450" />
      </Field>
      <Field label="Planned Distance (km) *" error={errors.plannedDistance}>
        <input {...register('plannedDistance')} type="number" step="0.01" className="input-dark" placeholder="500" />
      </Field>
      <Field label="Notes" error={errors.notes} span>
        <textarea {...register('notes')} className="input-dark min-h-24" placeholder="Cargo or delivery notes" />
      </Field>
    </form>
  );
}

function CompleteForm({ trip, onSubmit }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(tripCompleteSchema),
    defaultValues: {
      finalOdometerKm: trip?.startOdometerKm || 0,
      fuelConsumedLiters: 0,
      fuelCost: 0,
      revenue: 0,
      notes: '',
    },
  });

  return (
    <form id="complete-form" onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
      <Field label="Final Odometer (km) *" error={errors.finalOdometerKm}>
        <input {...register('finalOdometerKm')} type="number" step="0.01" className="input-dark" />
      </Field>
      <Field label="Fuel Consumed (L)" error={errors.fuelConsumedLiters}>
        <input {...register('fuelConsumedLiters')} type="number" step="0.001" className="input-dark" />
      </Field>
      <Field label="Fuel Cost (₹)" error={errors.fuelCost}>
        <input {...register('fuelCost')} type="number" step="0.01" className="input-dark" />
      </Field>
      <Field label="Revenue (₹)" error={errors.revenue}>
        <input {...register('revenue')} type="number" step="0.01" className="input-dark" />
      </Field>
      <Field label="Completion Notes" error={errors.notes} span>
        <textarea {...register('notes')} className="input-dark min-h-24" placeholder="Delivery completed successfully" />
      </Field>
    </form>
  );
}

export default function TripsPage() {
  const { hasRole } = useAuth();
  const canOperateTrips = hasRole('ADMIN', 'FLEET_MANAGER', 'DRIVER');

  const { data, isLoading, error } = useTrips();
  const { data: availableVehicles } = useAvailableVehicles();
  const { data: availableDrivers } = useAvailableDrivers();
  const createMutation = useCreateTrip();
  const dispatchMutation = useDispatchTrip();
  const completeMutation = useCompleteTrip();
  const cancelMutation = useCancelTrip();

  const [dialog, setDialog] = useState(null);
  const [actionTrip, setActionTrip] = useState(null);
  const [cancelReason, setCancelReason] = useState('');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const filtered = (data || []).filter((trip) => !statusFilter || trip.status === statusFilter);

  const columns = [
    { key: 'id', label: '#', render: (value) => <span className="font-mono text-xs text-[#6b7280]">#{value}</span> },
    { key: 'source', label: 'Route', render: (value, row) => <span className="font-medium">{value} → {row.destination}</span> },
    { key: 'driverName', label: 'Driver' },
    { key: 'vehicleRegistrationNumber', label: 'Vehicle' },
    { key: 'cargoWeight', label: 'Cargo', render: (value) => `${value.toLocaleString()} kg` },
    { key: 'plannedDistance', label: 'Distance', render: (value) => `${value.toLocaleString()} km` },
    { key: 'revenue', label: 'Revenue', render: (value) => value ? formatCurrency(value) : '—' },
    { key: 'startDate', label: 'Created / Started', render: (value) => formatDate(value) },
    { key: 'status', label: 'Status', render: (value) => <StatusBadge status={value} /> },
  ];

  const createTrip = async (formData) => {
    await createMutation.mutateAsync(formData);
    setDialog(null);
  };

  const completeTrip = async (formData) => {
    await completeMutation.mutateAsync({ id: actionTrip.id, data: formData });
    setActionTrip(null);
    setDialog(null);
  };

  const cancelTrip = async () => {
    await cancelMutation.mutateAsync({
      id: actionTrip.id,
      reason: cancelReason.trim() || 'Cancelled by user',
    });
    setActionTrip(null);
    setCancelReason('');
    setDialog(null);
  };

  return (
    <div className="space-y-5">
      <PageHeader
        title="Trips"
        description={`${data?.length || 0} total trips`}
        actions={canOperateTrips && (
          <button onClick={() => setDialog('create')} className="btn-amber">
            <Plus className="h-4 w-4" /> New Trip
          </button>
        )}
      />

      <div className="flex gap-3 flex-wrap">
        <input className="search-input" placeholder="Search route…" value={search} onChange={(event) => setSearch(event.target.value)} />
        <select className="input-dark w-40" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
          <option value="">All Statuses</option>
          {['DRAFT', 'DISPATCHED', 'COMPLETED', 'CANCELLED'].map((status) => <option key={status}>{status}</option>)}
        </select>
      </div>

      <div className="panel overflow-hidden">
        <DataTable
          columns={columns}
          data={filtered}
          loading={isLoading}
          error={error}
          searchKeys={['source', 'destination', 'driverName', 'vehicleRegistrationNumber']}
          searchValue={search}
          emptyTitle="No trips found"
          emptyDescription="Create your first trip draft."
          actions={canOperateTrips ? (row) => (
            <div className="flex items-center justify-end gap-2">
              {row.status === 'DRAFT' && (
                <button
                  onClick={() => dispatchMutation.mutate(row.id)}
                  className="btn-ghost py-1 px-2 text-xs text-blue-400 border-blue-900/40 hover:bg-blue-900/10"
                  disabled={dispatchMutation.isPending}
                >
                  Dispatch
                </button>
              )}
              {row.status === 'DISPATCHED' && (
                <button
                  onClick={() => { setActionTrip(row); setDialog('complete'); }}
                  className="btn-ghost py-1 px-2 text-xs text-emerald-400 border-emerald-900/40 hover:bg-emerald-900/10"
                >
                  <Check className="h-3.5 w-3.5" /> Complete
                </button>
              )}
              {['DRAFT', 'DISPATCHED'].includes(row.status) && (
                <button
                  onClick={() => { setActionTrip(row); setCancelReason(''); setDialog('cancel'); }}
                  className="btn-ghost py-1 px-2 text-xs text-red-400 border-red-900/40 hover:bg-red-900/10"
                >
                  Cancel
                </button>
              )}
            </div>
          ) : undefined}
        />
      </div>

      <FormDialog open={dialog === 'create'} title="New Trip" description="Only available vehicles and valid available drivers are listed." onClose={() => setDialog(null)} width="max-w-3xl">
        <TripForm onSubmit={createTrip} availableVehicles={availableVehicles} availableDrivers={availableDrivers} />
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={() => setDialog(null)}>Cancel</button>
          <button type="submit" form="trip-form" className="btn-amber" disabled={createMutation.isPending}>
            {createMutation.isPending ? 'Saving…' : 'Save Draft'}
          </button>
        </div>
      </FormDialog>

      <FormDialog open={dialog === 'complete'} title={`Complete Trip #${actionTrip?.id}`} description="The backend calculates actual distance from the final odometer." onClose={() => { setDialog(null); setActionTrip(null); }}>
        <CompleteForm trip={actionTrip} onSubmit={completeTrip} />
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={() => { setDialog(null); setActionTrip(null); }}>Cancel</button>
          <button type="submit" form="complete-form" className="btn-amber" disabled={completeMutation.isPending}>
            {completeMutation.isPending ? 'Processing…' : 'Mark Completed'}
          </button>
        </div>
      </FormDialog>

      <FormDialog open={dialog === 'cancel'} title={`Cancel Trip #${actionTrip?.id}`} description="A cancellation reason is required for the audit trail." onClose={() => { setDialog(null); setActionTrip(null); }}>
        <label className="label-dark">Reason</label>
        <textarea value={cancelReason} onChange={(event) => setCancelReason(event.target.value)} className="input-dark min-h-24" placeholder="Customer cancelled the delivery" />
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={() => { setDialog(null); setActionTrip(null); }}>Back</button>
          <button className="btn-amber" onClick={cancelTrip} disabled={cancelMutation.isPending || !cancelReason.trim()}>
            {cancelMutation.isPending ? 'Cancelling…' : 'Confirm Cancellation'}
          </button>
        </div>
      </FormDialog>
    </div>
  );
}

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Plus, Pencil, Trash2, Download } from 'lucide-react';
import { useExpenses, useCreateExpense, useUpdateExpense, useDeleteExpense } from '../hooks/useExpenses';
import { useFuelLogs } from '../hooks/useFuelLogs';
import { useVehicles } from '../hooks/useVehicles';
import DataTable from '../components/DataTable';
import PageHeader from '../components/PageHeader';
import FormDialog from '../components/FormDialog';
import ConfirmDialog from '../components/ConfirmDialog';
import { expenseSchema } from '../schemas';
import { formatCurrency, formatDate, exportToCSV } from '../lib/utils';
import { EXPENSE_TYPES } from '../config';
import { useAuth } from '../context/AuthContext';

const FIELD = ({ label, error, children, span }) => (
  <div className={span?'col-span-2':''}>
    <label className="label-dark">{label}</label>
    {children}
    {error && <p className="text-xs text-red-400 mt-1">{error.message}</p>}
  </div>
);

function ExpenseForm({ vehicles, defaultValues, onSubmit, isEdit = false }) {
  const { register, handleSubmit, formState:{errors} } = useForm({
    resolver: zodResolver(expenseSchema),
    defaultValues: defaultValues || {},
  });
  return (
    <form id="expense-form" onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
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
      <FIELD label="Expense Type *" error={errors.expenseType}>
        <select {...register('expenseType')} className="input-dark">
          <option value="">Select type</option>
          {EXPENSE_TYPES.map(t=><option key={t}>{t}</option>)}
        </select>
      </FIELD>
      <FIELD label="Description *" error={errors.description} span>
        <input {...register('description')} className="input-dark" placeholder="NH48 Delhi Toll (multiple)"/>
      </FIELD>
      <FIELD label="Amount (₹) *" error={errors.amount}>
        <input {...register('amount')} type="number" step="0.01" className="input-dark" placeholder="3200"/>
      </FIELD>
      <FIELD label="Date *" error={errors.expenseDate}>
        <input {...register('expenseDate')} type="date" className="input-dark"/>
      </FIELD>
    </form>
  );
}

const TYPE_BADGE = {
  TOLL:'text-blue-400 bg-blue-900/20 border-blue-900/40',
  MAINTENANCE:'text-amber-400 bg-amber-900/20 border-amber-900/40',
  PARKING:'text-purple-400 bg-purple-900/20 border-purple-900/40',
  INSURANCE:'text-emerald-400 bg-emerald-900/20 border-emerald-900/40',
  OTHER:'text-gray-400 bg-[#1f1f1f] border-[#2a2a2a]',
};

export default function ExpensesPage() {
  const { hasRole } = useAuth();
  const canCreate = hasRole('ADMIN', 'FLEET_MANAGER', 'FINANCIAL_ANALYST');
  const canEdit = hasRole('ADMIN', 'FINANCIAL_ANALYST');
  const { data, isLoading, error } = useExpenses();
  const { data: fuelLogs }  = useFuelLogs();
  const { data: vehicles }  = useVehicles();
  const createMutation = useCreateExpense();
  const updateMutation = useUpdateExpense();
  const deleteMutation = useDeleteExpense();

  const [dialog,  setDialog]  = useState(null);
  const [editing, setEditing] = useState(null);
  const [deleting,setDeleting]= useState(null);
  const [search,  setSearch]  = useState('');

  const totalExpenses = (data||[]).reduce((s,e)=>s+e.amount,0);
  const totalFuel     = (fuelLogs||[]).reduce((s,f)=>s+f.cost,0);
  const totalOp       = totalExpenses + totalFuel;

  const columns = [
    { key:'vehicleId',   label:'Vehicle',    render: v => { const veh=vehicles?.find(x=>x.id===v); return veh?veh.registrationNumber:`#${v}`; } },
    { key:'expenseType', label:'Type',       render: v => <span className={`badge text-[10px] border ${TYPE_BADGE[v]||TYPE_BADGE.OTHER}`}>{v}</span> },
    { key:'description', label:'Description',render: v => <span className="text-xs text-[#9ca3af]">{v}</span> },
    { key:'amount',      label:'Amount',     render: v => <span className="font-semibold text-[#f0f0f0]">{formatCurrency(v)}</span> },
    { key:'expenseDate', label:'Date',       render: v => formatDate(v) },
  ];

  const handleSubmit = async (data) => {
    if(dialog==='edit') await updateMutation.mutateAsync({id:editing.id,data});
    else                await createMutation.mutateAsync(data);
    setDialog(null); setEditing(null);
  };

  return (
    <div className="space-y-5">
      <PageHeader
        title="Expenses"
        description="Operational expenses: tolls, insurance, parking & more"
        actions={
          <div className="flex gap-2">
            <button onClick={()=>exportToCSV(data,'expenses')} className="btn-ghost"><Download className="h-4 w-4"/>Export</button>
            {canCreate && <button onClick={()=>setDialog('create')} className="btn-amber"><Plus className="h-4 w-4"/>Log Expense</button>}
          </div>
        }
      />

      {/* Summary */}
      <div className="grid grid-cols-3 gap-4">
        <div className="panel p-4"><p className="label-dark">Total Expenses</p><p className="text-2xl font-bold text-amber-400">{formatCurrency(totalExpenses)}</p></div>
        <div className="panel p-4"><p className="label-dark">Total Fuel Cost</p><p className="text-2xl font-bold text-blue-400">{formatCurrency(totalFuel)}</p></div>
        <div className="panel p-4 border-l-4 border-l-red-500">
          <p className="label-dark">Total Operational Cost</p>
          <p className="text-2xl font-bold text-red-400">{formatCurrency(totalOp)}</p>
          <p className="text-xs text-[#6b7280] mt-1">Fuel + All Expenses</p>
        </div>
      </div>

      <input className="search-input" placeholder="Search description…" value={search} onChange={e=>setSearch(e.target.value)}/>

      <div className="panel overflow-hidden">
        <DataTable
          columns={columns}
          data={data||[]}
          loading={isLoading}
          error={error}
          searchKeys={['description','expenseType']}
          searchValue={search}
          emptyTitle="No expenses logged"
          emptyDescription="Log your first operational expense."
          actions={canEdit ? row=>(
            <div className="flex gap-2">
              <button onClick={()=>{setEditing(row);setDialog('edit');}} className="p-1.5 rounded text-[#6b7280] hover:text-[#d97706] hover:bg-[#1f1f1f]"><Pencil className="h-3.5 w-3.5"/></button>
              <button onClick={()=>setDeleting(row)} className="p-1.5 rounded text-[#6b7280] hover:text-red-400 hover:bg-red-900/10"><Trash2 className="h-3.5 w-3.5"/></button>
            </div>
          ) : undefined}
        />
      </div>

      <FormDialog open={!!dialog} title={dialog==='edit'?'Edit Expense':'Log Expense'} onClose={()=>{setDialog(null);setEditing(null);}}>
        <ExpenseForm vehicles={vehicles} defaultValues={editing} onSubmit={handleSubmit} isEdit={dialog === 'edit'}/>
        <div className="flex justify-end gap-3 mt-6">
          <button className="btn-ghost" onClick={()=>{setDialog(null);setEditing(null);}}>Cancel</button>
          <button type="submit" form="expense-form" className="btn-amber" disabled={createMutation.isPending||updateMutation.isPending}>
            {createMutation.isPending||updateMutation.isPending?'Saving…':'Save Expense'}
          </button>
        </div>
      </FormDialog>

      <ConfirmDialog
        open={!!deleting}
        title="Delete Expense"
        description="Remove this expense permanently?"
        confirmLabel="Delete"
        onConfirm={async()=>{await deleteMutation.mutateAsync(deleting.id);setDeleting(null);}}
        onCancel={()=>setDeleting(null)}
        loading={deleteMutation.isPending}
      />
    </div>
  );
}
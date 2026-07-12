import { AlertTriangle, X } from 'lucide-react';

export default function ConfirmDialog({ open, title, description, confirmLabel = 'Confirm', danger = true, onConfirm, onCancel, loading }) {
  if (!open) return null;
  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="bg-[#141414] border border-[#2a2a2a] rounded-2xl w-full max-w-md p-6" onClick={e => e.stopPropagation()}>
        <div className="flex items-start gap-4 mb-6">
          <div className={`p-2 rounded-lg ${danger ? 'bg-red-900/20' : 'bg-amber-900/20'}`}>
            <AlertTriangle className={`h-5 w-5 ${danger ? 'text-red-400' : 'text-amber-400'}`} />
          </div>
          <div className="flex-1">
            <h3 className="font-semibold text-[#f0f0f0] mb-1">{title}</h3>
            <p className="text-sm text-[#6b7280]">{description}</p>
          </div>
          <button onClick={onCancel} className="text-[#6b7280] hover:text-[#f0f0f0]"><X className="h-4 w-4" /></button>
        </div>
        <div className="flex justify-end gap-3">
          <button className="btn-ghost" onClick={onCancel} disabled={loading}>Cancel</button>
          <button className={danger ? 'btn-danger' : 'btn-amber'} onClick={onConfirm} disabled={loading}>
            {loading ? 'Processing…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
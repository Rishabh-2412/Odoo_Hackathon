import { X } from 'lucide-react';

export default function FormDialog({ open, title, description, children, onClose, width = 'max-w-2xl' }) {
  if (!open) return null;
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className={`modal-box ${width}`} onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#2a2a2a] sticky top-0 bg-[#141414] z-10">
          <div>
            <h2 className="text-base font-semibold text-[#f0f0f0]">{title}</h2>
            {description && <p className="text-xs text-[#6b7280] mt-0.5">{description}</p>}
          </div>
          <button onClick={onClose} className="text-[#6b7280] hover:text-[#f0f0f0] transition-colors p-1">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="px-6 py-5">{children}</div>
      </div>
    </div>
  );
}
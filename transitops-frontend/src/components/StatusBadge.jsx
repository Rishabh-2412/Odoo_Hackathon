const STATUS_MAP = {
  // Vehicle
  AVAILABLE:  { label:'Available',  cls:'badge-green' },
  ON_TRIP:    { label:'On Trip',    cls:'badge-blue'  },
  IN_SHOP:    { label:'In Shop',    cls:'badge-amber' },
  RETIRED:    { label:'Retired',    cls:'badge-red'   },
  // Driver
  OFF_DUTY:   { label:'Off Duty',   cls:'badge-gray'  },
  SUSPENDED:  { label:'Suspended',  cls:'badge-red'   },
  // Trip
  DRAFT:      { label:'Draft',      cls:'badge-amber' },
  DISPATCHED: { label:'Dispatched', cls:'badge-blue'  },
  COMPLETED:  { label:'Completed',  cls:'badge-green' },
  CANCELLED:  { label:'Cancelled',  cls:'badge-red'   },
  // Maintenance
  ACTIVE:     { label:'Active',     cls:'badge-amber' },
  // Generic
  PENDING:    { label:'Pending',    cls:'badge-amber' },
};

const DOT_MAP = {
  'badge-green':'bg-emerald-400',
  'badge-blue': 'bg-blue-400',
  'badge-amber':'bg-amber-400',
  'badge-red':  'bg-red-400',
  'badge-gray': 'bg-gray-400',
};

export default function StatusBadge({ status, showDot = true }) {
  const cfg = STATUS_MAP[status] || { label: status, cls:'badge-gray' };
  return (
    <span className={cfg.cls}>
      {showDot && <span className={`inline-block w-1.5 h-1.5 rounded-full ${DOT_MAP[cfg.cls]}`} />}
      {cfg.label}
    </span>
  );
}
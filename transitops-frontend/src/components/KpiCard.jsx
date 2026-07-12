const BORDER_COLORS = {
  green: 'border-l-emerald-500',
  blue:  'border-l-blue-500',
  amber: 'border-l-amber-500',
  red:   'border-l-red-500',
  gray:  'border-l-gray-500',
};

const TEXT_COLORS = {
  green: 'text-emerald-400',
  blue:  'text-blue-400',
  amber: 'text-amber-400',
  red:   'text-red-400',
  gray:  'text-gray-400',
};

export default function KPICard({ label, value, subtitle, accent = 'gray', icon: Icon }) {
  return (
    <div className={`kpi-card border-l-4 ${BORDER_COLORS[accent]}`}>
      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <p className="label-dark">{label}</p>
          <p className={`text-2xl font-bold mt-1 ${TEXT_COLORS[accent]}`}>{value}</p>
          {subtitle && <p className="text-xs text-[#6b7280] mt-1">{subtitle}</p>}
        </div>
        {Icon && (
          <div className={`p-2 rounded-lg bg-[#1f1f1f]`}>
            <Icon className={`h-5 w-5 ${TEXT_COLORS[accent]}`} />
          </div>
        )}
      </div>
    </div>
  );
}
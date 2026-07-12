export default function PageHeader({ title, description, actions, children }) {
  return (
    <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-6">
      <div>
        <h1 className="text-xl font-bold text-[#f0f0f0]">{title}</h1>
        {description && <p className="text-sm text-[#6b7280] mt-0.5">{description}</p>}
        {children}
      </div>
      {actions && <div className="flex items-center gap-2 shrink-0">{actions}</div>}
    </div>
  );
}
import { Inbox } from 'lucide-react';

export default function EmptyState({ title = 'Nothing here yet', description = '', action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center px-4">
      <div className="p-4 rounded-full bg-[#1f1f1f] mb-4">
        <Inbox className="h-8 w-8 text-[#4b5563]" />
      </div>
      <h3 className="text-sm font-semibold text-[#9ca3af] mb-1">{title}</h3>
      {description && <p className="text-xs text-[#6b7280] max-w-xs">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
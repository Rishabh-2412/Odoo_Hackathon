import { AlertTriangle } from 'lucide-react';

export default function ErrorState({ message = 'Something went wrong.', onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="p-4 rounded-full bg-red-900/20 mb-4">
        <AlertTriangle className="h-8 w-8 text-red-400" />
      </div>
      <p className="text-sm text-[#9ca3af] mb-4">{message}</p>
      {onRetry && <button className="btn-ghost" onClick={onRetry}>Try Again</button>}
    </div>
  );
}
export default function LoadingSkeleton({ rows = 5, cols = 4 }) {
  return (
    <div className="p-4 space-y-2 animate-pulse">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-4">
          {Array.from({ length: cols }).map((_, j) => (
            <div key={j} className={`h-8 bg-[#1f1f1f] rounded ${j===0?'w-24':'flex-1'}`} />
          ))}
        </div>
      ))}
    </div>
  );
}

export function SkeletonCard({ className = '' }) {
  return (
    <div className={`panel p-5 animate-pulse ${className}`}>
      <div className="h-3 w-24 bg-[#1f1f1f] rounded mb-3" />
      <div className="h-8 w-16 bg-[#1f1f1f] rounded mb-2" />
      <div className="h-3 w-32 bg-[#1f1f1f] rounded" />
    </div>
  );
}
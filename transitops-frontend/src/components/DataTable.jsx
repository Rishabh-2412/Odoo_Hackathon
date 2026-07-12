import { useState, useMemo } from 'react';
import { ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-react';
import EmptyState from './EmptyState';
import LoadingSkeleton from './LoadingSkeleton';

export default function DataTable({
  columns,          // [{ key, label, render, sortable, className }]
  data,
  loading,
  error,
  emptyTitle = 'No records found',
  emptyDescription = 'Add a new record to get started.',
  rowKey = 'id',
  onRowClick,
  actions,          // (row) => JSX
  searchKeys = [],  // keys to search across
  searchValue = '',
}) {
  const [sortKey,   setSortKey]   = useState(null);
  const [sortDir,   setSortDir]   = useState('asc');
  const [page,      setPage]      = useState(1);
  const PER_PAGE = 10;

  const filtered = useMemo(() => {
    if (!data) return [];
    let rows = [...data];
    if (searchValue && searchKeys.length) {
      const q = searchValue.toLowerCase();
      rows = rows.filter(r => searchKeys.some(k => String(r[k] ?? '').toLowerCase().includes(q)));
    }
    if (sortKey) {
      rows.sort((a,b) => {
        const av = a[sortKey], bv = b[sortKey];
        if (av == null) return 1; if (bv == null) return -1;
        return sortDir === 'asc' ? (av > bv ? 1 : -1) : (av < bv ? 1 : -1);
      });
    }
    return rows;
  }, [data, searchValue, searchKeys, sortKey, sortDir]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PER_PAGE));
  const pageData   = filtered.slice((page-1)*PER_PAGE, page*PER_PAGE);

  const handleSort = (key) => {
    if (sortKey === key) setSortDir(d => d === 'asc' ? 'desc' : 'asc');
    else { setSortKey(key); setSortDir('asc'); }
    setPage(1);
  };

  if (loading) return <LoadingSkeleton rows={6} />;
  if (error)   return <div className="p-8 text-center text-red-400 text-sm">Failed to load data. Please retry.</div>;

  return (
    <div>
      <div className="overflow-x-auto">
        <table className="dark-table">
          <thead>
            <tr>
              {columns.map(col => (
                <th key={col.key} className={col.className}>
                  {col.sortable !== false ? (
                    <button className="flex items-center gap-1 hover:text-[#f0f0f0] transition-colors" onClick={() => handleSort(col.key)}>
                      {col.label}
                      {sortKey === col.key
                        ? sortDir === 'asc' ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />
                        : <ChevronsUpDown className="h-3 w-3 opacity-40" />}
                    </button>
                  ) : col.label}
                </th>
              ))}
              {actions && <th className="text-right">Actions</th>}
            </tr>
          </thead>
          <tbody>
            {pageData.length === 0 ? (
              <tr><td colSpan={columns.length + (actions?1:0)}>
                <EmptyState title={emptyTitle} description={emptyDescription} />
              </td></tr>
            ) : pageData.map(row => (
              <tr key={row[rowKey]} onClick={() => onRowClick?.(row)} className={onRowClick?'cursor-pointer':''}>
                {columns.map(col => (
                  <td key={col.key} className={col.className}>
                    {col.render ? col.render(row[col.key], row) : (row[col.key] ?? '—')}
                  </td>
                ))}
                {actions && <td className="text-right" onClick={e=>e.stopPropagation()}>{actions(row)}</td>}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filtered.length > PER_PAGE && (
        <div className="flex items-center justify-between px-4 py-3 border-t border-[#1f1f1f]">
          <p className="text-xs text-[#6b7280]">Showing {(page-1)*PER_PAGE+1}–{Math.min(page*PER_PAGE,filtered.length)} of {filtered.length}</p>
          <div className="flex gap-2">
            <button className="btn-ghost py-1 px-3 text-xs" disabled={page===1} onClick={()=>setPage(p=>p-1)}>Prev</button>
            <span className="text-xs text-[#6b7280] self-center">Page {page}/{totalPages}</span>
            <button className="btn-ghost py-1 px-3 text-xs" disabled={page===totalPages} onClick={()=>setPage(p=>p+1)}>Next</button>
          </div>
        </div>
      )}
    </div>
  );
}
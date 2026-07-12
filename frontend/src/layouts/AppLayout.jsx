import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Toaster } from 'sonner';
import Sidebar from '../components/Sidebar';
import TopBar from '../components/TopBar';

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [search, setSearch] = useState('');

  return (
    <div className="flex h-screen bg-[#0d0d0d] overflow-hidden">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="flex flex-col flex-1 lg:ml-64 min-w-0">
        <TopBar
          onMenuToggle={() => setSidebarOpen(o => !o)}
          searchValue={search}
          onSearchChange={setSearch}
        />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet context={{ search }} />
        </main>
      </div>

      <Toaster
        position="bottom-right"
        toastOptions={{
          style: { background:'#141414', border:'1px solid #2a2a2a', color:'#f0f0f0' },
          classNames: { success: 'border-l-4 border-l-emerald-500', error: 'border-l-4 border-l-red-500' },
        }}
        richColors
      />
    </div>
  );
}
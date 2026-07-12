import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Plus, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '../components/PageHeader';
import { userService } from '../services/userService';

const ROLE_OPTIONS = ['ADMIN', 'FLEET_MANAGER', 'DRIVER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST'];

export default function UsersRolesPage() {
  const queryClient = useQueryClient();
  const { data: users = [], isLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: userService.list,
  });
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    roles: ['DRIVER'],
  });
  const [selectedRoles, setSelectedRoles] = useState({});

  const createMutation = useMutation({
    mutationFn: userService.create,
    onSuccess: () => {
      toast.success('User created');
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      setForm({ name: '', email: '', password: '', roles: ['DRIVER'] });
    },
    onError: (error) => toast.error(error.message || 'Failed to create user'),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, enabled }) => userService.updateStatus(id, enabled),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
    onError: (error) => toast.error(error.message || 'Failed to update user status'),
  });

  const rolesMutation = useMutation({
    mutationFn: ({ id, roles }) => userService.updateRoles(id, roles),
    onSuccess: () => {
      toast.success('User role updated');
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
    },
    onError: (error) => toast.error(error.message || 'Failed to update roles'),
  });

  const submit = (event) => {
    event.preventDefault();
    createMutation.mutate(form);
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Users & Roles" description="Admin-only user, role, and account status management" />

      <form onSubmit={submit} className="panel p-5 grid md:grid-cols-5 gap-3">
        <input
          className="input-dark"
          placeholder="Name"
          value={form.name}
          onChange={(event) => setForm({ ...form, name: event.target.value })}
          required
        />
        <input
          className="input-dark"
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={(event) => setForm({ ...form, email: event.target.value })}
          required
        />
        <input
          className="input-dark"
          type="password"
          placeholder="Password (8+)"
          value={form.password}
          onChange={(event) => setForm({ ...form, password: event.target.value })}
          minLength={8}
          required
        />
        <select
          className="input-dark"
          value={form.roles[0]}
          onChange={(event) => setForm({ ...form, roles: [event.target.value] })}
        >
          {ROLE_OPTIONS.map((role) => <option key={role}>{role}</option>)}
        </select>
        <button className="btn-amber" disabled={createMutation.isPending}>
          <Plus className="h-4 w-4" /> Create User
        </button>
      </form>

      <div className="panel overflow-hidden">
        <div className="px-5 py-4 border-b border-[#2a2a2a] flex gap-2">
          <ShieldCheck className="h-4 w-4 text-[#d97706]" />
          <h3 className="text-sm font-semibold">Platform Users</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="dark-table">
            <thead>
              <tr><th>User</th><th>Email</th><th>Current Roles</th><th>Set Primary Role</th><th>Status</th><th>Action</th></tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan="6">Loading…</td></tr>
              ) : users.map((user) => {
                const selectedRole = selectedRoles[user.id] || user.roles?.[0] || 'DRIVER';
                return (
                  <tr key={user.id}>
                    <td>{user.name}</td>
                    <td>{user.email}</td>
                    <td>{(user.roles || []).join(', ')}</td>
                    <td>
                      <div className="flex gap-2 min-w-64">
                        <select
                          className="input-dark py-1.5"
                          value={selectedRole}
                          onChange={(event) => setSelectedRoles({ ...selectedRoles, [user.id]: event.target.value })}
                        >
                          {ROLE_OPTIONS.map((role) => <option key={role}>{role}</option>)}
                        </select>
                        <button
                          className="btn-ghost"
                          onClick={() => rolesMutation.mutate({ id: user.id, roles: [selectedRole] })}
                          type="button"
                        >
                          Save
                        </button>
                      </div>
                    </td>
                    <td>{user.enabled ? 'ACTIVE' : 'DISABLED'}</td>
                    <td>
                      <button
                        className="btn-ghost"
                        onClick={() => statusMutation.mutate({ id: user.id, enabled: !user.enabled })}
                        type="button"
                      >
                        {user.enabled ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

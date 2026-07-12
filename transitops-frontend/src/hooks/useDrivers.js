import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { driverService } from '../services/driverService';
import { toast } from 'sonner';

export const DRIVER_KEYS = {
  all: ['drivers'],
  available: ['drivers','available'],
};

export function useDrivers() {
  return useQuery({ queryKey: DRIVER_KEYS.all, queryFn: driverService.getAll, staleTime: 30000 });
}

export function useAvailableDrivers() {
  return useQuery({ queryKey: DRIVER_KEYS.available, queryFn: driverService.getAvailable, staleTime: 10000 });
}

export function useCreateDriver() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: driverService.create,
    onSuccess: () => { qc.invalidateQueries({ queryKey: DRIVER_KEYS.all }); toast.success('Driver added successfully'); },
    onError: (err) => { if(!err.fieldErrors) toast.error(err.message || 'Failed to add driver'); },
  });
}

export function useUpdateDriver() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => driverService.update(id, data),
    onSuccess: async () => { await qc.invalidateQueries({ queryKey: ['drivers'] }); await qc.invalidateQueries({ queryKey: ['dashboard'] }); toast.success('Driver details and status updated'); },
    onError: (err) => { if(!err.fieldErrors) toast.error(err.message || 'Failed to update driver'); },
  });
}

export function useDeleteDriver() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: driverService.delete,
    onSuccess: () => { qc.invalidateQueries({ queryKey: DRIVER_KEYS.all }); toast.success('Driver deleted'); },
    onError: () => toast.error('Failed to delete driver'),
  });
}

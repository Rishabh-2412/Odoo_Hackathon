import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { maintenanceService } from '../services/maintenanceService';

export const MAINT_KEYS = { all: ['maintenance'] };

const invalidateAll = (queryClient) => {
  queryClient.invalidateQueries({ queryKey: MAINT_KEYS.all });
  queryClient.invalidateQueries({ queryKey: ['vehicles'] });
  queryClient.invalidateQueries({ queryKey: ['dashboard'] });
};

export function useMaintenance() {
  return useQuery({ queryKey: MAINT_KEYS.all, queryFn: maintenanceService.getAll, staleTime: 30000 });
}

export function useCreateMaintenance() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: maintenanceService.create,
    onSuccess: () => {
      invalidateAll(queryClient);
      toast.success('Maintenance logged — vehicle set to IN_SHOP');
    },
    onError: (error) => {
      if (!error.fieldErrors) toast.error(error.message || 'Failed to log maintenance');
    },
  });
}

export function useCloseMaintenance() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => maintenanceService.close(id, data),
    onSuccess: () => {
      invalidateAll(queryClient);
      toast.success('Maintenance closed — vehicle set to AVAILABLE');
    },
    onError: (error) => toast.error(error.message || 'Failed to close maintenance'),
  });
}

export function useDeleteMaintenance() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: maintenanceService.delete,
    onSuccess: () => {
      invalidateAll(queryClient);
      toast.success('Maintenance record deleted');
    },
    onError: (error) => toast.error(error.message || 'Failed to delete maintenance'),
  });
}

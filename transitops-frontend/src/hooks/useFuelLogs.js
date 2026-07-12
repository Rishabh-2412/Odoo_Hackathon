import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fuelService } from '../services/fuelService';
import { toast } from 'sonner';

export const FUEL_KEYS = { all: ['fuelLogs'] };

export function useFuelLogs() {
  return useQuery({ queryKey: FUEL_KEYS.all, queryFn: fuelService.getAll, staleTime: 30000 });
}

export function useCreateFuelLog() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: fuelService.create,
    onSuccess: () => { qc.invalidateQueries({ queryKey: FUEL_KEYS.all }); toast.success('Fuel log added'); },
    onError: (err) => { if(!err.fieldErrors) toast.error(err.message || 'Failed to add fuel log'); },
  });
}

export function useUpdateFuelLog() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => fuelService.update(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: FUEL_KEYS.all }); toast.success('Fuel log updated'); },
  });
}

export function useDeleteFuelLog() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: fuelService.delete,
    onSuccess: () => { qc.invalidateQueries({ queryKey: FUEL_KEYS.all }); toast.success('Fuel log deleted'); },
    onError: () => toast.error('Failed to delete fuel log'),
  });
}

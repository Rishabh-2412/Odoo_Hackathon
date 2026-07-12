import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { vehicleService } from '../services/vehicleService';
import { toast } from 'sonner';

export const VEHICLE_KEYS = {
  all: ['vehicles'],
  available: ['vehicles','available'],
};

export function useVehicles() {
  return useQuery({ queryKey: VEHICLE_KEYS.all, queryFn: vehicleService.getAll, staleTime: 30000 });
}

export function useAvailableVehicles() {
  return useQuery({ queryKey: VEHICLE_KEYS.available, queryFn: vehicleService.getAvailable, staleTime: 10000 });
}

export function useCreateVehicle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: vehicleService.create,
    onSuccess: () => { qc.invalidateQueries({ queryKey: VEHICLE_KEYS.all }); toast.success('Vehicle added successfully'); },
    onError: (err) => { if(!err.fieldErrors) toast.error(err.message || 'Failed to add vehicle'); },
  });
}

export function useUpdateVehicle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => vehicleService.update(id, data),
    onSuccess: async () => { await qc.invalidateQueries({ queryKey: ['vehicles'] }); await qc.invalidateQueries({ queryKey: ['dashboard'] }); toast.success('Vehicle details and status updated'); },
    onError: (err) => { if(!err.fieldErrors) toast.error(err.message || 'Failed to update vehicle'); },
  });
}

export function useDeleteVehicle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: vehicleService.delete,
    onSuccess: () => { qc.invalidateQueries({ queryKey: VEHICLE_KEYS.all }); toast.success('Vehicle deleted'); },
    onError: () => toast.error('Failed to delete vehicle'),
  });
}

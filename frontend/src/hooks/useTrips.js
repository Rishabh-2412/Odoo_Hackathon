import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tripService } from '../services/tripService';
import { toast } from 'sonner';

export const TRIP_KEYS = { all: ['trips'] };

const invalidateAll = (qc) => {
  qc.invalidateQueries({ queryKey: TRIP_KEYS.all });
  qc.invalidateQueries({ queryKey: ['vehicles'] });
  qc.invalidateQueries({ queryKey: ['drivers'] });
  qc.invalidateQueries({ queryKey: ['dashboard'] });
};

export function useTrips() {
  return useQuery({ queryKey: TRIP_KEYS.all, queryFn: tripService.getAll, staleTime: 20000 });
}

export function useCreateTrip() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: tripService.create,
    onSuccess: () => { invalidateAll(qc); toast.success('Trip saved as draft'); },
    onError: (err) => { if(!err.fieldErrors) toast.error(err.message || 'Failed to create trip'); },
  });
}

export function useUpdateTrip() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => tripService.update(id, data),
    onSuccess: () => { invalidateAll(qc); toast.success('Trip updated'); },
    onError: (err) => { if(!err.fieldErrors) toast.error(err.message || 'Failed to update trip'); },
  });
}

export function useDispatchTrip() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: tripService.dispatch,
    onSuccess: () => { invalidateAll(qc); toast.success('Trip dispatched — vehicle & driver set to ON_TRIP'); },
    onError: () => toast.error('Failed to dispatch trip'),
  });
}

export function useCompleteTrip() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => tripService.complete(id, data),
    onSuccess: () => { invalidateAll(qc); toast.success('Trip completed — vehicle & driver now AVAILABLE'); },
    onError: () => toast.error('Failed to complete trip'),
  });
}

export function useCancelTrip() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: tripService.cancel,
    onSuccess: () => { invalidateAll(qc); toast.success('Trip cancelled'); },
    onError: () => toast.error('Failed to cancel trip'),
  });
}

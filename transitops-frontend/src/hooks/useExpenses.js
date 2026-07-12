import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { expenseService } from '../services/expenseService';
import { toast } from 'sonner';

export const EXPENSE_KEYS = { all: ['expenses'] };

export function useExpenses() {
  return useQuery({ queryKey: EXPENSE_KEYS.all, queryFn: expenseService.getAll, staleTime: 30000 });
}

export function useCreateExpense() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: expenseService.create,
    onSuccess: () => { qc.invalidateQueries({ queryKey: EXPENSE_KEYS.all }); toast.success('Expense logged'); },
    onError: (err) => { if(!err.fieldErrors) toast.error(err.message || 'Failed to log expense'); },
  });
}

export function useUpdateExpense() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => expenseService.update(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: EXPENSE_KEYS.all }); toast.success('Expense updated'); },
  });
}

export function useDeleteExpense() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: expenseService.delete,
    onSuccess: () => { qc.invalidateQueries({ queryKey: EXPENSE_KEYS.all }); toast.success('Expense deleted'); },
    onError: () => toast.error('Failed to delete expense'),
  });
}

import { useQuery } from '@tanstack/react-query';
import { reportService } from '../services/reportService';

export function useReports() {
  return useQuery({ queryKey: ['reports'], queryFn: reportService.getAnalytics, staleTime: 60000 });
}

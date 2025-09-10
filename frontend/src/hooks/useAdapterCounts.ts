import { useQuery } from '@tanstack/react-query';
import { adapterTypeService } from '@/services/adapterTypeService';

export const useAdapterCounts = () => {
  return useQuery({
    queryKey: ['adapterCounts'],
    queryFn: async () => {
      const response = await adapterTypeService.getAdapterCountsByCategory();
      if (!response.success) {
        throw new Error(response.error || 'Failed to fetch adapter counts');
      }
      return response.data || {};
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
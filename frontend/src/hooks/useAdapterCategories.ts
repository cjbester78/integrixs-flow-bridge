import { useQuery } from '@tanstack/react-query';
import { adapterTypeService } from '@/services/adapterTypeService';

export const useAdapterCategories = () => {
  return useQuery({
    queryKey: ['adapterCategories'],
    queryFn: async () => {
      const response = await adapterTypeService.getAdapterCategories();
      if (!response.success) {
        throw new Error(response.error || 'Failed to fetch adapter categories');
      }
      return response.data;
    },
    staleTime: 10 * 60 * 1000, // 10 minutes - categories don't change often
  });
};
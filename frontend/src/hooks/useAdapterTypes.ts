import { useQuery } from '@tanstack/react-query';
import { adapterTypeService, AdapterTypeFilters } from '@/services/adapterTypeService';

export const useAdapterTypes = (filters?: AdapterTypeFilters) => {
  return useQuery({
    queryKey: ['adapterTypes', filters],
    queryFn: async () => {
      const response = await adapterTypeService.getAdapterTypes(filters);
      if (!response.success) {
        throw new Error(response.error || 'Failed to fetch adapter types');
      }
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

export const useAdapterType = (id: string) => {
  return useQuery({
    queryKey: ['adapterType', id],
    queryFn: async () => {
      const response = await adapterTypeService.getAdapterType(id);
      if (!response.success) {
        throw new Error(response.error || 'Failed to fetch adapter type');
      }
      return response.data;
    },
    enabled: !!id,
  });
};

export const useAdapterConfigSchema = (
  typeId: string, 
  direction: 'inbound' | 'outbound' | 'bidirectional'
) => {
  return useQuery({
    queryKey: ['adapterConfigSchema', typeId, direction],
    queryFn: async () => {
      const response = await adapterTypeService.getConfigurationSchema(typeId, direction);
      if (!response.success) {
        throw new Error(response.error || 'Failed to fetch configuration schema');
      }
      return response.data;
    },
    enabled: !!typeId && !!direction,
  });
};
import { apiClient } from '@/lib/api-client';
import type { CommunicationAdapter, AdapterType, AdapterMode } from '@/types/communicationAdapter';

const API_PREFIX = '/adapters';

interface CreateAdapterRequest {
  name: string;
  description?: string;
  type: AdapterType;
  mode: AdapterMode;
  configuration: any;
  isActive: boolean;
  packageId?: string;
}

interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
}

export const communicationAdapterService = {
  getAllAdapters: async (): Promise<ApiResponse<CommunicationAdapter[]>> => {
    try {
      const response = await apiClient.get<CommunicationAdapter[]>(API_PREFIX);
      return { success: true, data: response };
    } catch (error) {
      console.error('Error fetching adapters:', error);
      return { success: false, message: 'Failed to fetch adapters' };
    }
  },

  createAdapter: async (data: CreateAdapterRequest): Promise<ApiResponse<CommunicationAdapter>> => {
    try {
      const response = await apiClient.post<CommunicationAdapter>(API_PREFIX, data);
      return { success: true, data: response };
    } catch (error) {
      console.error('Error creating adapter:', error);
      return { success: false, message: 'Failed to create adapter' };
    }
  },
};
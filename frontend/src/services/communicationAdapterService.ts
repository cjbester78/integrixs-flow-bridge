import { apiClient } from '@/lib/api-client';
import { logger, LogCategory } from '@/lib/logger';
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
   logger.error(LogCategory.API, 'Error fetching adapters', { error: error });
   return { success: false, message: 'Failed to fetch adapters' };
  }
 },

 createAdapter: async (data: CreateAdapterRequest): Promise<ApiResponse<CommunicationAdapter>> => {
  try {
   const response = await apiClient.post<CommunicationAdapter>(API_PREFIX, data);
   return { success: true, data: response };
  } catch (error) {
   logger.error(LogCategory.API, 'Error creating adapter', { error: error });
   return { success: false, message: 'Failed to create adapter' };
  }
 }
};
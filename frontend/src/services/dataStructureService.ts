import { apiClient } from '@/lib/api-client';
import { logger, LogCategory } from '@/lib/logger';
import type { DataStructure } from '@/types/dataStructure';

const API_PREFIX = '/flow-structures';
interface PaginatedResponse<T> {
 content: T[];
 totalElements: number;
 totalPages: number;
 size: number;
 number: number;
}

interface CreateStructureRequest {
 name: string;
 type: 'json' | 'xml' | 'xsd' | 'wsdl' | 'soap';
 content: string;
 description?: string;
 packageId?: string;
}

interface ApiResponse<T> {
 success: boolean;
 data?: T;
 message?: string;
}

export const dataStructureService = {
 getAllStructures: async (): Promise<ApiResponse<PaginatedResponse<DataStructure>>> => {
  try {
   const response = await apiClient.get<PaginatedResponse<DataStructure>>(API_PREFIX);
   return { success: true, data: response };
  } catch (error) {
   logger.error(LogCategory.API, 'Error fetching structures', { error: error });
   return { success: false, message: 'Failed to fetch structures' };
  }
 },

 createStructure: async (data: CreateStructureRequest): Promise<ApiResponse<DataStructure>> => {
  try {
   const response = await apiClient.post<DataStructure>(API_PREFIX, data);
   return { success: true, data: response };
  } catch (error) {
   logger.error(LogCategory.API, 'Error creating structure', { error: error });
   return { success: false, message: 'Failed to create structure' };
  }
 }
};
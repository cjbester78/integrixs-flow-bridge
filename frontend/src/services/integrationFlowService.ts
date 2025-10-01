import { apiClient } from '@/lib/api-client';
import { logger, LogCategory } from '@/lib/logger';
import type { IntegrationFlow } from '@/types/flow';

const API_PREFIX = '/flows';
;
interface PaginatedResponse<T> {
 content: T[];
 totalElements: number;
 totalPages: number;
 size: number;
 number: number;
}

interface CreateFlowRequest {
 name: string;
 description?: string;
 type?: 'DIRECT_MAPPING' | 'ORCHESTRATION';
 isActive?: boolean;
 inboundAdapterId?: string;
 outboundAdapterId?: string;
 sourceStructureId?: string;
 targetStructureId?: string;
 responseStructureId?: string;
 packageId?: string;
}

interface ApiResponse<T> {
 success: boolean;
 data?: T;
 message?: string;
}

export const integrationFlowService = {
 getAllFlows: async (): Promise<ApiResponse<PaginatedResponse<IntegrationFlow>>> => {
 try {
 const response = await apiClient.get<PaginatedResponse<IntegrationFlow>>(API_PREFIX);
 return { success: true, data: response.data };
 } catch (error) {
 logger.error(LogCategory.API, 'Error fetching flows', { error: error });
 return { success: false, message: 'Failed to fetch flows' };
 }
 },

 createFlow: async (data: CreateFlowRequest): Promise<ApiResponse<IntegrationFlow>> => {
 const response = await apiClient.post<IntegrationFlow>(API_PREFIX, data);
 return { success: true, data: response };
 }
};
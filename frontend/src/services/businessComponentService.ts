import { BusinessComponent, CreateBusinessComponentRequest, UpdateBusinessComponentRequest } from '@/types/businessComponent';
import { apiClient } from '@/lib/api-client';
import { logger, LogCategory } from '@/lib/logger';

class BusinessComponentService {
 async getAllBusinessComponents(): Promise<{ success: boolean; data?: BusinessComponent[]; error?: string }> {
 try {
            const data = await apiClient.get<BusinessComponent[]>('/business-components');
 return { success: true, data };
 } catch (error) {
 logger.error(LogCategory.API, 'Failed to fetch business components', { error: error });
 return { success: false, error: 'Failed to fetch business components' };
 }
 }

 async getBusinessComponentById(id: string): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
 try {
 const data = await apiClient.get<BusinessComponent>(`/business-components/${id}`);
 return { success: true, data };
 } catch (error) {
 logger.error(LogCategory.API, 'Failed to fetch business component', { error: error });
 return { success: false, error: 'Failed to fetch business component' };
 }
 }

 async createBusinessComponent(data: CreateBusinessComponentRequest): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
 try {
 logger.info(LogCategory.API, 'Frontend: Creating business component with data', { data: data });
 const result = await apiClient.post<BusinessComponent>('/business-components', data);
 logger.info(LogCategory.API, 'Frontend: Created business component', { data: result });
 return { success: true, data: result };
 } catch (error) {
 logger.error(LogCategory.API, 'Frontend: Failed to create business component', { error: error });
 return { success: false, error: 'Failed to create business component' };
 }
 }

 async updateBusinessComponent(data: UpdateBusinessComponentRequest): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
 try {
 const result = await apiClient.put<BusinessComponent>(`/business-components/${data.id}`, data);
 return { success: true, data: result };
 } catch (error) {
 logger.error(LogCategory.API, 'Failed to update business component', { error: error });
 return { success: false, error: 'Failed to update business component' };
 }
 }

 async deleteBusinessComponent(id: string): Promise<{ success: boolean; error?: string }> {
 try {
 await apiClient.delete(`/business-components/${id}`);
 return { success: true };
 } catch (error) {
 logger.error(LogCategory.API, 'Failed to delete business component', { error: error });
 return { success: false, error: 'Failed to delete business component' };
 }
 }
}

export const businessComponentService = new BusinessComponentService();
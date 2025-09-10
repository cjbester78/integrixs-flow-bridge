import { apiClient } from '@/lib/api-client';
import { logger, LogCategory } from '@/lib/logger';
import type {
 ExternalAuthConfig,
 CreateExternalAuthRequest,
 UpdateExternalAuthRequest,
 AuthAttemptLog
} from '@/types/externalAuth';

export const externalAuthService = {
 // Get all auth configurations
 getAllAuthConfigs: async () => {
    try {
 const response = await apiClient.get<{ success: boolean; data: ExternalAuthConfig[]; message?: string }>('/external-auth');
 return response.data;
 } catch (error) {
  logger.error(LogCategory.API, 'Failed to fetch auth configs', { error: error });
  return { success: false, data: [], message: 'Failed to fetch authentication configurations' };
 }
 },

 // Get auth config by ID
 getAuthConfigById: async (id: string) => {
 try {
 const response = await apiClient.get<{ success: boolean; data: ExternalAuthConfig; message?: string }>(`/external-auth/${id}`);
 return response.data;
 } catch (error) {
  logger.error(LogCategory.API, 'Failed to fetch auth config', { error: error });
  return { success: false, data: null, message: 'Failed to fetch authentication configuration' };
 }
 },

 // Create new auth config
 createAuthConfig: async (request: CreateExternalAuthRequest) => {
 try {
 const response = await apiClient.post<{ success: boolean; data: ExternalAuthConfig; message?: string }>('/external-auth', request);
 return response.data;
 } catch (error) {
  logger.error(LogCategory.API, 'Failed to create auth config', { error: error });
  return { success: false, data: null, message: 'Failed to create authentication configuration' };
 }
 },

 // Update auth config
 updateAuthConfig: async (id: string, request: UpdateExternalAuthRequest) => {
  try {
   const response = await apiClient.put<{ success: boolean; data: ExternalAuthConfig; message?: string }>(`/external-auth/${id}`, request);
   return response.data;
  } catch (error) {
   logger.error(LogCategory.API, 'Failed to update auth config', { error: error });
   return { success: false, data: null, message: 'Failed to update authentication configuration' };
  }
 },

 // Delete auth config
 deleteAuthConfig: async (id: string) => {
  try {
   const response = await apiClient.delete(`/external-auth/${id}`);
   return response;
  } catch (error) {
   logger.error(LogCategory.API, 'Failed to delete auth config', { error: error });
   return { success: false, message: 'Failed to delete authentication configuration' };
  }
 },

 // Get auth attempts for a config
 getAuthAttempts: async (configId: string, limit = 50) => {
  try {
   const response = await apiClient.get<{ success: boolean; data: AuthAttemptLog[]; message?: string }>(`/external-auth/${configId}/attempts`, {
    params: { limit }
   });
   return response.data;
  } catch (error) {
   logger.error(LogCategory.API, 'Failed to fetch auth attempts', { error: error });
   return { success: false, data: [], message: 'Failed to fetch authentication attempts' };
  }
 },

 // Test auth configuration
 testAuthConfig: async (id: string) => {
  try {
   const response = await apiClient.post(`/external-auth/${id}/test`);
   return response;
  } catch (error) {
   logger.error(LogCategory.API, 'Failed to test auth config', { error: error });
   return { success: false, message: 'Failed to test authentication configuration' };
  }
 },

 // Link auth config to flow
 linkToFlow: async (authConfigId: string, flowId: string, adapterType: 'SOURCE' | 'TARGET') => {
 try {
 const response = await apiClient.post('/external-auth/link-flow', {
 authConfigId,
 flowId,
 adapterType
 });
 return response;
 } catch (error) {
  logger.error(LogCategory.API, 'Failed to link auth config to flow', { error: error });
  return { success: false, message: 'Failed to link authentication to flow' };
 }
 },

 // Unlink auth config from flow
 unlinkFromFlow: async (flowId: string, adapterType: 'SOURCE' | 'TARGET') => {
  try {
   const response = await apiClient.delete(`/external-auth/unlink-flow/${flowId}/${adapterType}`);
   return response;
  } catch (error) {
   logger.error(LogCategory.API, 'Failed to unlink auth config from flow', { error: error });
   return { success: false, message: 'Failed to unlink authentication from flow' };
  }
 }
};
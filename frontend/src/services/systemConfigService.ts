import { api } from './api';
import { logger, LogCategory } from '@/lib/logger';

export interface EnvironmentInfo {
 type: 'DEVELOPMENT' | 'QUALITY_ASSURANCE' | 'PRODUCTION';
 displayName: string;
 description: string;
 enforceRestrictions: boolean;
 permissions: EnvironmentPermissions;
}

export interface EnvironmentPermissions {
 canCreateFlows: boolean;
 canCreateAdapters: boolean;
 canModifyAdapterConfig: boolean;
 canImportFlows: boolean;
 canDeployFlows: boolean;
 canCreateBusinessComponents: boolean;
 canCreateDataStructures: boolean;
 isAdmin: boolean;
 canAccessAdmin: boolean;
}

export interface UIVisibility {
 element: string;
 visible: boolean;
}

export const systemConfigService = {
 /**
 * Get current environment configuration
 */
 getEnvironmentConfig: async (): Promise<EnvironmentInfo> => {
 try {
 const response = await api.get<EnvironmentInfo>('/system/config/environment');
 // Validate the response has the expected structure
 if (response.data && typeof response.data === 'object' && 'type' in response.data) {
 return response.data;
 }
 } catch (error) {
 logger.error(LogCategory.API, 'Failed to get environment config', { error });
 }
 // Return default if response is invalid
 logger.warn(LogCategory.API, 'Invalid environment config response, using defaults');
 return {
 type: 'DEVELOPMENT',
 displayName: 'Development',
 description: 'Development Environment',
 enforceRestrictions: false,
 permissions: {
 canCreateFlows: true,
 canCreateAdapters: true,
 canModifyAdapterConfig: true,
 canImportFlows: true,
 canDeployFlows: true,
 canCreateBusinessComponents: true,
 canCreateDataStructures: true,
 isAdmin: false,
 canAccessAdmin: false
 }
 }
}
} catch (error) {
 logger.error(LogCategory.API, 'Error fetching environment config', { error: error });
 // Return default on error
 return {
 type: 'DEVELOPMENT',
 displayName: 'Development',
 description: 'Development Environment',
 enforceRestrictions: false,
 permissions: {
 canCreateFlows: true,
 canCreateAdapters: true,
 canModifyAdapterConfig: true,
 canImportFlows: true,
 canDeployFlows: true,
 canCreateBusinessComponents: true,
 canCreateDataStructures: true,
 isAdmin: false,
 canAccessAdmin: false
 }
 }
}
 },

 /**
 * Get current user permissions
 */
 getPermissions: async (): Promise<EnvironmentPermissions> => {
 try {
const response = await api.get<EnvironmentPermissions>('/system/config/permissions');
 // Validate the response has the expected structure
 if (response.data && typeof response.data === 'object') {
 return response.data;
 
} catch (error) {
  // Handle error
}
 // Return default if response is invalid
 logger.warn(LogCategory.API, 'Invalid permissions response, using defaults');
 return {
 canCreateFlows: true,
 canCreateAdapters: true,
 canModifyAdapterConfig: true,
 canImportFlows: true,
 canDeployFlows: true,
 canCreateBusinessComponents: true,
 canCreateDataStructures: true,
 isAdmin: false,
 canAccessAdmin: false
 }
}
} catch (error) {
 logger.error(LogCategory.API, 'Error fetching permissions', { error: error });
 // Return default on error
 return {
 canCreateFlows: true,
 canCreateAdapters: true,
 canModifyAdapterConfig: true,
 canImportFlows: true,
 canDeployFlows: true,
 canCreateBusinessComponents: true,
 canCreateDataStructures: true,
 isAdmin: false,
 canAccessAdmin: false
 }
}
 },

 /**
 * Update environment type (admin only)
 */
 updateEnvironmentType: async (environmentType: string): Promise<EnvironmentInfo> => {
 const response = await api.put<EnvironmentInfo>('/system/config/environment', {
 environmentType
 });
 return response.data;
 },

 /**
 * Check if a specific action is allowed
 */
 checkPermission: async (action: string): Promise<boolean> => {
 const response = await api.get<{ allowed: boolean }>('/system/config/check-permission', {
 params: { action }
 });
 return response.data?.allowed || false;
 },

 /**
 * Check if UI element should be visible
 */
 checkUIVisibility: async (element: string): Promise<boolean> => {
 const response = await api.get<UIVisibility>('/system/config/ui-visibility', {
 params: { element }
 });
 return response.data?.visible || false;
 }
};
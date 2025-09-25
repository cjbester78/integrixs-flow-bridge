import { api } from './api';
import { logger, LogCategory } from '@/lib/logger';

export interface EnvironmentInfo {
 type: 'DEVELOPMENT' | 'QUALITY_ASSURANCE' | 'PRODUCTION';
 displayName: string;
 description: string;
 enforceRestrictions: boolean,
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
 isAdmin: boolean,
 canAccessAdmin: boolean;
}

export interface UIVisibility {
 element: string,
 visible: boolean;
}

export interface ConfigurationCategory {
  id: string;
  code: string;
  name: string;
  description: string;
  parentCategoryId?: string;
  displayOrder: number;
  isActive: boolean;
}

export interface Configuration {
  id: string;
  categoryId: string;
  configKey: string;
  configValue: string;
  configType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON' | 'YAML' | 'ENCRYPTED';
  defaultValue?: string;
  description?: string;
  isRequired: boolean;
  isEncrypted: boolean;
  isSensitive: boolean;
  validationRules?: any;
  allowedValues?: string[];
  environment?: string;
  profile?: string;
}

export interface MessagingConfiguration {
  id: string;
  messagingSystem: string;
  configGroup: string;
  configKey: string;
  configValue: string;
  configType: string;
  defaultValue?: string;
  description?: string;
  isRequired: boolean;
  isEncrypted: boolean;
  environment?: string;
}

export interface SecurityConfiguration {
  id: string;
  securityDomain: string;
  configKey: string;
  configValue: string;
  configType: string;
  defaultValue?: string;
  description?: string;
  isRequired: boolean;
  isEncrypted: boolean;
  isSensitive: boolean;
  environment?: string;
}

export interface AdapterConfiguration {
  id: string;
  adapterTypeId: string;
  configKey: string;
  configValue: string;
  configType: string;
  defaultValue?: string;
  description?: string;
  isRequired: boolean;
  isEncrypted: boolean;
  validationRules?: any;
  displayOrder: number;
  uiComponent?: string;
  uiOptions?: any;
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
 }
 } catch (error) {
 logger.error(LogCategory.API, 'Failed to get permissions', { error });
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
 },

 // ===== Configuration Management Methods =====
 
 /**
  * Get all configuration categories
  */
 getConfigurationCategories: async (): Promise<ConfigurationCategory[]> => {
   try {
     const response = await api.get<ConfigurationCategory[]>('/admin/configurations/categories');
     return response.data;
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to get configuration categories', { error });
     throw error;
   }
 },

 /**
  * Get configurations by category
  */
 getConfigurationsByCategory: async (categoryCode: string, environment?: string): Promise<Configuration[]> => {
   try {
     const params = environment ? { environment } : {};
     const response = await api.get<Configuration[]>(`/admin/configurations/category/${categoryCode}`, { params });
     return response.data;
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to get configurations by category', { error, categoryCode });
     throw error;
   }
 },

 /**
  * Get messaging configurations
  */
 getMessagingConfigurations: async (messagingSystem?: string): Promise<MessagingConfiguration[]> => {
   try {
     const params = messagingSystem ? { messagingSystem } : {};
     const response = await api.get<MessagingConfiguration[]>('/admin/configurations/messaging', { params });
     return response.data;
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to get messaging configurations', { error });
     throw error;
   }
 },

 /**
  * Get security configurations
  */
 getSecurityConfigurations: async (securityDomain?: string): Promise<SecurityConfiguration[]> => {
   try {
     const params = securityDomain ? { securityDomain } : {};
     const response = await api.get<SecurityConfiguration[]>('/admin/configurations/security', { params });
     return response.data;
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to get security configurations', { error });
     throw error;
   }
 },

 /**
  * Get adapter type default configurations
  */
 getAdapterDefaultConfigurations: async (adapterTypeId: string): Promise<AdapterConfiguration[]> => {
   try {
     const response = await api.get<AdapterConfiguration[]>(`/admin/configurations/adapter/${adapterTypeId}`);
     return response.data;
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to get adapter default configurations', { error, adapterTypeId });
     throw error;
   }
 },

 /**
  * Update configuration value
  */
 updateConfiguration: async (configKey: string, configValue: string, tableName: string = 'application_configurations'): Promise<void> => {
   try {
     await api.put(`/admin/configurations/${configKey}`, { 
       value: configValue,
       table: tableName 
     });
     logger.info(LogCategory.API, 'Configuration updated', { configKey, tableName });
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to update configuration', { error, configKey });
     throw error;
   }
 },

 /**
  * Update multiple configurations
  */
 updateConfigurations: async (configs: Record<string, string>, tableName: string = 'application_configurations'): Promise<void> => {
   try {
     await api.put('/admin/configurations/batch', { 
       configurations: configs,
       table: tableName 
     });
     logger.info(LogCategory.API, 'Multiple configurations updated', { count: Object.keys(configs).length });
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to update configurations', { error });
     throw error;
   }
 },

 /**
  * Refresh configurations from database
  */
 refreshConfigurations: async (): Promise<void> => {
   try {
     await api.post('/admin/configurations/refresh');
     logger.info(LogCategory.API, 'Configurations refreshed from database');
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to refresh configurations', { error });
     throw error;
   }
 },

 /**
  * Export configurations
  */
 exportConfigurations: async (category?: string, environment?: string): Promise<Blob> => {
   try {
     const params = { category, environment };
     const response = await api.get('/admin/configurations/export', { 
       params,
       responseType: 'blob' 
     });
     return response.data;
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to export configurations', { error });
     throw error;
   }
 },

 /**
  * Import configurations
  */
 importConfigurations: async (file: File): Promise<{ imported: number; skipped: number }> => {
   try {
     const formData = new FormData();
     formData.append('file', file);
     const response = await api.post<{ imported: number; skipped: number }>('/admin/configurations/import', formData, {
       headers: {
         'Content-Type': 'multipart/form-data',
       },
     });
     return response.data;
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to import configurations', { error });
     throw error;
   }
 },

 /**
  * Get configuration for adapter creation (hierarchy: UI → DB → YAML)
  */
 getAdapterConfigurationWithDefaults: async (adapterTypeId: string, instanceConfig?: Record<string, any>): Promise<Record<string, any>> => {
   try {
     // Get default configurations from database
     const defaultConfigs = await systemConfigService.getAdapterDefaultConfigurations(adapterTypeId);
     
     // Convert to key-value map
     const configMap: Record<string, any> = {};
     defaultConfigs.forEach(config => {
       configMap[config.configKey] = config.configValue || config.defaultValue;
     });
     
     // Override with instance-specific config if provided
     if (instanceConfig) {
       Object.assign(configMap, instanceConfig);
     }
     
     return configMap;
   } catch (error) {
     logger.error(LogCategory.API, 'Failed to get adapter configuration with defaults', { error, adapterTypeId });
     // Return empty config as fallback
     return instanceConfig || {};
   }
 }
};
import { apiClient } from '@/lib/api-client';
import type { Plugin, PluginDetails, UploadResult, ValidationResult } from '../types/plugin';

export const pluginApi = {
  // Get all plugins
  getAllPlugins: async (): Promise<Plugin[]> => {
    const response = await apiClient.get('/api/plugins');
    return response.data;
  },

  // Search plugins
  searchPlugins: async (params: {
    query?: string;
    category?: string | null;
    tags?: string[];
  }): Promise<Plugin[]> => {
    const response = await apiClient.get('/api/plugins/search', { params });
    return response.data;
  },

  // Get plugin details
  getPluginDetails: async (pluginId: string): Promise<PluginDetails> => {
    const response = await apiClient.get(`/api/plugins/${pluginId}`);
    return response.data;
  },

  // Upload plugin
  uploadPlugin: async (file: File, validate = true): Promise<UploadResult> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('validate', String(validate));

    const response = await apiClient.post('/api/plugins/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Initialize plugin
  initializePlugin: async (pluginId: string, configuration: Record<string, any>) => {
    const response = await apiClient.post(`/api/plugins/${pluginId}/initialize`, configuration);
    return response.data;
  },

  // Test connection
  testConnection: async (pluginId: string, direction: 'INBOUND' | 'OUTBOUND', configuration?: Record<string, any>) => {
    const response = await apiClient.post(`/api/plugins/${pluginId}/test-connection`, configuration, {
      params: { direction },
    });
    return response.data;
  },

  // Get health status
  getHealth: async (pluginId: string) => {
    const response = await apiClient.get(`/api/plugins/${pluginId}/health`);
    return response.data;
  },

  // Get configuration schema
  getConfigurationSchema: async (pluginId: string) => {
    const response = await apiClient.get(`/api/plugins/${pluginId}/configuration-schema`);
    return response.data;
  },

  // Validate configuration
  validateConfiguration: async (pluginId: string, configuration: Record<string, any>): Promise<ValidationResult> => {
    const response = await apiClient.post(`/api/plugins/${pluginId}/validate-configuration`, configuration);
    return response.data;
  },

  // Get categories
  getCategories: async (): Promise<string[]> => {
    const response = await apiClient.get('/api/plugins/categories');
    return response.data;
  },

  // Unregister plugin
  unregisterPlugin: async (pluginId: string): Promise<void> => {
    await apiClient.delete(`/api/plugins/${pluginId}`);
  },

  // Get plugin metrics
  getMetrics: async (pluginId: string) => {
    const response = await apiClient.get(`/api/plugins/${pluginId}/metrics`);
    return response.data;
  },

  // Get performance report
  getPerformanceReport: async (pluginId: string) => {
    const response = await apiClient.get(`/api/plugins/${pluginId}/performance-report`);
    return response.data;
  },

  // Get all plugin metrics
  getAllMetrics: async () => {
    const response = await apiClient.get('/api/plugins/metrics');
    return response.data;
  },
};
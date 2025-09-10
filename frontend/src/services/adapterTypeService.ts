import { api, ApiResponse } from './api';

export interface AdapterCategory {
  id: string;
  code: string;
  name: string;
  description?: string;
  icon?: string;
  parentCategoryId?: string;
  displayOrder: number;
}

export interface AdapterType {
  id: string;
  code: string;
  name: string;
  categoryId: string;
  categoryName?: string;
  vendor?: string;
  version?: string;
  description?: string;
  icon?: string;
  
  // Direction support
  supportsInbound: boolean;
  supportsOutbound: boolean;
  supportsBidirectional: boolean;
  
  // Configuration schemas
  inboundConfigSchema?: any;
  outboundConfigSchema?: any;
  commonConfigSchema?: any;
  
  // Capabilities and metadata
  capabilities?: any;
  supportedProtocols?: string[];
  supportedFormats?: string[];
  authenticationMethods?: string[];
  
  // Documentation and support
  documentationUrl?: string;
  supportUrl?: string;
  pricingTier?: string;
  
  // Status
  status: string;
  isCertified: boolean;
  certificationDate?: string;
  
  // Audit
  createdAt?: string;
  updatedAt?: string;
}

export interface ConfigurationSchema {
  direction: string;
  schema: any;
  hasAdvancedOptions?: boolean;
  requiresAuthentication?: boolean;
  supportedAuthMethods?: string[];
}

export interface AdapterTypeFilters {
  category?: string;
  search?: string;
  status?: string;
  direction?: 'inbound' | 'outbound' | 'bidirectional';
  page?: number;
  size?: number;
  sort?: string;
}

class AdapterTypeService {
  async getAdapterTypes(filters?: AdapterTypeFilters): Promise<ApiResponse<any>> {
    const params = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          params.append(key, value.toString());
        }
      });
    }
    
    return api.get(`/adapter-types${params.toString() ? `?${params.toString()}` : ''}`);
  }
  
  async getAdapterType(id: string): Promise<ApiResponse<AdapterType>> {
    return api.get(`/adapter-types/${id}`);
  }
  
  async getAdapterCategories(): Promise<ApiResponse<AdapterCategory[]>> {
    return api.get('/adapter-types/categories');
  }
  
  async getConfigurationSchema(
    typeId: string, 
    direction: 'inbound' | 'outbound' | 'bidirectional'
  ): Promise<ApiResponse<ConfigurationSchema>> {
    return api.get(`/adapter-types/${typeId}/schema/${direction}`);
  }
  
  async createAdapterType(adapterType: Partial<AdapterType>): Promise<ApiResponse<AdapterType>> {
    return api.post('/adapter-types', adapterType);
  }
  
  async updateAdapterType(id: string, updates: Partial<AdapterType>): Promise<ApiResponse<void>> {
    return api.put(`/adapter-types/${id}`, updates);
  }
  
  async getAdapterCountsByCategory(): Promise<ApiResponse<Record<string, number>>> {
    return api.get('/adapter-types/counts-by-category');
  }
}

export const adapterTypeService = new AdapterTypeService();
import { api, ApiError } from './api';

// Types for target field mappings
export interface TargetFieldMapping {
  id: string;
  orchestrationTargetId: string;
  sourceFieldPath: string;
  targetFieldPath: string;
  mappingType: 'DIRECT' | 'FUNCTION' | 'CONSTANT' | 'CONDITIONAL' | 'CONCATENATION' | 
                'SPLIT' | 'LOOKUP' | 'CALCULATION' | 'DATE_FORMAT' | 'CUSTOM';
  transformationExpression?: string;
  constantValue?: string;
  conditionExpression?: string;
  defaultValue?: string;
  targetDataType?: string;
  required: boolean;
  mappingOrder: number;
  visualFlowData?: string;
  validationRules?: string;
  description?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface CreateTargetFieldMappingRequest {
  sourceFieldPath: string;
  targetFieldPath: string;
  mappingType?: 'DIRECT' | 'FUNCTION' | 'CONSTANT' | 'CONDITIONAL' | 'CONCATENATION' | 
                 'SPLIT' | 'LOOKUP' | 'CALCULATION' | 'DATE_FORMAT' | 'CUSTOM';
  transformationExpression?: string;
  constantValue?: string;
  conditionExpression?: string;
  defaultValue?: string;
  targetDataType?: string;
  required?: boolean;
  mappingOrder?: number;
  visualFlowData?: string;
  validationRules?: string;
  description?: string;
  active?: boolean;
}

export interface UpdateTargetFieldMappingRequest {
  sourceFieldPath?: string;
  targetFieldPath?: string;
  mappingType?: 'DIRECT' | 'FUNCTION' | 'CONSTANT' | 'CONDITIONAL' | 'CONCATENATION' | 
                 'SPLIT' | 'LOOKUP' | 'CALCULATION' | 'DATE_FORMAT' | 'CUSTOM';
  transformationExpression?: string;
  constantValue?: string;
  conditionExpression?: string;
  defaultValue?: string;
  targetDataType?: string;
  required?: boolean;
  mappingOrder?: number;
  visualFlowData?: string;
  validationRules?: string;
  description?: string;
}

export interface MappingOrderRequest {
  mappingId: string;
  mappingOrder: number;
}

export interface MappingValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
  totalMappings: number;
  validMappings: number;
  requiredMappings: number;
  missingRequiredMappings: number;
}

// Service class for target field mapping operations
export class TargetFieldMappingService {
  /**
   * Get all mappings for a target
   */
  static async getTargetMappings(
    flowId: string, 
    targetId: string, 
    activeOnly: boolean = false
  ): Promise<TargetFieldMapping[]> {
    try {
      const params = activeOnly ? '?activeOnly=true' : '';
      const response = await api.get<TargetFieldMapping[]>(
        `/flows/${flowId}/targets/${targetId}/mappings${params}`
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to fetch target mappings');
    } catch (error) {
      console.error('Error fetching target mappings:', error);
      throw error;
    }
  }

  /**
   * Get a specific mapping
   */
  static async getMapping(
    flowId: string, 
    targetId: string, 
    mappingId: string
  ): Promise<TargetFieldMapping> {
    try {
      const response = await api.get<TargetFieldMapping>(
        `/flows/${flowId}/targets/${targetId}/mappings/${mappingId}`
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to fetch mapping');
    } catch (error) {
      console.error('Error fetching mapping:', error);
      throw error;
    }
  }

  /**
   * Create a field mapping
   */
  static async createMapping(
    flowId: string, 
    targetId: string, 
    request: CreateTargetFieldMappingRequest
  ): Promise<TargetFieldMapping> {
    try {
      const response = await api.post<TargetFieldMapping>(
        `/flows/${flowId}/targets/${targetId}/mappings`, 
        request
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to create mapping');
    } catch (error) {
      console.error('Error creating mapping:', error);
      throw error;
    }
  }

  /**
   * Update a field mapping
   */
  static async updateMapping(
    flowId: string, 
    targetId: string, 
    mappingId: string,
    request: UpdateTargetFieldMappingRequest
  ): Promise<TargetFieldMapping> {
    try {
      const response = await api.put<TargetFieldMapping>(
        `/flows/${flowId}/targets/${targetId}/mappings/${mappingId}`, 
        request
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to update mapping');
    } catch (error) {
      console.error('Error updating mapping:', error);
      throw error;
    }
  }

  /**
   * Delete a field mapping
   */
  static async deleteMapping(flowId: string, targetId: string, mappingId: string): Promise<void> {
    try {
      const response = await api.delete(`/flows/${flowId}/targets/${targetId}/mappings/${mappingId}`);
      if (!response.success) {
        throw new ApiError(response.error || 'Failed to delete mapping');
      }
    } catch (error) {
      console.error('Error deleting mapping:', error);
      throw error;
    }
  }

  /**
   * Create multiple mappings
   */
  static async createMappings(
    flowId: string, 
    targetId: string, 
    requests: CreateTargetFieldMappingRequest[]
  ): Promise<TargetFieldMapping[]> {
    try {
      const response = await api.post<TargetFieldMapping[]>(
        `/flows/${flowId}/targets/${targetId}/mappings/batch`, 
        requests
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to create mappings');
    } catch (error) {
      console.error('Error creating mappings:', error);
      throw error;
    }
  }

  /**
   * Delete all mappings for a target
   */
  static async deleteAllMappings(flowId: string, targetId: string): Promise<void> {
    try {
      const response = await api.delete(`/flows/${flowId}/targets/${targetId}/mappings`);
      if (!response.success) {
        throw new ApiError(response.error || 'Failed to delete all mappings');
      }
    } catch (error) {
      console.error('Error deleting all mappings:', error);
      throw error;
    }
  }

  /**
   * Activate a mapping
   */
  static async activateMapping(
    flowId: string, 
    targetId: string, 
    mappingId: string
  ): Promise<TargetFieldMapping> {
    try {
      const response = await api.post<TargetFieldMapping>(
        `/flows/${flowId}/targets/${targetId}/mappings/${mappingId}/activate`
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to activate mapping');
    } catch (error) {
      console.error('Error activating mapping:', error);
      throw error;
    }
  }

  /**
   * Deactivate a mapping
   */
  static async deactivateMapping(
    flowId: string, 
    targetId: string, 
    mappingId: string
  ): Promise<TargetFieldMapping> {
    try {
      const response = await api.post<TargetFieldMapping>(
        `/flows/${flowId}/targets/${targetId}/mappings/${mappingId}/deactivate`
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to deactivate mapping');
    } catch (error) {
      console.error('Error deactivating mapping:', error);
      throw error;
    }
  }

  /**
   * Reorder mappings
   */
  static async reorderMappings(
    flowId: string, 
    targetId: string, 
    orderRequests: MappingOrderRequest[]
  ): Promise<TargetFieldMapping[]> {
    try {
      const response = await api.put<TargetFieldMapping[]>(
        `/flows/${flowId}/targets/${targetId}/mappings/reorder`, 
        orderRequests
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to reorder mappings');
    } catch (error) {
      console.error('Error reordering mappings:', error);
      throw error;
    }
  }

  /**
   * Validate mappings for a target
   */
  static async validateMappings(flowId: string, targetId: string): Promise<MappingValidationResult> {
    try {
      const response = await api.get<MappingValidationResult>(
        `/flows/${flowId}/targets/${targetId}/mappings/validate`
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to validate mappings');
    } catch (error) {
      console.error('Error validating mappings:', error);
      throw error;
    }
  }

  /**
   * Helper to create default field mapping
   */
  static getDefaultMapping(): Partial<CreateTargetFieldMappingRequest> {
    return {
      mappingType: 'DIRECT',
      required: false,
      active: true,
      mappingOrder: 0,
    };
  }

  /**
   * Helper to generate transformation function templates
   */
  static getTransformationTemplates() {
    return {
      uppercase: 'value.toUpperCase()',
      lowercase: 'value.toLowerCase()',
      trim: 'value.trim()',
      substring: 'value.substring(0, 10)',
      replace: "value.replace('old', 'new')",
      concat: "value + ' suffix'",
      dateFormat: "new Date(value).toISOString()",
      parseInt: 'parseInt(value, 10)',
      parseFloat: 'parseFloat(value)',
      conditional: "value === 'condition' ? 'true_value' : 'false_value'",
      jsonParse: 'JSON.parse(value)',
      jsonStringify: 'JSON.stringify(value)',
    };
  }
}

export default TargetFieldMappingService;
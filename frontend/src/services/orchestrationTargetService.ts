import { api, ApiError, ApiResponse } from './api';

// Types for orchestration targets
export interface OrchestrationTarget {
  id: string;
  flowId: string;
  targetAdapter: {
    id: string;
    name: string;
    type: string;
    mode: string;
    active: boolean;
  };
  executionOrder: number;
  parallel: boolean;
  routingCondition?: string;
  conditionType: 'ALWAYS' | 'EXPRESSION' | 'HEADER_MATCH' | 'CONTENT_TYPE' | 'XPATH' | 'JSONPATH' | 'REGEX' | 'CUSTOM';
  structureId?: string;
  responseStructureId?: string;
  awaitResponse: boolean;
  timeoutMs: number;
  retryPolicy: {
    maxAttempts: number;
    retryDelayMs: number;
    backoffMultiplier: number;
    maxRetryDelayMs: number;
    retryOnErrors?: string;
  };
  errorStrategy: 'FAIL_FLOW' | 'SKIP_TARGET' | 'RETRY' | 'DEAD_LETTER' | 'COMPENSATE' | 'CUSTOM';
  active: boolean;
  configuration?: Record<string, any>;
  description?: string;
  createdAt: string;
  updatedAt: string;
  mappingCount: number;
}

export interface CreateOrchestrationTargetRequest {
  targetAdapterId: string;
  executionOrder?: number;
  parallel?: boolean;
  routingCondition?: string;
  conditionType?: 'ALWAYS' | 'EXPRESSION' | 'HEADER_MATCH' | 'CONTENT_TYPE' | 'XPATH' | 'JSONPATH' | 'REGEX' | 'CUSTOM';
  structureId?: string;
  responseStructureId?: string;
  awaitResponse?: boolean;
  timeoutMs?: number;
  retryPolicy?: {
    maxAttempts?: number;
    retryDelayMs?: number;
    backoffMultiplier?: number;
    maxRetryDelayMs?: number;
    retryOnErrors?: string;
  };
  errorStrategy?: 'FAIL_FLOW' | 'SKIP_TARGET' | 'RETRY' | 'DEAD_LETTER' | 'COMPENSATE' | 'CUSTOM';
  active?: boolean;
  configuration?: Record<string, any>;
  description?: string;
}

export interface UpdateOrchestrationTargetRequest {
  executionOrder?: number;
  parallel?: boolean;
  routingCondition?: string;
  conditionType?: 'ALWAYS' | 'EXPRESSION' | 'HEADER_MATCH' | 'CONTENT_TYPE' | 'XPATH' | 'JSONPATH' | 'REGEX' | 'CUSTOM';
  structureId?: string;
  responseStructureId?: string;
  awaitResponse?: boolean;
  timeoutMs?: number;
  retryPolicy?: {
    maxAttempts?: number;
    retryDelayMs?: number;
    backoffMultiplier?: number;
    maxRetryDelayMs?: number;
    retryOnErrors?: string;
  };
  errorStrategy?: 'FAIL_FLOW' | 'SKIP_TARGET' | 'RETRY' | 'DEAD_LETTER' | 'COMPENSATE' | 'CUSTOM';
  configuration?: Record<string, any>;
  description?: string;
}

export interface TargetOrderRequest {
  targetId: string;
  executionOrder: number;
}

// Service class for orchestration target operations
export class OrchestrationTargetService {
  /**
   * Get all targets for a flow
   */
  static async getFlowTargets(flowId: string): Promise<OrchestrationTarget[]> {
    try {
      const response = await api.get<OrchestrationTarget[]>(`/flows/${flowId}/targets`);
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to fetch flow targets');
    } catch (error) {
      console.error('Error fetching flow targets:', error);
      throw error;
    }
  }

  /**
   * Get a specific target
   */
  static async getTarget(flowId: string, targetId: string): Promise<OrchestrationTarget> {
    try {
      const response = await api.get<OrchestrationTarget>(`/flows/${flowId}/targets/${targetId}`);
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to fetch target');
    } catch (error) {
      console.error('Error fetching target:', error);
      throw error;
    }
  }

  /**
   * Add a target to a flow
   */
  static async addTarget(flowId: string, request: CreateOrchestrationTargetRequest): Promise<OrchestrationTarget> {
    try {
      const response = await api.post<OrchestrationTarget>(`/flows/${flowId}/targets`, request);
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to add target');
    } catch (error) {
      console.error('Error adding target:', error);
      throw error;
    }
  }

  /**
   * Update a target
   */
  static async updateTarget(
    flowId: string, 
    targetId: string, 
    request: UpdateOrchestrationTargetRequest
  ): Promise<OrchestrationTarget> {
    try {
      const response = await api.put<OrchestrationTarget>(`/flows/${flowId}/targets/${targetId}`, request);
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to update target');
    } catch (error) {
      console.error('Error updating target:', error);
      throw error;
    }
  }

  /**
   * Remove a target
   */
  static async removeTarget(flowId: string, targetId: string): Promise<void> {
    try {
      const response = await api.delete(`/flows/${flowId}/targets/${targetId}`);
      if (!response.success) {
        throw new ApiError(response.error || 'Failed to remove target');
      }
    } catch (error) {
      console.error('Error removing target:', error);
      throw error;
    }
  }

  /**
   * Activate a target
   */
  static async activateTarget(flowId: string, targetId: string): Promise<OrchestrationTarget> {
    try {
      const response = await api.post<OrchestrationTarget>(`/flows/${flowId}/targets/${targetId}/activate`);
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to activate target');
    } catch (error) {
      console.error('Error activating target:', error);
      throw error;
    }
  }

  /**
   * Deactivate a target
   */
  static async deactivateTarget(flowId: string, targetId: string): Promise<OrchestrationTarget> {
    try {
      const response = await api.post<OrchestrationTarget>(`/flows/${flowId}/targets/${targetId}/deactivate`);
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to deactivate target');
    } catch (error) {
      console.error('Error deactivating target:', error);
      throw error;
    }
  }

  /**
   * Reorder targets
   */
  static async reorderTargets(flowId: string, orderRequests: TargetOrderRequest[]): Promise<OrchestrationTarget[]> {
    try {
      const response = await api.put<OrchestrationTarget[]>(`/flows/${flowId}/targets/reorder`, orderRequests);
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to reorder targets');
    } catch (error) {
      console.error('Error reordering targets:', error);
      throw error;
    }
  }

  /**
   * Helper to create default retry policy
   */
  static getDefaultRetryPolicy() {
    return {
      maxAttempts: 3,
      retryDelayMs: 1000,
      backoffMultiplier: 2.0,
      maxRetryDelayMs: 60000,
    };
  }

  /**
   * Helper to create default target configuration
   */
  static getDefaultTargetConfig(): Partial<CreateOrchestrationTargetRequest> {
    return {
      parallel: false,
      conditionType: 'ALWAYS',
      awaitResponse: false,
      timeoutMs: 30000,
      retryPolicy: this.getDefaultRetryPolicy(),
      errorStrategy: 'FAIL_FLOW',
      active: true,
    };
  }
}

export default OrchestrationTargetService;
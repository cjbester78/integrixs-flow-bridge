import { apiClient } from '@/lib/api-client';

export interface TestConditionRequest {
  condition: string;
  conditionType: 'ALWAYS' | 'EXPRESSION' | 'HEADER_MATCH' | 'CONTENT_TYPE' | 'XPATH' | 'JSONPATH' | 'REGEX' | 'CUSTOM';
  payload: Record<string, any>;
  headers?: Record<string, string>;
  metadata?: Record<string, any>;
}

export interface TestConditionResponse {
  id: string;
  timestamp: string;
  condition: string;
  conditionType: string;
  result: boolean;
  executionTimeMs: number;
  error?: string;
  details?: {
    evaluatedExpression: string;
    variables: Record<string, any>;
    steps: Array<{
      description: string;
      result: any;
      durationMs?: number;
    }>;
  };
}

export class ConditionTestService {
  static async testCondition(request: TestConditionRequest): Promise<TestConditionResponse> {
    const response = await apiClient.post<TestConditionResponse>('/conditions/test', request);
    return response.data;
  }
  
  static async validateCondition(request: TestConditionRequest): Promise<TestConditionResponse> {
    const response = await apiClient.post<TestConditionResponse>('/conditions/validate', request);
    return response.data;
  }
}
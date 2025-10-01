import { apiClient } from '@/lib/api-client';
import type { AdapterType } from '@/types/communicationAdapter';

export interface ConnectionTestRequest {
  adapterType: AdapterType;
  adapterName: string;
  configuration: Record<string, any>;
  timeout?: number;
  performExtendedTests?: boolean;
  includeMetadata?: boolean;
}

export interface ConnectionDiagnostic {
  step: string;
  status: 'SUCCESS' | 'FAILED' | 'WARNING' | 'SKIPPED';
  message: string;
  duration: number;
  details?: Record<string, any>;
  errorCode?: string;
  stackTrace?: string;
}

export interface ConnectionTestResponse {
  success: boolean;
  message: string;
  diagnostics: ConnectionDiagnostic[];
  duration: number;
  timestamp: string;
  metadata?: Record<string, any>;
  healthScore?: number;
  recommendations?: string[];
}

export class ConnectionTestService {
  static async testConnection(request: ConnectionTestRequest): Promise<ConnectionTestResponse> {
    const response = await apiClient.post<ConnectionTestResponse>(
      '/adapters/connection/test',
      request
    );
    return response.data;
  }
  
  static async getSupportedTypes(): Promise<string[]> {
    const response = await apiClient.get<string[]>('/adapters/connection/test/supported-types');
    return response.data;
  }
}
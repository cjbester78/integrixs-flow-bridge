import { apiClient } from '@/lib/api-client';

export interface StructureCompatibilityRequest {
  sourceContent: string;
  sourceType: 'WSDL' | 'JSON_SCHEMA' | 'XSD';
  targetContent: string;
  targetType: 'WSDL' | 'JSON_SCHEMA' | 'XSD';
  includeDetailedAnalysis?: boolean;
  generateMappingSuggestions?: boolean;
}

export interface CompatibilityIssue {
  severity: 'ERROR' | 'WARNING' | 'INFO';
  category: 'TYPE_MISMATCH' | 'MISSING_FIELD' | 'FORMAT_DIFFERENCE' | 'CONSTRAINT_CONFLICT' | 'NAMESPACE_ISSUE' | 'OTHER';
  sourcePath: string;
  targetPath?: string;
  message: string;
  suggestion?: string;
}

export interface FieldMapping {
  sourcePath: string;
  targetPath: string;
  sourceType?: string;
  targetType?: string;
  compatible: boolean;
  transformationRequired: boolean;
  transformationHint?: string;
}

export interface StructureMetadata {
  fields: Array<{
    path: string;
    type: string;
    required: boolean;
  }>;
  namespaces?: Record<string, string>;
}

export interface StructureCompatibilityResponse {
  overallCompatibility: number;
  isCompatible: boolean;
  issues: CompatibilityIssue[];
  mappings: FieldMapping[];
  sourceMetadata?: StructureMetadata;
  targetMetadata?: StructureMetadata;
  recommendations: string[];
}

export class StructureCompatibilityService {
  static async analyzeCompatibility(request: StructureCompatibilityRequest): Promise<StructureCompatibilityResponse> {
    const response = await apiClient.post<StructureCompatibilityResponse>(
      '/structures/compatibility/analyze',
      request
    );
    return response.data;
  }
}
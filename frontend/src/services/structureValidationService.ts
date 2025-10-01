import { apiClient } from '@/lib/api-client';

export interface ValidateStructureRequest {
  content: string;
  structureType: 'WSDL' | 'JSON_SCHEMA' | 'XSD';
  strictMode?: boolean;
  extractMetadata?: boolean;
}

export interface StructureValidationResponse {
  valid: boolean;
  message?: string;
  issues: ValidationIssue[];
  wsdlMetadata?: WsdlMetadata;
  jsonSchemaMetadata?: JsonSchemaMetadata;
}

export interface ValidationIssue {
  type: 'ERROR' | 'WARNING' | 'INFO';
  message: string;
  line?: number;
  column?: number;
  path?: string;
}

export interface WsdlMetadata {
  targetNamespace?: string;
  version?: string;
  namespaces?: Record<string, string>;
  services?: ServiceInfo[];
  portTypes?: PortTypeInfo[];
  messages?: MessageInfo[];
}

export interface ServiceInfo {
  name: string;
  ports: PortInfo[];
}

export interface PortInfo {
  name: string;
  binding: string;
  address?: string;
}

export interface PortTypeInfo {
  name: string;
  operations: OperationInfo[];
}

export interface OperationInfo {
  name: string;
  inputMessage?: string;
  outputMessage?: string;
  faultMessages?: string[];
}

export interface MessageInfo {
  name: string;
  parts: PartInfo[];
}

export interface PartInfo {
  name: string;
  type?: string;
  element?: string;
}

export interface JsonSchemaMetadata {
  schema?: string;
  title?: string;
  description?: string;
  type?: string;
  properties?: Record<string, any>;
  required?: string[];
}

export class StructureValidationService {
  static async validateStructure(request: ValidateStructureRequest): Promise<StructureValidationResponse> {
    const response = await apiClient.post<StructureValidationResponse>('/structures/validate', request);
    return response.data;
  }
  
  static async extractWsdlOperations(request: ValidateStructureRequest): Promise<StructureValidationResponse> {
    const response = await apiClient.post<StructureValidationResponse>('/structures/wsdl/extract-operations', request);
    return response.data;
  }
}
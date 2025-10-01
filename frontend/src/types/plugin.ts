export interface Plugin {
  id: string;
  name: string;
  version: string;
  vendor: string;
  description?: string;
  icon?: string;
  category: string;
  supportedProtocols?: string[];
  supportedFormats?: string[];
  authenticationMethods?: string[];
  capabilities?: Record<string, any>;
  documentationUrl?: string;
  license?: string;
  tags?: string[];
}

export interface PluginDetails {
  metadata: Plugin;
  configurationSchema?: ConfigurationSchema;
  health?: HealthStatus;
  isInitialized: boolean;
}

export interface ConfigurationSchema {
  sections: ConfigurationSection[];
}

export interface ConfigurationSection {
  id: string;
  title: string;
  description?: string;
  fields: ConfigurationField[];
}

export interface ConfigurationField {
  name: string;
  type: string;
  label: string;
  required?: boolean;
  defaultValue?: any;
  placeholder?: string;
  help?: string;
  validation?: FieldValidation;
  options?: FieldOption[];
  condition?: FieldCondition;
}

export interface FieldValidation {
  min?: number;
  max?: number;
  minLength?: number;
  maxLength?: number;
  pattern?: string;
  message?: string;
}

export interface FieldOption {
  value: string;
  label: string;
  description?: string;
}

export interface FieldCondition {
  field: string;
  operator: 'equals' | 'notEquals' | 'in' | 'notIn';
  value: any;
}

export interface HealthStatus {
  state: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
  message: string;
  components?: ComponentHealth[];
  metrics?: PerformanceMetrics;
}

export interface ComponentHealth {
  name: string;
  state: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY' | 'STOPPED';
  message: string;
}

export interface PerformanceMetrics {
  messagesProcessed?: number;
  errors?: number;
  successRate?: number;
  averageResponseTime?: number;
}

export interface UploadResult {
  successful: boolean;
  pluginId?: string;
  metadata?: Plugin;
  warnings?: string[];
  error?: string;
}

export interface ValidationResult {
  valid: boolean;
  errors?: ValidationError[];
  message?: string;
}

export interface ValidationError {
  field: string;
  message: string;
  value?: string;
}

export interface ConnectionTestResult {
  successful: boolean;
  message: string;
  responseTime?: number;
  errorDetails?: string;
  systemInfo?: {
    name: string;
    version: string;
    vendor: string;
  };
}
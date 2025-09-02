export interface IntegrationPackage {
 id: string;
 name: string;
 description?: string;
 transformationRequired: boolean;
 syncType: 'SYNCHRONOUS' | 'ASYNCHRONOUS';
 status: 'DRAFT' | 'CONFIGURED' | 'DEPLOYED' | 'INACTIVE';
 sourceNamespace?: string;
 targetNamespace?: string;
 version: number;
 isTemplate: boolean;
 parentPackageId?: string;
 components?: PackageComponent[];
 createdBy: string;
 updatedBy: string;
 createdAt: string;
 updatedAt: string;
 componentCount?: number;
 lastDeploymentDate?: string;
 lastDeploymentStatus?: string;
}

export interface PackageComponent {
 id: string;
 componentType: ComponentType;
 componentId: string;
 isRequired: boolean;
 configuration?: Record<string, any>;
 createdAt: string;
 componentName?: string;
 componentDescription?: string;
}

export type ComponentType =
 | 'FLOW'
 | 'SOURCE_ADAPTER'
 | 'TARGET_ADAPTER'
 | 'SOURCE_STRUCTURE'
 | 'TARGET_STRUCTURE'
 | 'RESPONSE_STRUCTURE';

export interface CreatePackageRequest {
 name: string;
 description?: string;
 transformationRequired: boolean;
 syncType: 'SYNCHRONOUS' | 'ASYNCHRONOUS';
 sourceNamespace?: string;
 targetNamespace?: string;
}

export interface UpdatePackageRequest {
 name: string;
 description?: string;
 sourceNamespace?: string;
 targetNamespace?: string;
}

export interface PackageValidationResult {
 packageId: string;
 isValid: boolean;
 errors: string[];
 warnings: string[];
 validatedAt: string;
}

export interface PackageDeploymentRequest {
 environment: string;
 notes?: string;
}

export interface AddComponentRequest {
 componentType: ComponentType;
 componentId: string;
 configuration?: Record<string, any>;
}
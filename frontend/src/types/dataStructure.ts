// Re-export from dataStructures.ts for compatibility
export * from './dataStructures';

// Additional types for data structure management
export interface FlowStructure {
 id: string;
 name: string;
 wsdlContent: string;
 operations: string[];
 createdAt: string;
 updatedAt: string;
}
export type AdapterType = 'FILE' | 'FTP' | 'SFTP' | 'HTTP' | 'REST' | 'SOAP' |
 'IBMMQ' | 'JDBC' | 'IDOC' | 'ODATA' | 'MAIL' | 'RFC';

export type AdapterMode = 'INBOUND' | 'OUTBOUND';

export interface CommunicationAdapter {
 id: string;
 name: string;
 type: AdapterType;
 mode: AdapterMode;
 direction?: string;
 configuration: any;
 isActive: boolean;
 description?: string;
 businessComponentId?: string;
 createdAt: string;
 updatedAt: string;
 createdBy?: string;
 updatedBy?: string;
}

// Helper function to determine if adapter requires Flow Structures
export const requiresFlowStructure = (adapterType: AdapterType): boolean => {
 return adapterType === 'SOAP';
};
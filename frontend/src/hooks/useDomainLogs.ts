import { useState, useEffect, useCallback } from 'react';
import { api } from '@/services/api';
import { useSystemLogs } from './useSystemLogs';
import { logger, LogCategory } from '@/lib/logger';

// Domain-specific error types
export interface DomainErrorEntry {
 id: string;
 action: string;
 description?: string;
 payload: string;
 createdAt: string;
 systemLogId?: string;
 userId?: string;
}
export interface UserManagementError extends DomainErrorEntry {
 userId?: string;
}
export interface FlowManagementError extends DomainErrorEntry {
 flowId?: string;
}
export interface AdapterManagementError extends DomainErrorEntry {
 adapterId?: string;
 adapterType?: 'source' | 'target' | 'bidirectional';
}
export interface StructureManagementError extends DomainErrorEntry {
 structureId?: string;
 structureType?: 'XSD' | 'JSON' | 'WSDL' | 'EDMX' | 'Custom';
}
export interface ChannelManagementError extends DomainErrorEntry {
 channelId?: string;
}
export interface MessageProcessingError extends DomainErrorEntry {
 messageId?: string;
 flowId?: string;
 channelId?: string;
}
export type DomainType = 'UserManagement' | 'FlowEngine' | 'AdapterManagement' | 'DataStructures' | 'ChannelManagement' | 'MessageProcessing';

interface UseDomainLogsParams {
 domainType: DomainType;
 referenceId?: string;
 includeSystemLogs?: boolean;
 autoRefresh?: boolean;
 refreshInterval?: number;
}

export const useDomainLogs = (params: UseDomainLogsParams) => {
 const [domainErrors, setDomainErrors] = useState<DomainErrorEntry[]>([]);
 const [loading, setLoading] = useState(false);
 const [error, setError] = useState<string>();

 // Get system logs for this domain
 const {
 logs: systemLogs,
 loading: systemLogsLoading,
 error: systemLogsError,
 } = useSystemLogs({
 domainType: params.domainType,
 domainReferenceId: params.referenceId,
 autoRefresh: params.includeSystemLogs ? params.autoRefresh : false,
 refreshInterval: params.refreshInterval,
 });


 const fetchDomainErrors = useCallback(async () => {
 setLoading(true);
 setError(undefined);

 try {
// Use the consolidated system logs endpoint with domain type filter
 const queryParams = new URLSearchParams();
 queryParams.append('domainType', params.domainType);
 queryParams.append('level', 'error'); // Only fetch error-level logs
 if (params.referenceId) {
 queryParams.append('domainReferenceId', params.referenceId);
 }
 const endpoint = `/logs/system?${queryParams.toString()}`;
 const response = await api.get(endpoint);
 if (response.success && response.data) {
 setDomainErrors(response.data);
 } else {
 // API returned empty data, set empty array
 setDomainErrors([]);
 logger.info(LogCategory.BUSINESS_LOGIC, `No domain errors found for ${params.domainType}`);
 }
} catch (err) {
 // API error, set empty array instead of mock data
 setDomainErrors([]);
 setError(`Failed to fetch domain errors for ${params.domainType}`);
 logger.error(LogCategory.BUSINESS_LOGIC, 'Error occurred', { error: { data: `Failed to fetch domain errors for ${params.domainType}`, extra: err } });
 } finally {
 setLoading(false);
 }
 }, [params.domainType, params.referenceId]);

 const refetch = () => {
 fetchDomainErrors();
 };

 // Fetch domain errors when parameters change
 useEffect(() => {
 fetchDomainErrors();
 }, [params.domainType, params.referenceId, fetchDomainErrors]);

 // Auto refresh setup
 useEffect(() => {
 if (params.autoRefresh) {
 const interval = setInterval(fetchDomainErrors, params.refreshInterval || 30000);
 return () => clearInterval(interval);
 }
 return undefined;
 }, [params.autoRefresh, params.refreshInterval, fetchDomainErrors]);

 return {
 domainErrors: domainErrors || [],
 systemLogs: params.includeSystemLogs ? (systemLogs || []) : [],
 loading: loading || (params.includeSystemLogs ? systemLogsLoading : false),
 error: error || (params.includeSystemLogs ? systemLogsError : undefined),
 refetch,
 };
};

// Helper functions

// Specific domain hooks
export const useUserManagementLogs = (userId?: string) => {
 return useDomainLogs({
 domainType: 'UserManagement',
 referenceId: userId,
 includeSystemLogs: true,
 });
};

export const useFlowManagementLogs = (flowId?: string) => {
 return useDomainLogs({
 domainType: 'FlowEngine',
 referenceId: flowId,
 includeSystemLogs: true,
 });
};

export const useAdapterManagementLogs = (adapterId?: string) => {
 return useDomainLogs({
 domainType: 'AdapterManagement',
 referenceId: adapterId,
 includeSystemLogs: true,
 });
};

export const useStructureManagementLogs = (structureId?: string) => {
 return useDomainLogs({
 domainType: 'DataStructures',
 referenceId: structureId,
 includeSystemLogs: true,
 });
};

export const useChannelManagementLogs = (channelId?: string) => {
 return useDomainLogs({
 domainType: 'ChannelManagement',
 referenceId: channelId,
 includeSystemLogs: true,
 });
};

export const useMessageProcessingLogs = (messageId?: string, flowId?: string, channelId?: string) => {
 const referenceId = messageId || flowId || channelId;
 return useDomainLogs({
 domainType: 'MessageProcessing',
 referenceId: referenceId,
 includeSystemLogs: true,
 });
};
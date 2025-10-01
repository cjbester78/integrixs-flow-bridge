import { api } from './api';

export type AdapterMonitoringStatus = 'active' | 'inactive' | 'error' | 'running' | 'idle' | 'stopped';
export type LogLevel = 'INFO' | 'WARN' | 'ERROR' | 'SUCCESS' | 'error' | 'warn' | 'info' | 'debug';

export interface AdapterMonitoring {
 id: string;
 name: string;
 type: string;
 mode: string; // INBOUND or OUTBOUND
 status: AdapterMonitoringStatus;
 load: number;
 businessComponentId?: string;
 businessComponentName?: string;
 messagesProcessed?: number;
 errorsCount?: number;
 lastActivityTime?: string;
}

export interface AdapterLog {
 id: string;
 timestamp: string;
 level: LogLevel;
 message: string;
 adapterId: string;
 details?: any;
 adapterName?: string;
 correlationId?: string;
 duration?: number;
}

export interface FlowMetrics {
 totalMessages: number;
 successfulMessages: number;
 failedMessages: number;
 averageProcessingTime: string;
 lastProcessed?: string;
}

// Remove the old Channel interface as we now use AdapterMonitoring

export interface AdapterLogsFilters {
 level?: LogLevel[];
 dateFrom?: string;
 dateTo?: string;
 search?: string;
 limit?: number;
}

class AdapterMonitoringService {
 async getAdapters(businessComponentId?: string): Promise<{ success: boolean; data?: AdapterMonitoring[]; error?: string }> {
 try {
 const endpoint = businessComponentId ? `/adapters?businessComponentId=${businessComponentId}` : '/adapters';
 return await api.get<AdapterMonitoring[]>(endpoint);
 } catch (error) {
 return {
 success: false,
 error: error instanceof Error ? error.message : 'Failed to fetch adapters'
 };
 }
 }

 async getAdapterById(adapterId: string): Promise<{ success: boolean; data?: AdapterMonitoring; error?: string }> {
 try {
 return await api.get<AdapterMonitoring>(`/adapters/${adapterId}`);
 } catch (error) {
 return {
 success: false,
 error: error instanceof Error ? error.message : 'Failed to fetch adapter'
 };
 }
 }

 async createAdapter(adapter: Omit<AdapterMonitoring, 'id'>): Promise<{ success: boolean; data?: AdapterMonitoring; error?: string }> {
 try {
 return await api.post<AdapterMonitoring>('/adapters', adapter);
 } catch (error) {
 return {
 success: false,
 error: error instanceof Error ? error.message : 'Failed to create adapter'
 };
 }
 }

 async updateAdapter(adapterId: string, updates: Partial<AdapterMonitoring>): Promise<{ success: boolean; data?: AdapterMonitoring; error?: string }> {
 try {
 return await api.put<AdapterMonitoring>(`/adapters/${adapterId}`, updates);
 } catch (error) {
 return {
 success: false,
 error: error instanceof Error ? error.message : 'Failed to update adapter'
 };
 }
 }

 async deleteAdapter(adapterId: string): Promise<{ success: boolean; error?: string }> {
 try {
 await api.delete(`/adapters/${adapterId}`);
 return { success: true };
 } catch (error) {
 return {
 success: false,
 error: error instanceof Error ? error.message : 'Failed to delete adapter'
 };
 }
 }

 async getAdapterLogs(adapterId: string, filters?: AdapterLogsFilters): Promise<{ success: boolean; data?: AdapterLog[]; error?: string }> {
 try {
 const params = new URLSearchParams();
 // Add pagination parameters
 params.append('page', '0');
 params.append('size', filters?.limit?.toString() || '50');

 if (filters?.level?.length) params.append('level', filters.level.join(','));
 if (filters?.dateFrom) params.append('dateFrom', filters.dateFrom);
 if (filters?.dateTo) params.append('dateTo', filters.dateTo);
 if (filters?.search) params.append('search', filters.search);

 const queryString = params.toString() ? `?${params.toString()}` : '';
 const response = await api.get<any>(`/adapters/${adapterId}/logs${queryString}`);
 // Handle paginated response format
 if (response.success && response.data) {
 // Extract content array from paginated response
 const rawLogs = response.data.content || [];
 // Map backend log format to frontend AdapterLog interface
 const logs: AdapterLog[] = rawLogs.map((log: any) => ({
 id: log.id,
 timestamp: log.timestamp,
 level: (log.level || 'INFO').toLowerCase() as LogLevel, // Convert uppercase to lowercase
 message: log.message,
 adapterId: adapterId, // Use the adapterId from the request
 details: log.details ? (typeof log.details === 'string' ? JSON.parse(log.details) : log.details) : undefined,
 adapterName: log.sourceName,
 correlationId: log.correlationId,
 duration: undefined // Backend doesn't provide duration in this format
 }));

 return {
 success: true,
 data: logs
 };
 }

 return response;
 } catch (error) {
 return {
 success: false,
 error: error instanceof Error ? error.message : 'Failed to fetch adapter logs'
 };
 }
 }

 async getAvailableAdapterTypes(): Promise<{ success: boolean; data?: string[]; error?: string }> {
 try {
 return await api.get<string[]>('/admin/adapter-types');
 } catch (error) {
 return {
 success: false,
 error: error instanceof Error ? error.message : 'Failed to fetch available adapter types'
 };
 }
 }

 async testAdapterConnection(adapterId: string): Promise<{ success: boolean; error?: string }> {
 try {
 await api.post(`/adapters/${adapterId}/test`);
 return { success: true };
 } catch (error) {
 return {
 success: false,
 error: error instanceof Error ? error.message : 'Failed to test adapter connection'
 };
 }
 }
}

export const adapterMonitoringService = new AdapterMonitoringService();
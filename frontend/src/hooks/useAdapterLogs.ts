import { useState, useEffect, useRef, useCallback } from 'react';
import { adapterMonitoringService, AdapterLog, AdapterLogsFilters } from '@/services/adapterMonitoringService';
import { logger, LogCategory } from '@/lib/logger';

export const useAdapterLogs = (adapterId: string, filters?: AdapterLogsFilters, autoLoad: boolean = false) => {
 const [logs, setLogs] = useState<AdapterLog[]>([]);
 const [loading, setLoading] = useState(false);
 const [error, setError] = useState<string | null>(null);

 // Use a ref to track filters to avoid dependency issues
 const filtersRef = useRef(filters);
 filtersRef.current = filters;

 const loadLogs = useCallback(async () => {
 if (!adapterId) return;

 setLoading(true);
 setError(null);

 try {
 logger.info(LogCategory.BUSINESS_LOGIC, `[useAdapterLogs] Loading logs for adapter: ${adapterId}`);
 const response = await adapterMonitoringService.getAdapterLogs(adapterId, filtersRef.current);
 logger.info(LogCategory.BUSINESS_LOGIC, '[useAdapterLogs] Response:', { data: response });
 if (response.success && response.data) {
 logger.info(LogCategory.BUSINESS_LOGIC, `[useAdapterLogs] Setting ${response.data.length} logs`);
 setLogs(response.data);
 } else {
 logger.error(LogCategory.BUSINESS_LOGIC, '[useAdapterLogs] Error:', response.error);
 setError(response.error || 'Failed to load adapter logs');
 setLogs([]);
 }
} catch (err) {
 logger.error(LogCategory.BUSINESS_LOGIC, '[useAdapterLogs] Exception:', err);
 setError(err instanceof Error ? err.message : 'An unexpected error occurred');
 setLogs([]);
 } finally {
 setLoading(false);
 }
 }, [adapterId]);

 useEffect(() => {
 if (adapterId && autoLoad) {
 loadLogs();
 }
 }, [adapterId, autoLoad, loadLogs]); // Removed filters from dependencies

 return {
 logs,
 loading,
 error,
 refreshLogs: loadLogs,
 connected: false,
 exportLogs: async () => {
 logger.info(LogCategory.BUSINESS_LOGIC, 'Export logs functionality will be implemented');
 }
 };
}
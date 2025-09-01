import { useState, useEffect, useRef } from 'react';
import { adapterMonitoringService, AdapterLog, AdapterLogsFilters } from '@/services/adapterMonitoringService';

export const useAdapterLogs = (adapterId: string, filters?: AdapterLogsFilters, autoLoad: boolean = false) => {
  const [logs, setLogs] = useState<AdapterLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Use a ref to track filters to avoid dependency issues
  const filtersRef = useRef(filters);
  filtersRef.current = filters;

  useEffect(() => {
    if (adapterId && autoLoad) {
      loadLogs();
    }
  }, [adapterId, autoLoad]); // Removed filters from dependencies

  const loadLogs = async () => {
    if (!adapterId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      console.log(`[useAdapterLogs] Loading logs for adapter: ${adapterId}`);
      const response = await adapterMonitoringService.getAdapterLogs(adapterId, filtersRef.current);
      console.log('[useAdapterLogs] Response:', response);
      
      if (response.success && response.data) {
        console.log(`[useAdapterLogs] Setting ${response.data.length} logs`);
        setLogs(response.data);
      } else {
        console.error('[useAdapterLogs] Error:', response.error);
        setError(response.error || 'Failed to load adapter logs');
        setLogs([]);
      }
    } catch (err) {
      console.error('[useAdapterLogs] Exception:', err);
      setError(err instanceof Error ? err.message : 'An unexpected error occurred');
      setLogs([]);
    } finally {
      setLoading(false);
    }
  };

  return {
    logs,
    loading,
    error,
    refreshLogs: loadLogs,
    connected: false,
    exportLogs: async () => {
      console.log('Export logs functionality will be implemented');
    },
  };
};
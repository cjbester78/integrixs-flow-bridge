import { useState, useEffect, useCallback, useRef } from 'react';
import { integrationFlowService, IntegrationFlow, IntegrationFlowStats, IntegrationFlowFilters } from '@/services/integrationFlowService';
import { useToast } from '@/hooks/use-toast';
import { logger, LogCategory } from '@/lib/logger';

export const useIntegrationFlowMonitoring = (businessComponentId?: string) => {
  const [integrationFlows, setIntegrationFlows] = useState<IntegrationFlow[]>([]);
  const [stats, setStats] = useState<IntegrationFlowStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [connected, setConnected] = useState(false);
  const [currentFilters, setCurrentFilters] = useState<IntegrationFlowFilters | undefined>();
  const { toast } = useToast();

  // Load initial data
  const loadIntegrationFlows = useCallback(async (filters?: IntegrationFlowFilters) => {
    setLoading(true);
    setCurrentFilters(filters); // Store current filters
    try {
      const response = businessComponentId
        ? await integrationFlowService.getBusinessComponentIntegrationFlows(businessComponentId, filters)
        : await integrationFlowService.getIntegrationFlows(filters);

      if (response.success && response.data) {
        logger.info(LogCategory.BUSINESS_LOGIC, `[useIntegrationFlowMonitoring] Integration flows data:`, { data: response.data.integrationFlows });
        // Check first integration flow for debugging
        if (response.data.integrationFlows && response.data.integrationFlows.length > 0) {
          logger.info(LogCategory.BUSINESS_LOGIC, `[useIntegrationFlowMonitoring] First integration flow:`, { data: response.data.integrationFlows[0] });
          logger.info(LogCategory.BUSINESS_LOGIC, `[useIntegrationFlowMonitoring] First integration flow timestamp:`, { data: response.data.integrationFlows[0].timestamp });
          logger.info(LogCategory.BUSINESS_LOGIC, `[useIntegrationFlowMonitoring] Timestamp type:`, { data: typeof response.data.integrationFlows[0].timestamp });
        }
        setIntegrationFlows(response.data.integrationFlows || []);
      } else {
        // Ensure integration flows is always an array even on error
        setIntegrationFlows([]);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load integration flows",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  }, [businessComponentId, toast]);

  const loadStats = useCallback(async (filters?: Omit<IntegrationFlowFilters, 'limit' | 'offset'>) => {
    try {
      // Add businessComponentId to filters if present
      const statsFilters = businessComponentId
        ? { ...filters, source: businessComponentId }
        : filters;

      const response = await integrationFlowService.getIntegrationFlowStats(statsFilters);
      if (response.success && response.data) {
        setStats(response.data);
      }
    } catch (error) {
      logger.error(LogCategory.BUSINESS_LOGIC, 'Failed to load integration flow stats', { error: error });
    }
  }, [businessComponentId]);

  // Real-time updates
  useEffect(() => {
    // Connect WebSocket with filters if available
    const connectionParams = {
      businessComponentId,
      ...(currentFilters && {
        flowId: currentFilters.flowId,
        status: currentFilters.status,
        startDate: currentFilters.startDate,
        endDate: currentFilters.endDate,
      })
    };
    
    integrationFlowService.connectWebSocket(businessComponentId, connectionParams);
    setConnected(true);

    // Subscribe to integration flow updates
    const unsubscribeIntegrationFlows = integrationFlowService.onIntegrationFlowUpdate((newIntegrationFlow) => {
      setIntegrationFlows(prev => {
        const existingIndex = prev.findIndex(f => f.id === newIntegrationFlow.id);
        if (existingIndex >= 0) {
          // Update existing integration flow
          const updated = [...prev];
          updated[existingIndex] = newIntegrationFlow;
          return updated;
        } else {
          // Add new integration flow at the beginning
          return [newIntegrationFlow, ...prev];
        }
      });

      // Show toast for failed integration flows
      if (newIntegrationFlow.status === 'failed') {
        toast({
          title: "Integration Flow Failed",
          description: `Integration flow ${newIntegrationFlow.id} failed processing`,
          variant: "destructive",
        });
      }
    });

    // Subscribe to stats updates with filter support
    const unsubscribeStats = integrationFlowService.onStatsUpdate((newStats) => {
      // Only update stats if they match current filters
      // The backend should already be sending filtered stats based on connectionParams
      setStats(newStats);
    });

    // Don't load initial data - let the parent component control this with filters

    // Cleanup on unmount
    return () => {
      unsubscribeIntegrationFlows();
      unsubscribeStats();
      integrationFlowService.disconnectWebSocket();
      setConnected(false);
    };
  }, [businessComponentId, loadIntegrationFlows, loadStats, toast, currentFilters]);

  const reprocessIntegrationFlow = useCallback(async (integrationFlowId: string) => {
    try {
      const response = await integrationFlowService.reprocessIntegrationFlow(integrationFlowId);
      if (response.success) {
        toast({
          title: "Success",
          description: "Integration flow queued for reprocessing",
        });
        // The WebSocket will handle the real-time update
      } else {
        throw new Error(response.error || 'Failed to reprocess integration flow');
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to reprocess integration flow",
        variant: "destructive",
      });
    }
  }, [toast]);

  const refreshData = useCallback((filters?: IntegrationFlowFilters) => {
    loadIntegrationFlows(filters);
    loadStats(filters);
  }, [loadIntegrationFlows, loadStats]);

  const subscribeToIntegrationFlowType = useCallback((integrationFlowType: string) => {
    integrationFlowService.sendCommand('subscribe', { integrationFlowType });
  }, []);

  const unsubscribeFromIntegrationFlowType = useCallback((integrationFlowType: string) => {
    integrationFlowService.sendCommand('unsubscribe', { integrationFlowType });
  }, []);

  return {
    integrationFlows,
    stats,
    loading,
    connected,
    loadIntegrationFlows,
    loadStats,
    reprocessIntegrationFlow,
    refreshData,
    subscribeToIntegrationFlowType,
    unsubscribeFromIntegrationFlowType,
    // Keep original names for backward compatibility with components that haven't been updated yet
    messages: integrationFlows,
    loadMessages: loadIntegrationFlows,
    reprocessMessage: reprocessIntegrationFlow,
  };
};
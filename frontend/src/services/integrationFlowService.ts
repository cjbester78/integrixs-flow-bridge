import { api, ApiResponse } from './api';
import { logger, LogCategory } from '@/lib/logger';

export type IntegrationFlowStatus = 'success' | 'failed' | 'processing';

export interface IntegrationFlowLog {
  timestamp: string;
  level: string;
  message: string;
}

export interface IntegrationFlow {
  id: string;
  timestamp: string;
  source: string;
  target: string;
  type: string;
  status: IntegrationFlowStatus;
  processingTime: string;
  size: string;
  businessComponentId: string;
  logs: IntegrationFlowLog[];
}

export interface IntegrationFlowFilters {
  status?: IntegrationFlowStatus[];
  source?: string;
  target?: string;
  type?: string;
  dateFrom?: string;
  dateTo?: string;
  search?: string;
}

export interface IntegrationFlowStats {
  total: number;
  successful: number;
  processing: number;
  failed: number;
  successRate: number;
  avgProcessingTime: number;
}

class IntegrationFlowService {
  private websocket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;
  private integrationFlowListeners: ((integrationFlow: IntegrationFlow) => void)[] = [];
  private statsListeners: ((stats: IntegrationFlowStats) => void)[] = [];

  // Get all integration flows with optional filtering
  async getIntegrationFlows(filters?: IntegrationFlowFilters): Promise<ApiResponse<{ integrationFlows: IntegrationFlow[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          if (Array.isArray(value)) {
            queryParams.append(key, value.join(','));
          } else {
            queryParams.append(key, value.toString());
          }
        }
      });
    }

    const endpoint = `/flow-executions${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Get a specific integration flow execution by ID
  async getIntegrationFlow(id: string): Promise<ApiResponse<IntegrationFlow>> {
    return api.get<IntegrationFlow>(`/flow-executions/${id}`);
  }

  // Get integration flow statistics
  async getIntegrationFlowStats(filters?: Omit<IntegrationFlowFilters, 'limit' | 'offset'>): Promise<ApiResponse<IntegrationFlowStats>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          if (Array.isArray(value)) {
            queryParams.append(key, value.join(','));
          } else {
            queryParams.append(key, value.toString());
          }
        }
      });
    }

    const endpoint = `/flow-executions/stats${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Reprocess a failed integration flow execution
  async reprocessIntegrationFlow(id: string): Promise<ApiResponse<IntegrationFlow>> {
    return api.post<IntegrationFlow>(`/flow-executions/${id}/reprocess`);
  }

  // Get integration flows for a specific businessComponent
  async getBusinessComponentIntegrationFlows(businessComponentId: string, filters?: Omit<IntegrationFlowFilters, 'businessComponentId'>): Promise<ApiResponse<{ integrationFlows: IntegrationFlow[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          if (Array.isArray(value)) {
            queryParams.append(key, value.join(','));
          } else {
            queryParams.append(key, value.toString());
          }
        }
      });
    }

    const endpoint = `/business-components/${businessComponentId}/flow-executions${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // WebSocket Real-time Updates
  connectWebSocket(businessComponentId?: string, connectionParams?: Record<string, any>): void {

    if (this.websocket?.readyState === WebSocket.OPEN) {
      return; // Already connected
    }

    // Use dynamic host detection for WebSocket
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.hostname;
    const port = window.location.port || '8080';

    // Build query parameters
    const params = new URLSearchParams();
    if (businessComponentId) {
      params.append('businessComponentId', businessComponentId);
    }
    // Add additional connection parameters for filtering
    if (connectionParams) {
      Object.entries(connectionParams).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          params.append(key, String(value));
        }
      });
    }

    const queryString = params.toString();
    const baseUrl = `${protocol}//${host}:${port}`;
    const wsUrl = `${baseUrl}/ws/flow-executions${queryString ? `?${queryString}` : ''}`;

    try {
      this.websocket = new WebSocket(wsUrl);

      this.websocket.onopen = () => {
        logger.info(LogCategory.API, 'WebSocket connected for integration flow execution monitoring');
        this.reconnectAttempts = 0;
      };

      this.websocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);

          if (data.type === 'integration_flow_update') {
            this.integrationFlowListeners.forEach(listener => listener(data.integrationFlow));
          } else if (data.type === 'stats_update') {
            this.statsListeners.forEach(listener => listener(data.stats));
          }
        } catch (error) {
          logger.error(LogCategory.API, 'Error parsing WebSocket message', { error: error });
        }
      };

      this.websocket.onclose = () => {
        logger.info(LogCategory.API, 'WebSocket connection closed');
        this.attemptReconnect(businessComponentId, connectionParams);
      };

      this.websocket.onerror = (error) => {
        logger.error(LogCategory.API, 'WebSocket error', { error: error });
      };
    } catch (error) {
      logger.error(LogCategory.API, 'Failed to create WebSocket connection', { error: error });
    }
  }

  private attemptReconnect(businessComponentId?: string, connectionParams?: Record<string, any>): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      logger.info(LogCategory.API, `Attempting to reconnect WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

      setTimeout(() => {
        this.connectWebSocket(businessComponentId, connectionParams);
      }, this.reconnectInterval * this.reconnectAttempts);
    } else {
      logger.error(LogCategory.API, 'Max reconnection attempts reached');
    }
  }

  disconnectWebSocket(): void {
    if (this.websocket) {
      this.websocket.close();
      this.websocket = null;
    }
    this.integrationFlowListeners = [];
    this.statsListeners = [];
    this.reconnectAttempts = 0;
  }

  // Event listeners for real-time updates
  onIntegrationFlowUpdate(callback: (integrationFlow: IntegrationFlow) => void): () => void {
    this.integrationFlowListeners.push(callback);

    // Return unsubscribe function
    return () => {
      const index = this.integrationFlowListeners.indexOf(callback);
      if (index > -1) {
        this.integrationFlowListeners.splice(index, 1);
      }
    };
  }

  onStatsUpdate(callback: (stats: IntegrationFlowStats) => void): () => void {
    this.statsListeners.push(callback);

    // Return unsubscribe function
    return () => {
      const index = this.statsListeners.indexOf(callback);
      if (index > -1) {
        this.statsListeners.splice(index, 1);
      }
    };
  }

  // Send WebSocket command (e.g., subscribe to specific integration flow types)
  sendCommand(command: string, data?: any): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      this.websocket.send(JSON.stringify({ command, data }));
    }
  }
}

export const integrationFlowService = new IntegrationFlowService();
import { logger, LogCategory } from '@/lib/logger';

export class ChannelWebSocket {
 private websocket: WebSocket | null = null;
 private reconnectAttempts = 0;
 private maxReconnectAttempts = 5;
 private reconnectInterval = 3000;
 private channelListeners: ((channel: any) => void)[] = [];
 private statsListeners: ((stats: any) => void)[] = [];
 private alertListeners: ((alert: any) => void)[] = [];

 // WebSocket Real-time Updates
 connectWebSocket(businessComponentId?: string): void {
 if (this.websocket?.readyState === WebSocket.OPEN) {
 return;
 }

 const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/channels${businessComponentId ? `?businessComponentId=${businessComponentId}` : ''}`;

 try {
 this.websocket = new WebSocket(wsUrl);

 this.websocket.onopen = () => {
 logger.info(LogCategory.API, 'WebSocket connected for channel monitoring');
 this.reconnectAttempts = 0;
 };

 this.websocket.onmessage = (event) => {
 try {
 const data = JSON.parse(event.data);
 if (data.type === 'channel_update') {
 this.channelListeners.forEach(listener => listener(data.channel));
 } else if (data.type === 'stats_update') {
 this.statsListeners.forEach(listener => listener(data.stats));
 } else if (data.type === 'channel_alert') {
 this.alertListeners.forEach(listener => listener(data.alert));
 } else if (data.type === 'channel_log') {
 this.channelListeners.forEach(listener => listener(data));
 }
 } catch (error) {
 logger.error(LogCategory.API, 'Error parsing WebSocket message', { error: error });
 }
 };

 this.websocket.onclose = () => {
 logger.info(LogCategory.API, 'WebSocket connection closed');
 this.attemptReconnect(businessComponentId);
 };

 this.websocket.onerror = (error) => {
 logger.error(LogCategory.API, 'WebSocket error', { error: error });
 };
 } catch (error) {
 logger.error(LogCategory.API, 'Failed to create WebSocket connection', { error: error });
 }
 }

 private attemptReconnect(businessComponentId?: string): void {
 if (this.reconnectAttempts < this.maxReconnectAttempts) {
 this.reconnectAttempts++;
 logger.info(LogCategory.API, `Attempting to reconnect WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

 setTimeout(() => {
 this.connectWebSocket(businessComponentId);
 }, this.reconnectInterval * this.reconnectAttempts);
 }
 }

 disconnectWebSocket(): void {
 if (this.websocket) {
 this.websocket.close();
 this.websocket = null;
 }
 this.channelListeners = [];
 this.statsListeners = [];
 this.alertListeners = [];
 this.reconnectAttempts = 0;
 }

 // Event listeners
 onChannelUpdate(callback: (channel: any) => void): () => void {
 this.channelListeners.push(callback);
 return () => {
 const index = this.channelListeners.indexOf(callback);
 if (index > -1) this.channelListeners.splice(index, 1);
 };
 }

 onStatsUpdate(callback: (stats: any) => void): () => void {
 this.statsListeners.push(callback);
 return () => {
 const index = this.statsListeners.indexOf(callback);
 if (index > -1) this.statsListeners.splice(index, 1);
 };
 }

 onAlert(callback: (alert: any) => void): () => void {
 this.alertListeners.push(callback);
 return () => {
 const index = this.alertListeners.indexOf(callback);
 if (index > -1) this.alertListeners.splice(index, 1);
 };
 }

 sendCommand(command: string, data?: any): void {
 if (this.websocket?.readyState === WebSocket.OPEN) {
 this.websocket.send(JSON.stringify({ command, data }));
 }
 }

 // Real-time log streaming
 connectLogStream(channelId: string): void {
 if (this.websocket?.readyState === WebSocket.OPEN) {
 this.sendCommand('subscribe_channel_logs', { channelId });
 }
 }

 disconnectLogStream(channelId: string): void {
 if (this.websocket?.readyState === WebSocket.OPEN) {
 this.sendCommand('unsubscribe_channel_logs', { channelId });
 }
 }
}
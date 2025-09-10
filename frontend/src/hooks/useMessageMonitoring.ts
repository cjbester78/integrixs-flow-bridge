import { useState, useEffect, useCallback, useRef } from 'react';
import { messageService, Message, MessageStats, MessageFilters } from '@/services/messageService';
import { useToast } from '@/hooks/use-toast';
import { logger, LogCategory } from '@/lib/logger';

export const useMessageMonitoring = (businessComponentId?: string) => {
 const [messages, setMessages] = useState<Message[]>([]);
 const [stats, setStats] = useState<MessageStats | null>(null);
 const [loading, setLoading] = useState(false);
 const [connected, setConnected] = useState(false);
 const [currentFilters, setCurrentFilters] = useState<MessageFilters | undefined>();
 const { toast } = useToast();

 // Load initial data
 const loadMessages = useCallback(async (filters?: MessageFilters) => {
 setLoading(true);
 setCurrentFilters(filters); // Store current filters
 try {
 const response = businessComponentId
 ? await messageService.getBusinessComponentMessages(businessComponentId, filters)
 : await messageService.getMessages(filters);

 if (response.success && response.data) {
 logger.info(LogCategory.BUSINESS_LOGIC, `[useMessageMonitoring] Messages data:`, { data: response.data.messages });
 // Check first message for debugging
 if (response.data.messages && response.data.messages.length > 0) {
 logger.info(LogCategory.BUSINESS_LOGIC, `[useMessageMonitoring] First message:`, { data: response.data.messages[0] });
 logger.info(LogCategory.BUSINESS_LOGIC, `[useMessageMonitoring] First message timestamp:`, { data: response.data.messages[0].timestamp });
 logger.info(LogCategory.BUSINESS_LOGIC, `[useMessageMonitoring] Timestamp type:`, { data: typeof response.data.messages[0].timestamp });
 }
 setMessages(response.data.messages || []);
 } else {
 // Ensure messages is always an array even on error
 setMessages([]);
 }
 } catch (error) {
 toast({
 title: "Error",
 description: "Failed to load messages",
 variant: "destructive",
 });
 } finally {
 setLoading(false);
 }
 }, [businessComponentId, toast]);

 const loadStats = useCallback(async (filters?: Omit<MessageFilters, 'limit' | 'offset'>) => {
 try {
 // Add businessComponentId to filters if present
 const statsFilters = businessComponentId
 ? { ...filters, source: businessComponentId }
 : filters;

 const response = await messageService.getMessageStats(statsFilters);
 if (response.success && response.data) {
 setStats(response.data);
 }
 } catch (error) {
 logger.error(LogCategory.BUSINESS_LOGIC, 'Failed to load message stats', { error: error });
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
 
 messageService.connectWebSocket(businessComponentId, connectionParams);
 setConnected(true);

 // Subscribe to message updates
 const unsubscribeMessages = messageService.onMessageUpdate((newMessage) => {
 setMessages(prev => {
 const existingIndex = prev.findIndex(m => m.id === newMessage.id);
 if (existingIndex >= 0) {
 // Update existing message
 const updated = [...prev];
 updated[existingIndex] = newMessage;
 return updated;
 } else {
 // Add new message at the beginning
 return [newMessage, ...prev];
 }
 });

 // Show toast for failed messages
 if (newMessage.status === 'failed') {
 toast({
 title: "Message Failed",
 description: `Message ${newMessage.id} failed processing`,
 variant: "destructive",
 });
 }
 });

 // Subscribe to stats updates with filter support
 const unsubscribeStats = messageService.onStatsUpdate((newStats) => {
 // Only update stats if they match current filters
 // The backend should already be sending filtered stats based on connectionParams
 setStats(newStats);
 });

 // Don't load initial data - let the parent component control this with filters

 // Cleanup on unmount
 return () => {
 unsubscribeMessages();
 unsubscribeStats();
 messageService.disconnectWebSocket();
 setConnected(false);
 };
 }, [businessComponentId, loadMessages, loadStats, toast, currentFilters]);

 const reprocessMessage = useCallback(async (messageId: string) => {
 try {
 const response = await messageService.reprocessMessage(messageId);
 if (response.success) {
 toast({
 title: "Success",
 description: "Message queued for reprocessing",
 });
 // The WebSocket will handle the real-time update
 } else {
 throw new Error(response.error || 'Failed to reprocess message');
 }
 } catch (error) {
 toast({
 title: "Error",
 description: "Failed to reprocess message",
 variant: "destructive",
 });
 }
 }, [toast]);

 const refreshData = useCallback((filters?: MessageFilters) => {
 loadMessages(filters);
 loadStats(filters);
 }, [loadMessages, loadStats]);

 const subscribeToMessageType = useCallback((messageType: string) => {
 messageService.sendCommand('subscribe', { messageType });
 }, []);

 const unsubscribeFromMessageType = useCallback((messageType: string) => {
 messageService.sendCommand('unsubscribe', { messageType });
 }, []);

 return {
 messages,
 stats,
 loading,
 connected,
 loadMessages,
 loadStats,
 reprocessMessage,
 refreshData,
 subscribeToMessageType,
 unsubscribeFromMessageType,
 };
};
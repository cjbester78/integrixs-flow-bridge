import { useState, useEffect, useCallback } from 'react';
import { webserviceService, WebserviceFile } from '@/services/webserviceService';
import { FieldNode } from '@/components/fieldMapping/types';
import { logger, LogCategory } from '@/lib/logger';

export const useWebservices = (businessComponentId?: string) => {
 const [webservices, setWebservices] = useState<WebserviceFile[]>([]);
 const [loading, setLoading] = useState(true);
 const [error, setError] = useState<string | null>(null);

 const loadWebservices = useCallback(async () => {
 setLoading(true);
 setError(null);

 try {
 const response = await webserviceService.getWebserviceFiles(businessComponentId);
 if (response.success && response.data) {
 setWebservices(response.data);
 } else {
 setError(response.error || 'Failed to load webservices');
 setWebservices([]);
 }
 } catch (err) {
 setError(err instanceof Error ? err.message : 'An unexpected error occurred');
 setWebservices([]);
 } finally {
 setLoading(false);
 }
 }, [businessComponentId]);

 useEffect(() => {
 loadWebservices();
 }, [loadWebservices]);

 const getWebserviceStructure = async (filename: string): Promise<FieldNode[] | null> => {
 try {
 const response = await webserviceService.getWebserviceStructure(filename);
 if (response.success && response.data) {
 return response.data;
 }
 return null;
 } catch (err) {
 logger.error(LogCategory.BUSINESS_LOGIC, 'Failed to load webservice structure', { error: err });
 return null;
 }
 };

 const uploadWebservice = async (file: File, businessComponentId: string) => {
 try {
 const response = await webserviceService.uploadWebservice(file, businessComponentId);
 if (response.success) {
 await loadWebservices(); // Refresh the list
 }
 return response;
 } catch (err) {
 return {
 success: false,
 error: err instanceof Error ? err.message : 'Failed to upload webservice'
 };
 }
 };

 const deleteWebservice = async (filename: string) => {
 try {
 const response = await webserviceService.deleteWebservice(filename);
 if (response.success) {
 await loadWebservices(); // Refresh the list
 }
 return response;
 } catch (err) {
 return {
 success: false,
 error: err instanceof Error ? err.message : 'Failed to delete webservice'
 };
 }
 };

 return {
 webservices,
 loading,
 error,
 refreshWebservices: loadWebservices,
 getWebserviceStructure,
 uploadWebservice,
 deleteWebservice,
 };
};
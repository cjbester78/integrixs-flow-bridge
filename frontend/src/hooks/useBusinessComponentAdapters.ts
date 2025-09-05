import { useState, useEffect } from 'react';
import { BusinessComponent } from '@/types/businessComponent';
import { businessComponentService } from '@/services/businessComponentService';
import { adapterMonitoringService } from '@/services/adapterMonitoringService';
import { structureService } from '@/services/structureService';
import { logger, LogCategory } from '@/lib/logger';

export const useBusinessComponentAdapters = () => {
 const [businessComponents, setBusinessComponents] = useState<BusinessComponent[]>([]);
 const [loading, setLoading] = useState(true);

 useEffect(() => {
 loadData();
 }, []);

 const loadData = async () => {
    try {
 logger.info(LogCategory.BUSINESS_LOGIC, 'Loading businessComponent data...');
 const businessComponentsResponse = await businessComponentService.getAllBusinessComponents();

 if (businessComponentsResponse.success && businessComponentsResponse.data) {
 logger.info(LogCategory.BUSINESS_LOGIC, 'API businessComponents loaded', { data: businessComponentsResponse.data });
 const businessComponents = Array.isArray(businessComponentsResponse.data)
 ? businessComponentsResponse.data
 : [];

 // Set business components from API, even if empty
 logger.info(LogCategory.BUSINESS_LOGIC, `API returned ${businessComponents.length} business components`);
 setBusinessComponents(businessComponents);
 } else {
 // If API fails, show empty list
 logger.info(LogCategory.BUSINESS_LOGIC, 'API failed, showing empty business components list');
 setBusinessComponents([]);
 }
} catch (error) {
 logger.error(LogCategory.BUSINESS_LOGIC, 'Error loading business components', { error: error });
 setBusinessComponents([]);
 } finally {
 setLoading(false);
 }
 };

 const getAdaptersForBusinessComponent = async (businessComponentId: string) => {
 try {
const response = await adapterMonitoringService.getAdapters(businessComponentId);
 if (response.success && response.data && Array.isArray(response.data)) {
 // response.data is already an array of AdapterMonitoring objects
 const adapterIds = response.data
 .map(adapter => adapter?.id)
 .filter(id => id !== null && id !== undefined);
 return [...new Set(adapterIds)]; // Remove duplicates
 }
 } catch (error) {
 logger.error(LogCategory.BUSINESS_LOGIC, 'Error getting adapters for business component', { error: error });
 }
 return [];
 };

 const getStructuresForBusinessComponent = async (businessComponentId: string) => {
 try {
 const response = await structureService.getStructures();
 if (response.success && response.data) {
 // Handle both { structures: [...] } and direct array formats
 const structures = response.data.structures || response.data;
 // Ensure structures is an array
 if (!Array.isArray(structures)) {
 logger.warn(LogCategory.BUSINESS_LOGIC, 'Structures is not an array', { data: structures });
 return [];
 }

 // Filter and map structures safely
 return structures
 .filter(s => {
 // Ensure each structure is a valid object with required properties
 if (!s || typeof s !== 'object') {
 logger.warn(LogCategory.BUSINESS_LOGIC, 'Invalid structure object', { data: s });
 return false;
 }
 return s.businessComponentId === businessComponentId;
 })
 .map(structure => structure.id)
 .filter(id => id !== null && id !== undefined);
 }
 } catch (error) {
 logger.error(LogCategory.BUSINESS_LOGIC, 'Error getting structures for business component', { error: error });
 return [];
 }
 };

 return {
 businessComponents,
 loading,
 getAdaptersForBusinessComponent,
 getStructuresForBusinessComponent,
 refreshData: loadData,
 };
};
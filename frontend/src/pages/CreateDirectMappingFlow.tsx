import { useState, useEffect, useCallback } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { useNavigationHistory } from '@/hooks/useNavigationHistory';
import { usePageReset } from '@/hooks/usePageReset';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { ArrowLeft, Settings as Loader2 } from 'lucide-react';
import { FlowActionsCard } from '@/components/createFlow/FlowActionsCard';
import { FlowSummaryCard } from '@/components/createFlow/FlowSummaryCard';
import { QuickTipsCard } from '@/components/createFlow/QuickTipsCard';
import { FieldMappingScreen } from '@/components/FieldMappingScreen';
import { Database, Server, Globe, Mail, FileText, Code } from 'lucide-react';
import { businessComponentService } from '@/services/businessComponentService';
import { api } from '@/services/api';
import { useToast } from '@/hooks/use-toast';
import { convertStructureToXml } from '@/utils/xmlStructureConverter';
import { parseWsdlStructure } from '@/utils/structureParsers';
import { logger, LogCategory } from '@/lib/logger';

// Adapter icon mapping
const getAdapterIcon = (type: string) => {
 const typeUpper = type.toUpperCase();
 if (typeUpper.includes('JDBC')) return Database;
 if (typeUpper.includes('HTTP') || typeUpper.includes('REST')) return Globe;
 if (typeUpper.includes('SOAP')) return Code;
 if (typeUpper.includes('MAIL')) return Mail;
 if (typeUpper.includes('FILE') || typeUpper.includes('FTP')) return FileText;
 return Server;
};

export function CreateDirectMappingFlow() {
 const location = useLocation();
 const { flowId } = useParams<{ flowId?: string }>();
 const { navigateBack, isDirectNavigation } = useNavigationHistory();
 const { toast } = useToast();
 const { user } = useAuth();
 const [showFieldMapping, setShowFieldMapping] = useState(false);
 const editingFlow = location.state?.flow || null;
 // Flow state
 const [flowName, setFlowName] = useState('');
 const [description, setDescription] = useState('');
 const [sourceBusinessComponent, setSourceBusinessComponent] = useState('');
 const [targetBusinessComponent, setTargetBusinessComponent] = useState('');
 const [inboundAdapter, setInboundAdapter] = useState('');
 const [outboundAdapter, setOutboundAdapter] = useState('');
 const [sourceStructure, setSourceStructure] = useState('');
 const [targetStructure, setTargetStructure] = useState('');
 const [mappingRequired, setMappingRequired] = useState(true);
 const [skipXmlConversion, setSkipXmlConversion] = useState(false);

 // Log initial states
 logger.info(LogCategory.SYSTEM, '=== INITIAL TOGGLE STATES ===');
 logger.info(LogCategory.SYSTEM, 'Initial mappingRequired', { data: true })
 logger.info(LogCategory.SYSTEM, 'Initial skipXmlConversion', { data: false })
 const [fieldMappings, setFieldMappings] = useState<any[]>([]);
 const [isAsynchronous, setIsAsynchronous] = useState(true); // Default to async
 const [additionalMappings, setAdditionalMappings] = useState<any[]>([]);
 const [wsdlOperationInfo, setWsdlOperationInfo] = useState<any>(null);
 const [requestMappingName, setRequestMappingName] = useState('');

 // Data state
 const [businessComponents, setBusinessComponents] = useState<any[]>([]);
 const [adapters, setAdapters] = useState<any[]>([]);
 const [dataStructures, setDataStructures] = useState<any[]>([]);
 const [loading, setLoading] = useState(true);
 const [saving, setSaving] = useState(false);
 const [convertingStructures, setConvertingStructures] = useState(false);
 const [sourceXml, setSourceXml] = useState<string>('');
 const [targetXml, setTargetXml] = useState<string>('');
 const [currentMappingType, setCurrentMappingType] = useState<string>('request');


 // Reset form when navigating directly from menu
 const resetForm = () => {
 setFlowName('');
 setDescription('');
 setSourceBusinessComponent('');
 setTargetBusinessComponent('');
 setInboundAdapter('');
 setOutboundAdapter('');
 setSourceStructure('');
 setTargetStructure('');
 setMappingRequired(true);
 setFieldMappings([]);
 setIsAsynchronous(true);
 setAdditionalMappings([]);
 setShowFieldMapping(false);
 setSourceXml('');
 setTargetXml('');
 };

 usePageReset(resetForm);

 // Debug business components state
 useEffect(() => {
 logger.info(LogCategory.SYSTEM, 'Business components state updated', { data: businessComponents })
 }, [businessComponents]);

 // No longer needed - handled by the onChange handlers

 // Load data on component mount
 useEffect(() => {
 loadComponentData();
 }, [loadComponentData]);

 // Load existing flow data if editing
 useEffect(() => {
 if (editingFlow || flowId) {
 loadExistingFlow();
 }
 }, [editingFlow, flowId, loadExistingFlow]);

 // Analyze structure when source or target changes
 useEffect(() => {
 const analyzeStructureForWsdl = async (structureId: string) => {
 if (!structureId) return;

 // Don't analyze WSDL when in edit mode - it will overwrite existing mappings
 if (location.pathname.includes('/edit')) return;

 try {
 // Get the structure details
 const structure = dataStructures.find(s => s.id === structureId);
 if (!structure || structure.type !== 'wsdl') return;

 // Get the actual structure content
 const response = await api.get(`/structures/${structureId}`);
 logger.info(LogCategory.SYSTEM, 'Structure response', { data: response.data }) // Debug log

 if (response.data) {
 // Try to find WSDL content in various places
 let wsdlContent = null;
 // Check if structure is a string (raw WSDL })
 if (typeof response.data.structure === 'string' && response.data.structure.includes('wsdl:')) {
 wsdlContent = response.data.structure;
 }
 // Check namespace for WSDL content
 else if (response.data.namespace) {
 // Try schemaLocation
 if (response.data.namespace.schemaLocation && response.data.namespace.schemaLocation.includes('wsdl:')) {
 wsdlContent = response.data.namespace.schemaLocation;
 }
 // Try raw namespace as string
 else if (typeof response.data.namespace === 'string' && response.data.namespace.includes('wsdl:')) {
 wsdlContent = response.data.namespace;
 }
 }
 // Check tags or other fields that might contain WSDL
 else if (response.data.tags && Array.isArray(response.data.tags)) {
 // Sometimes WSDL might be stored in tags
 const wsdlTag = response.data.tags.find((tag: string) => tag.includes('wsdl:'));
 if (wsdlTag) {
 wsdlContent = wsdlTag;
 }
 }

 // If we found WSDL content but it's not being detected,
 // let's check the structure itself for message patterns
 // First check if structure has metadata with operation info (from saved WSDL structures)
 if (response.data.metadata?.operationInfo) {
 const operationInfo = response.data.metadata.operationInfo;
 setWsdlOperationInfo(operationInfo);

 // Auto-switch to sync mode if WSDL has request-response pattern
 if (operationInfo.isSynchronous) {
 setIsAsynchronous(false);

 toast({
 title: "Synchronous Service Detected",
 description: `This WSDL structure defines a synchronous service with ${operationInfo.messageTypes.join(', ')} messages. The flow has been switched to synchronous mode.`,
 })

 // Auto-create mapping sets
 const newMappings = [];
 if (operationInfo.hasOutput) {
 newMappings.push({
 id: `response_mapping_${Date.now()}`,
 name: flowName ? `${flowName} Response` : 'Response Mapping',
 fieldMappings: [],
 messageType: 'response'
 })
 }

 if (operationInfo.hasFault) {
 newMappings.push({
 id: `fault_mapping_${Date.now() + 1}`,
 name: flowName ? `${flowName} Fault` : 'Fault Mapping',
 fieldMappings: [],
 messageType: 'fault'
 })
 }

 if (newMappings.length > 0 && additionalMappings.length === 0) {
 // Only set new mappings if we don't already have mappings loaded
 setAdditionalMappings(newMappings);
 toast({
 title: "Mapping Sets Created",
 description: `Created mapping sets for ${newMappings.map(m => m.name).join(' and ')}. Please configure each mapping.`,
 })
 }

 return; // Exit early since we handled it
 }
 } else if (!wsdlContent && response.data.structure && typeof response.data.structure === 'object') {
 // Fall back to checking structure patterns if no metadata
 const structureKeys = Object.keys(response.data.structure);
 const hasRequest = structureKeys.some(key =>
 key.toLowerCase().includes('req') ||
 key.toLowerCase().includes('request') ||
 key.toLowerCase().includes('_mt') // SAP message type pattern
 );
 const hasResponse = structureKeys.some(key =>
 key.toLowerCase().includes('resp') ||
 key.toLowerCase().includes('response')
 );
 const hasFault = structureKeys.some(key =>
 key.toLowerCase().includes('fault') ||
 key.toLowerCase().includes('error')
 );

 if (hasRequest && hasResponse) {
 // It's a sync pattern even without raw WSDL
 setWsdlOperationInfo({
 hasInput: hasRequest,
 hasOutput: hasResponse,
 hasFault: hasFault,
 isSynchronous: true,
 messageTypes: ['input', 'output', ...(hasFault ? ['fault'] : [])]
 })

 setIsAsynchronous(false);

 toast({
 title: "Synchronous Service Detected",
 description: `This structure appears to define a synchronous service with request and response messages. The flow has been switched to synchronous mode.`,
 })

 // Auto-create mapping sets
 const newMappings = [];
 if (hasResponse) {
 newMappings.push({
 id: `response_mapping_${Date.now()}`,
 name: flowName ? `${flowName} Response` : 'Response Mapping',
 fieldMappings: [],
 messageType: 'response'
 })
 }

 if (hasFault) {
 newMappings.push({
 id: `fault_mapping_${Date.now() + 1}`,
 name: flowName ? `${flowName} Fault` : 'Fault Mapping',
 fieldMappings: [],
 messageType: 'fault'
 })
 }

 if (newMappings.length > 0 && additionalMappings.length === 0) {
 // Only set new mappings if we don't already have mappings loaded
 setAdditionalMappings(newMappings);
 toast({
 title: "Mapping Sets Created",
 description: `Created mapping sets for ${newMappings.map(m => m.name).join(' and ')}. Please configure each mapping.`,
 })
 }

 return; // Exit early since we handled it
 }
 }

 if (wsdlContent) {
 const wsdlResult = parseWsdlStructure(wsdlContent);
 if (wsdlResult && wsdlResult.operationInfo) {
 setWsdlOperationInfo(wsdlResult.operationInfo);

 // Auto-switch to sync mode if WSDL has request-response pattern
 if (wsdlResult.operationInfo.isSynchronous) {
 setIsAsynchronous(false);

 // Show a message to the user
 toast({
 title: "Synchronous Service Detected",
 description: `This WSDL defines a synchronous service with ${wsdlResult.operationInfo.messageTypes.join(', ')} messages. The flow has been switched to synchronous mode.`,
 })

 // Auto-create mapping sets for each message type
 const newMappings = [];
 // Primary mapping is always for the request/input
 // Additional mappings for output and fault
 if (wsdlResult.operationInfo.hasOutput) {
 newMappings.push({
 id: `response_mapping_${Date.now()}`,
 name: flowName ? `${flowName} Response` : 'Response Mapping',
 fieldMappings: [],
 messageType: 'response'
 })
 }

 if (wsdlResult.operationInfo.hasFault) {
 newMappings.push({
 id: `fault_mapping_${Date.now()}`,
 name: flowName ? `${flowName} Fault` : 'Fault Mapping',
 fieldMappings: [],
 messageType: 'fault'
 })
 }

 if (newMappings.length > 0 && additionalMappings.length === 0) {
 // Only set new mappings if we don't already have mappings loaded
 setAdditionalMappings(newMappings);
 toast({
 title: "Mapping Sets Created",
 description: `Created mapping sets for ${newMappings.map(m => m.name).join(' and ')}. Please configure each mapping.`,
 })
 }
 }
      }
    }
    }
  } catch (error) {
    logger.error(LogCategory.ERROR, 'Error analyzing WSDL structure', { error: error })
  }
  };

  // Analyze both source and target structures
  if (sourceStructure) {
    analyzeStructureForWsdl(sourceStructure);
  }
  if (targetStructure) {
    analyzeStructureForWsdl(targetStructure);
  }
}, [sourceStructure, targetStructure, dataStructures, toast, additionalMappings.length, flowName, location.pathname]);

 // Debug state changes
 useEffect(() => {
 logger.info(LogCategory.SYSTEM, 'State updated - additionalMappings', { data: additionalMappings })
 additionalMappings.forEach((mapping, index) => {
 logger.info(LogCategory.SYSTEM, `Mapping ${index}: messageType=${mapping.messageType}, fieldMappings.length=${mapping.fieldMappings?.length || 0}`);
 })
 logger.info(LogCategory.SYSTEM, 'State updated - isAsynchronous', { data: isAsynchronous })
 logger.info(LogCategory.SYSTEM, 'State updated - mappingRequired', { data: mappingRequired })
 }, [additionalMappings, isAsynchronous, mappingRequired]);

 const loadComponentData = useCallback(async () => {
    try {
 setLoading(true);

 // Load business components
 const businessResult = await businessComponentService.getAllBusinessComponents();
 logger.info(LogCategory.SYSTEM, 'Business components result', { data: businessResult })
 if (businessResult.success && businessResult.data) {
 setBusinessComponents(businessResult.data);
 logger.info(LogCategory.SYSTEM, 'Set business components', { data: businessResult.data })
 } else {
 logger.error(LogCategory.ERROR, 'Failed to load business components', { error: businessResult.error })
 logger.error(LogCategory.ERROR, 'Business result full object', { error: businessResult })
 }
 
 // Load adapters
 try {
 const adaptersResponse = await api.get('/adapters');
 if (adaptersResponse.data) {
 setAdapters(adaptersResponse.data);
 }
 } catch (error) {
 logger.warn(LogCategory.SYSTEM, 'Could not load adapters', { data: error })
 setAdapters([]);
 }

 // Load data structures
 try {
 const structuresResponse = await api.get('/structures');
 if (structuresResponse.data) {
 // Ensure we have an array
 const structures = Array.isArray(structuresResponse.data)
 ? structuresResponse.data
 : (structuresResponse.data.structures || []);
 setDataStructures(structures);
 }
 } catch (error) {
 logger.warn(LogCategory.SYSTEM, 'Could not load data structures', { data: error })
 setDataStructures([]);
 }
        } catch (error) {
            logger.error(LogCategory.ERROR, 'Error loading component data', { error: error })
 toast({
 title: "Error",
 description: "Failed to load component data. Some features may not work properly.",
 variant: "destructive",
 })
        } finally {
 setLoading(false);
 }
 }, [toast]);

 const loadExistingFlow = useCallback(async () => {
    try {
 let flowData = editingFlow;
 // If no flow data passed through state, fetch it
 if (!flowData && flowId) {
 const response = await api.get(`/flows/${flowId}`);
 if (response.data) {
 flowData = response.data;
 }
 }

 if (flowData) {
 // Set basic flow information
 setFlowName(flowData.name || '');
 setDescription(flowData.description || '');

 // Set business component if available
 if (flowData.businessComponentId) {
 setSourceBusinessComponent(flowData.businessComponentId);
 setTargetBusinessComponent(flowData.businessComponentId);
 }

 // Try to parse configuration for specific business component IDs
 try {
 if (flowData.configuration) {
 const config = JSON.parse(flowData.configuration);
 logger.info(LogCategory.SYSTEM, 'Flow configuration', { data: config });
 if (config.sourceBusinessComponentId) {
 logger.info(LogCategory.SYSTEM, 'Setting source business component from config', { data: config.sourceBusinessComponentId });
 setSourceBusinessComponent(config.sourceBusinessComponentId);
 }
 if (config.targetBusinessComponentId) {
 logger.info(LogCategory.SYSTEM, 'Setting target business component from config', { data: config.targetBusinessComponentId });
 setTargetBusinessComponent(config.targetBusinessComponentId);
 }
 }
 } catch (e) {
 logger.error(LogCategory.ERROR, 'Error parsing flow configuration', { error: e });
 }
 // Set adapters
 if (flowData.inboundAdapterId) {
 setInboundAdapter(flowData.inboundAdapterId);
 }
 if (flowData.outboundAdapterId) {
 setOutboundAdapter(flowData.outboundAdapterId);
 }

 // Set structures
 if (flowData.sourceStructureId) {
 setSourceStructure(flowData.sourceStructureId);
 }
 if (flowData.targetStructureId) {
 setTargetStructure(flowData.targetStructureId);
 }

 // Set processing mode based on mappingMode
 if (flowData.mappingMode === 'PASS_THROUGH') {
 setIsAsynchronous(true);
 // For pass-through mode: no mapping required, skip XML conversion
 setMappingRequired(false);
 setSkipXmlConversion(true);
 logger.info(LogCategory.SYSTEM, 'Loading PASS_THROUGH flow: mappingRequired=false, skipXmlConversion=true');
 } else if (flowData.mappingMode === 'WITH_MAPPING') {
 setIsAsynchronous(false);
 // For mapping mode: mapping required, don't skip XML conversion
 setMappingRequired(true);
 setSkipXmlConversion(false);
 logger.info(LogCategory.SYSTEM, 'Loading WITH_MAPPING flow: mappingRequired=true, skipXmlConversion=false');
 } else {
 // Default case
 setIsAsynchronous(false);
 setMappingRequired(true);
 setSkipXmlConversion(false);
 }

 // Load transformations and field mappings
 try {
 const transformationsResponse = await api.get(`/flows/${flowData.id}/transformations`);
 if (transformationsResponse.data && Array.isArray(transformationsResponse.data)) {
 // Sort transformations by execution order
 const sortedTransformations = transformationsResponse.data.sort((a, b) => a.executionOrder - b.executionOrder);
 // Collect all additional mappings first
 const allAdditionalMappings: any[] = [];

 // Determine mapping types based on order and sync/async mode
 for (let i = 0; i < sortedTransformations.length; i++) {
 const transformation = sortedTransformations[i];
 if (transformation.type === 'FIELD_MAPPING') {
 const mappingsResponse = await api.get(`/transformations/${transformation.id}/mappings`);
 if (mappingsResponse.data && Array.isArray(mappingsResponse.data)) {
 // Check configuration first for mapping type
 let messageType;
 let config: any = {};
 try {
 config = JSON.parse(transformation.configuration || '{}');
 logger.info(LogCategory.SYSTEM, 'Transformation config', { data: config });
 // Use mappingType from config if available
 if (config.mappingType) {
 messageType = config.mappingType;
 } else {
 // Fallback to position-based logic
 if (i === 0) {
 messageType = 'request';
 } else if (flowData.mappingMode === 'PASS_THROUGH') {
 messageType = 'fault';
 } else {
 messageType = i === 1 ? 'response' : 'fault';
 }
 }
 } catch (e) {
                logger.error(LogCategory.ERROR, 'Error parsing transformation config', { error: e });
 // Default fallback
 messageType = i === 0 ? 'request' : i === 1 ? 'response' : 'fault';
 }

 // Use the transformation name directly
 const mappingName = transformation.name || '';
 logger.info(LogCategory.SYSTEM, `Transformation ${i}: name="${transformation.name}", executionOrder=${transformation.executionOrder}, messageType: ${messageType}`);
 // Parse sourceFields from JSON string to array for each mapping
 const parsedMappings = mappingsResponse.data.map((mapping: any) => {
 try {
 const parsedMapping: any = {
 ...mapping,
 sourceFields: typeof mapping.sourceFields === 'string'
 ? JSON.parse(mapping.sourceFields)
 : mapping.sourceFields || []
 };

 // Parse visualFlowData if present
 if (mapping.visualFlowData) {
 try {
 parsedMapping.visualFlowData = typeof mapping.visualFlowData === 'string'
 ? JSON.parse(mapping.visualFlowData)
 : mapping.visualFlowData;
 } catch (e) {
 logger.error(LogCategory.ERROR, 'Error parsing visualFlowData', { error: e });
 }
 }

 // Parse functionNode if present
 if (mapping.functionNode) {
 try {
 parsedMapping.functionNode = typeof mapping.functionNode === 'string'
 ? JSON.parse(mapping.functionNode)
 : mapping.functionNode;
 } catch (e) {
 logger.error(LogCategory.ERROR, 'Error parsing functionNode', { error: e });
 }
 }

 return parsedMapping;
 } catch (e) {
 logger.error(LogCategory.ERROR, 'Error parsing sourceFields for mapping', { data: mapping, error: e });
 return {
 ...mapping,
 sourceFields: []
 };
 }
 });

 if (messageType === 'request') {
 setFieldMappings(parsedMappings);
 setRequestMappingName(mappingName || 'Request Mapping');
 } else {
 // Add to additional mappings collection
 allAdditionalMappings.push({
 id: transformation.id,
 name: mappingName || (messageType === 'response' ? 'Response Mapping' : 'Fault Mapping'),
 fieldMappings: parsedMappings,
 messageType: messageType
 });
 }
 }
 }
 }

 // Update additional mappings state once with all collected mappings
 if (allAdditionalMappings.length > 0) {
 setAdditionalMappings(allAdditionalMappings);
 }
 }
			} catch (error) {
				logger.error(LogCategory.ERROR, 'Error loading transformations and mappings', { error: error });
			}
			// Debug logging is now handled by useEffect
		}
	} catch (error) {
		logger.error(LogCategory.ERROR, 'Error loading flow data', { error: error });
 toast({
 title: "Error",
 description: "Failed to load flow data for editing.",
 variant: "destructive",
 });
 }
 }, [editingFlow, flowId, setInboundAdapter, setOutboundAdapter, setAdditionalMappings, toast]);

 // Handle source adapter selection
 const handleInboundAdapterChange = (adapterId: string) => {
 setInboundAdapter(adapterId);

 // Find the selected adapter
 const adapter = adapters.find(a => a.id === adapterId);
 if (!adapter) return;

 // Check if adapter has transformation configuration
 const config = adapter.configuration;
 if (config && typeof config === 'object') {
 const transformationConfig = config.transformationConfig;
 // If passthrough mode, disable mapping
 if (transformationConfig?.mode === 'passthrough') {
 setMappingRequired(false);
 setSourceStructure('');
 toast({
 title: "Passthrough Mode",
 description: "This adapter is configured for passthrough. Mapping has been disabled.",
 });
 } else if (transformationConfig?.dataStructureId) {
 // Auto-populate structure if available
 setSourceStructure(transformationConfig.dataStructureId);
 setMappingRequired(true);
 toast({
 title: "Structure Auto-Selected",
 description: "Data structure has been automatically selected based on adapter configuration.",
 });
 }
 }

 // Auto-detect flow mode based on adapter type
 autoDetectFlowMode(adapter, outboundAdapter);
 };

 // Handle target adapter selection
 const handleOutboundAdapterChange = (adapterId: string) => {
 setOutboundAdapter(adapterId);

 // Find the selected adapter
 const adapter = adapters.find(a => a.id === adapterId);
 if (!adapter) return;

 // Check if adapter has transformation configuration
 const config = adapter.configuration;
 if (config && typeof config === 'object') {
 const transformationConfig = config.transformationConfig;
 // If passthrough mode, disable mapping
 if (transformationConfig?.mode === 'passthrough') {
 setMappingRequired(false);
 setTargetStructure('');
 toast({
 title: "Passthrough Mode",
 description: "This adapter is configured for passthrough. Mapping has been disabled.",
 });
 } else if (transformationConfig?.dataStructureId) {
 // Auto-populate structure if available
 setTargetStructure(transformationConfig.dataStructureId);
 setMappingRequired(true);
 toast({
 title: "Structure Auto-Selected",
 description: "Data structure has been automatically selected based on adapter configuration.",
 });
 }
 }

 // Auto-detect flow mode based on adapter type
 autoDetectFlowMode(inboundAdapter, adapter);
 };

 // Auto-detect flow mode based on adapter configurations
 const autoDetectFlowMode = (inboundAdapterId: string | any, outboundAdapterId: string | any) => {
 const source = typeof inboundAdapterId === 'string'
 ? adapters.find(a => a.id === inboundAdapterId)
 : inboundAdapterId;
 const target = typeof outboundAdapterId === 'string'
 ? adapters.find(a => a.id === outboundAdapterId)
 : outboundAdapterId;

 if (!source || !target) return;

 // Check adapter types and modes
 const sourceType = source.type?.toUpperCase();
 const targetType = target.type?.toUpperCase();
 // HTTP/REST adapters often support synchronous mode
 if (['HTTP', 'HTTPS', 'REST'].includes(sourceType) || ['HTTP', 'HTTPS', 'REST'].includes(targetType)) {
 // Check if either adapter expects response
 const sourceConfig = source.configuration;
 const targetConfig = target.configuration;
 if (sourceConfig?.expectsResponse || targetConfig?.expectsResponse) {
 setIsAsynchronous(false);
 toast({
 title: "Synchronous Mode Detected",
 description: "Flow mode set to synchronous based on adapter configuration.",
 });
 }
 }
 };

 const handleClose = () => {
 navigateBack('/dashboard');
 };

 const handleSave = async (mappings?: any[], mappingName?: string) => {
 if (!flowName.trim()) {
 toast({
 title: "Validation Error",
 description: "Flow name is required.",
 variant: "destructive",
 })
 return;
 }

 if (!inboundAdapter || !outboundAdapter) {
 toast({
 title: "Validation Error",
 description: "Both source and target adapters are required.",
 variant: "destructive",
 })
 return;
 }

 // Validate mapping names
 if (mappingRequired && fieldMappings.length > 0 && !requestMappingName.trim()) {
 toast({
 title: "Validation Error",
 description: "Please provide a name for the request mapping.",
 variant: "destructive",
 })
 return;
 }

 // Validate additional mapping names
 if (!isAsynchronous && additionalMappings.length > 0) {
 const unmappedMappings = additionalMappings.filter(am => !am.name?.trim());
 if (unmappedMappings.length > 0) {
 toast({
 title: "Validation Error",
 description: "Please provide names for all mappings.",
 variant: "destructive",
 })
 return;
 }
 }

 try {
 setSaving(true);

 // Transform field mappings to match backend DTO structure
 const transformedMappings = mappingRequired && (mappings || fieldMappings)
 ? (mappings || fieldMappings).map((mapping: any, index: number) => ({
 sourceFields: JSON.stringify(mapping.sourceFields || []), // Convert array to JSON string
 targetField: mapping.targetField || mapping.targetPath || '',
 javaFunction: mapping.javaFunction || mapping.functionNode?.javaFunction || null,
 mappingRule: mapping.mappingRule || null,
 functionName: mapping.functionNode?.functionName || null,
 isActive: true,
 isArrayMapping: mapping.functionNode?.functionName === 'nodeMapping' || false,
 arrayContextPath: mapping.functionNode?.parameters?.arrayContextPath || null,
 sourceXPath: mapping.functionNode?.parameters?.sourceXPath || null,
 targetXPath: mapping.functionNode?.parameters?.targetXPath || null,
 // Include visual flow data if present
 visualFlowData: mapping.visualFlowData ? JSON.stringify(mapping.visualFlowData) : null,
 functionNode: mapping.functionNode ? JSON.stringify(mapping.functionNode) : null,
 mappingOrder: index
 }))
 : [];

 // Prepare the direct mapping flow request
 const flowRequest = {
 flowName: flowName.trim(),
 description: description.trim(),
 sourceBusinessComponentId: sourceBusinessComponent || null,
 targetBusinessComponentId: targetBusinessComponent || null,
 inboundAdapterId: inboundAdapter,
 outboundAdapterId: outboundAdapter,
 sourceStructureId: sourceStructure || null,
 targetStructureId: targetStructure || null,
 createdBy: user?.id || user?.username || 'unknown', // Use user ID from auth context
 skipXmlConversion: skipXmlConversion,
 mappingMode: mappingRequired ? 'WITH_MAPPING' : 'PASS_THROUGH', // Add mapping mode
 requestMappingName: requestMappingName || (flowName ? `${flowName} Request` : 'Request Mapping'),
 fieldMappings: transformedMappings,
 additionalMappings: !isAsynchronous && additionalMappings.length > 0
 ? additionalMappings.map(am => ({
 name: am.name,
 fieldMappings: am.fieldMappings.map((mapping: any, index: number) => ({
 sourceFields: JSON.stringify(mapping.sourceFields || []),
 targetField: mapping.targetField || mapping.targetPath || '',
 javaFunction: mapping.javaFunction || mapping.functionNode?.javaFunction || null,
 mappingRule: mapping.mappingRule || null,
 functionName: mapping.functionNode?.functionName || null,
 isActive: true,
 isArrayMapping: mapping.functionNode?.functionName === 'nodeMapping' || false,
 arrayContextPath: mapping.functionNode?.parameters?.arrayContextPath || null,
 sourceXPath: mapping.functionNode?.parameters?.sourceXPath || null,
 targetXPath: mapping.functionNode?.parameters?.targetXPath || null,
 // Include visual flow data if present
 visualFlowData: mapping.visualFlowData ? JSON.stringify(mapping.visualFlowData) : null,
 functionNode: mapping.functionNode ? JSON.stringify(mapping.functionNode) : null,
 mappingOrder: index
 }))
 }))
 : []
 };

 // Log the request payload for debugging
 logger.info(LogCategory.SYSTEM, '=== FLOW SAVE DEBUG === ');
 logger.info(LogCategory.SYSTEM, 'mappingRequired state:', { data: mappingRequired })
 logger.info(LogCategory.SYSTEM, 'skipXmlConversion state:', { data: skipXmlConversion })
 logger.info(LogCategory.SYSTEM, 'Calculated mappingMode:', { data: mappingRequired ? 'WITH_MAPPING' : 'PASS_THROUGH' })
 logger.info(LogCategory.SYSTEM, 'Full flow request:', { data: JSON.stringify(flowRequest, null, 2) })

 // Save the complete flow using the flow composition API
 const response = await api.post('/flow-composition/direct-mapping', flowRequest);
 if (response.data) {
 toast({
 title: "Success",
 description: `Integration flow "${flowName}" has been created successfully.`,
 })
 navigateBack('/dashboard');
			}
		} catch (error: any) {
			logger.error(LogCategory.ERROR, 'Error saving flow', { error: error })
 logger.error(LogCategory.ERROR, 'Error response', { error: error.response })
 // Log detailed error information
 if (error.response) {
 logger.error(LogCategory.ERROR, 'Response status', { error: error.response.status })
 logger.error(LogCategory.ERROR, 'Response data', { error: error.response.data })
 logger.error(LogCategory.ERROR, 'Response headers', { error: error.response.headers })
 // Extract error message - ApiError already has the message extracted
 let errorMessage = "Failed to save the integration flow. Please try again.";
 if (error.message) {
 errorMessage = error.message;
 } else if (error.response?.data?.message) {
 errorMessage = error.response.data.message;
 } else if (error.response?.data && typeof error.response.data === 'string') {
 errorMessage = error.response.data;
 }

 // Show more specific message for common errors
 if (errorMessage.includes('already exists')) {
 errorMessage = `${errorMessage} Please choose a different name.`;
 }

 toast({
 title: "Save Failed",
 description: errorMessage,
 variant: "destructive",
 })        }
      } finally {
 setSaving(false);
 }
 };


 const handleCreateMapping = async (mappingType: 'request' | 'response' | 'fault' = 'request') => {
 if (!sourceStructure || !targetStructure) {
 toast({
 title: "Validation Error",
 description: "Please select both source and target data structures.",
 variant: "destructive",
 })
 return;
 }

 try {
 setConvertingStructures(true);

 // Store the current mapping type
 setCurrentMappingType(mappingType);

 // Convert both structures to XML using the API
 // This will ensure WSDL structures use their original content if available
 const [sourceXmlResult, targetXmlResult] = await Promise.all([
 convertStructureToXml(sourceStructure, {
 rootElementName: 'SourceMessage',
 includeXmlDeclaration: true,
 prettyPrint: true,
 convertPropertyNames: true
 }),
 convertStructureToXml(targetStructure, {
 rootElementName: 'TargetMessage',
 includeXmlDeclaration: true,
 prettyPrint: true,
 convertPropertyNames: true
 })
 ]);

 setSourceXml(sourceXmlResult.xmlContent);
 setTargetXml(targetXmlResult.xmlContent);
 setShowFieldMapping(true);
      } catch (error: any) {
 logger.error(LogCategory.ERROR, 'Error converting structures', { error: error });
 const errorDetails = error.response?.data?.details ||
 error.response?.data?.error ||
 error.message ||
 "Failed to convert structures to XML";
 logger.error(LogCategory.ERROR, 'Error details', { error: error.response?.data });
 toast({
 title: "Conversion Failed",
 description: errorDetails,
 variant: "destructive",
 });
 } finally {
 setConvertingStructures(false);
 }
 };

 if (showFieldMapping) {
 return (
 <div className="min-h-screen bg-background">
 <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
 <div className="w-full px-6 py-4">
 <div className="flex items-center gap-4">
 <Button
 variant="ghost"
 size="sm"
 onClick={() => setShowFieldMapping(false)}
 className="gap-2"
 >
 <ArrowLeft className="h-4 w-4" />
 Back to Flow Configuration
 </Button>
 <div>
 <h1 className="text-2xl font-bold tracking-tight">Field Mapping - {flowName}</h1>
 <p className="text-muted-foreground">
 Map fields between {inboundAdapter} and {outboundAdapter}
 </p>
 </div>
 </div>
 </div>
 </div>

 <FieldMappingScreen
 onClose={() => setShowFieldMapping(false)}
 onSave={(mappings, mappingName) => {
 if (currentMappingType === 'request') {
 setFieldMappings(mappings);
 // Update request mapping name if changed
 if (mappingName) {
 setRequestMappingName(mappingName);
 }
 } else {
 // Update the specific mapping in additionalMappings
 const messageType = currentMappingType === 'response' ? 'response' : currentMappingType;
 setAdditionalMappings(prev => {
 const existingIndex = prev.findIndex(m => m.messageType === messageType);
 if (existingIndex >= 0) {
 const updated = [...prev];
 updated[existingIndex] = {
 ...updated[existingIndex],
 fieldMappings: mappings,
 name: mappingName || updated[existingIndex].name
 };
 return updated;
 }
 return prev;
 });
 }
 setShowFieldMapping(false);
 toast({
 title: "Mappings Saved",
 description: `${mappings.length} ${currentMappingType} mappings have been configured.`,
 });
 }}
 initialMappingName={(() => {
 if (currentMappingType === 'request') {
 return requestMappingName || (flowName ? `${flowName} Request` : '');
 } else {
 const mapping = additionalMappings.find(m =>
 m.messageType === (currentMappingType === 'response' ? 'response' : 'fault')
 );
 return mapping?.name || (flowName ? `${flowName} ${currentMappingType.charAt(0).toUpperCase() + currentMappingType.slice(1)}` : '');
 }
 })()}
 initialMappings={(() => {
 // Return the appropriate mappings based on the current mapping type
 if (currentMappingType === 'request') {
 return fieldMappings;
 }
 const messageType = currentMappingType === 'response' ? 'response' : currentMappingType;
 const mapping = additionalMappings.find(m => m.messageType === messageType);
 return mapping?.fieldMappings || [];
 })()}
 inboundAdapterType={inboundAdapter}
 outboundAdapterType={outboundAdapter}
 sourceXml={sourceXml}
 targetXml={targetXml}
 preConverted={true}
 mappingType={currentMappingType}
 />
 </div>
 );
 }

 return (
 <div className="min-h-screen bg-background">
 <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
 <div className="px-6 py-4">
 <div className="flex items-center gap-4">
 <Button
 variant="ghost"
 size="sm"
 onClick={handleClose}
 className="gap-2"
 >
 <ArrowLeft className="h-4 w-4" />
 Back
 </Button>
 <div>
 <h1 className="text-2xl font-bold tracking-tight">{flowId ? 'Edit Integration Flow' : 'Create Integration Flow'}</h1>
 <p className="text-muted-foreground">
 {flowId ? 'Modify the configuration of your integration flow' : 'Design and configure a new message integration flow'}
 </p>
 </div>
 </div>
 </div>
 </div>

 <div className="p-6">
 <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
 <div className="lg:col-span-2 space-y-6">
 {/* Flow Details */}
 <Card className="animate-scale-in">
 <CardHeader>
 <CardTitle>Flow Details</CardTitle>
 <CardDescription>Configure the basic information for your integration flow</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="flowName">Flow Name *</Label>
 <Input
 id="flowName"
 placeholder="e.g., Customer Data Sync"
 value={flowName}
 onChange={(e) => {
 const newFlowName = e.target.value;
 setFlowName(newFlowName);
 // Update request mapping name if not manually edited
 if (!requestMappingName || requestMappingName === `${flowName} Request` || requestMappingName === 'Request Mapping') {
 setRequestMappingName(newFlowName ? `${newFlowName} Request` : '');
 }
 }}
 className="transition-all duration-300 focus:scale-[1.01]"
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="description">Description</Label>
 <Textarea
 id="description"
 placeholder="Describe what this integration flow does..."
 value={description}
 onChange={(e) => setDescription(e.target.value)}
 className="transition-all duration-300 focus:scale-[1.01]"
 rows={3}
 />
 </div>

 {/* Processing Mode */}
 <div className="border-t pt-4">
 <div className="flex items-center justify-between">
 <div className="space-y-0.5">
 <Label htmlFor="async-mode" className="text-base">Processing Mode</Label>
 <p className="text-sm text-muted-foreground">
 {isAsynchronous
 ? "Asynchronous - Process messages in the background"
 : "Synchronous - Wait for processing to complete before responding"}
 </p>
 </div>
 <div className="flex items-center gap-2">
 <span className={`text-sm ${!isAsynchronous ? 'font-medium' : 'text-muted-foreground'}`}>Sync</span>
 <Switch
 id="async-mode"
 checked={isAsynchronous}
 onCheckedChange={setIsAsynchronous}
 />
 <span className={`text-sm ${isAsynchronous ? 'font-medium' : 'text-muted-foreground'}`}>Async</span>
 </div>
 </div>
 {!isAsynchronous && (
 <div className="mt-3 p-3 bg-muted rounded-md">
 <p className="text-sm text-muted-foreground">
 <strong>Note:</strong> Synchronous mode allows multiple field mappings to be configured for complex transformations.
 </p>
 </div>
 )}
 </div>
 </CardContent>
 </Card>

 {/* Source & Target Configuration */}
 <Card className="animate-scale-in" style={{ animationDelay: '0.1s' }}>
 <CardHeader>
 <CardTitle>Source & Target Configuration</CardTitle>
 <CardDescription>Select the source and target systems for your integration</CardDescription>
 </CardHeader>
 <CardContent className="space-y-6">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
 {/* Source Configuration */}
 <div className="space-y-4">
 <h4 className="font-medium text-sm text-muted-foreground uppercase tracking-wide">Source</h4>
 <div className="space-y-3">
 <div className="space-y-2">
 <Label>Source Business Component *</Label>
 <Select value={sourceBusinessComponent} onValueChange={setSourceBusinessComponent} disabled={loading}>
 <SelectTrigger>
 <SelectValue placeholder={loading ? "Loading..." : "Select source business component"} />
 </SelectTrigger>
 <SelectContent>
 {businessComponents.map((component) => (
 <SelectItem key={component.id} value={component.id}>
 {component.name}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </div>
 <div className="space-y-2">
 <Label>Source Adapter *</Label>
 <Select value={inboundAdapter} onValueChange={handleInboundAdapterChange} disabled={loading}>
 <SelectTrigger>
 <SelectValue placeholder={loading ? "Loading..." : "Select source system"} />
 </SelectTrigger>
 <SelectContent>
 {adapters
 .filter((adapter) => adapter.mode === 'INBOUND')
 .map((adapter) => {
 const Icon = getAdapterIcon(adapter.type || adapter.name);
 return (
 <SelectItem key={adapter.id || adapter.name} value={adapter.id || adapter.name}>
 <div className="flex items-center gap-2">
 <Icon className="h-4 w-4" />
 {adapter.name}
 </div>
 </SelectItem>
 );
 })}
 </SelectContent>
 </Select>
 </div>
 <div className="space-y-2">
 <Label>Source Data Structure *</Label>
 <Select value={sourceStructure} onValueChange={setSourceStructure} disabled={loading || !sourceBusinessComponent}>
 <SelectTrigger>
 <SelectValue placeholder={loading ? "Loading..." : "Select source structure"} />
 </SelectTrigger>
 <SelectContent>
 {(Array.isArray(dataStructures) ? dataStructures : [])
 .filter(structure => structure.usage === 'source' || !structure.usage)
 .map((structure) => (
 <SelectItem key={structure.id} value={structure.id}>
 {structure.name}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </div>
 </div>
 </div>

 {/* Target Configuration */}
 <div className="space-y-4">
 <h4 className="font-medium text-sm text-muted-foreground uppercase tracking-wide">Target</h4>
 <div className="space-y-3">
 <div className="space-y-2">
 <Label>Target Business Component *</Label>
 <Select value={targetBusinessComponent} onValueChange={setTargetBusinessComponent} disabled={loading}>
 <SelectTrigger>
 <SelectValue placeholder={loading ? "Loading..." : "Select target business component"} />
 </SelectTrigger>
 <SelectContent>
 {businessComponents.map((component) => (
 <SelectItem key={component.id} value={component.id}>
 {component.name}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </div>
 <div className="space-y-2">
 <Label>Target Adapter *</Label>
 <Select value={outboundAdapter} onValueChange={handleOutboundAdapterChange} disabled={loading}>
 <SelectTrigger>
 <SelectValue placeholder={loading ? "Loading..." : "Select target system"} />
 </SelectTrigger>
 <SelectContent>
 {adapters
 .filter((adapter) => adapter.mode === 'OUTBOUND')
 .map((adapter) => {
 const Icon = getAdapterIcon(adapter.type || adapter.name);
 return (
 <SelectItem key={adapter.id || adapter.name} value={adapter.id || adapter.name}>
 <div className="flex items-center gap-2">
 <Icon className="h-4 w-4" />
 {adapter.name}
 </div>
 </SelectItem>
 );
 })}
 </SelectContent>
 </Select>
 </div>
 <div className="space-y-2">
 <Label>Target Data Structure *</Label>
 <Select value={targetStructure} onValueChange={setTargetStructure} disabled={loading || !targetBusinessComponent}>
 <SelectTrigger>
 <SelectValue placeholder={loading ? "Loading..." : "Select target structure"} />
 </SelectTrigger>
 <SelectContent>
 {(Array.isArray(dataStructures) ? dataStructures : [])
 .filter(structure => structure.usage === 'target' || !structure.usage)
 .map((structure) => (
 <SelectItem key={structure.id} value={structure.id}>
 {structure.name}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </div>
 </div>
 </div>
 </div>
 </CardContent>
 </Card>

 {/* Data Transformations */}
 <Card className="animate-scale-in" style={{ animationDelay: '0.2s' }}>
 <CardHeader>
 <CardTitle>Data Transformations</CardTitle>
 <CardDescription>Configure field mapping between source and target</CardDescription>
 </CardHeader>
 <CardContent className="space-y-6">
 {/* Mapping Toggle */}
 <div className="flex items-center justify-between">
 <div className="space-y-0.5">
 <Label htmlFor="mapping-required" className="text-base">Mapping Required</Label>
 <p className="text-sm text-muted-foreground">
 Enable if field mapping is needed between source and target
 </p>
 </div>
 <Switch
 id="mapping-required"
 checked={mappingRequired}
 onCheckedChange={(checked) => {
 logger.info(LogCategory.SYSTEM, 'Mapping Required toggle changed to', { data: checked });
 setMappingRequired(checked);
 // These are mutually exclusive
 setSkipXmlConversion(!checked);
 logger.info(LogCategory.SYSTEM, 'Skip XML Conversion automatically set to', { data: !checked });
 }}
 />
 </div>

 {/* Direct Passthrough Toggle */}
 <div className="flex items-center justify-between">
 <div className="space-y-0.5">
 <Label htmlFor="skip-xml-conversion" className="text-base">Direct File Passthrough</Label>
 <p className="text-sm text-muted-foreground">
 Skip XML conversion for direct file transfers (improved performance)
 </p>
 </div>
 <Switch
 id="skip-xml-conversion"
 checked={skipXmlConversion}
 onCheckedChange={(checked) => {
 logger.info(LogCategory.SYSTEM, 'Direct File Passthrough toggle changed to', { data: checked });
 setSkipXmlConversion(checked);
 // These are mutually exclusive
 setMappingRequired(!checked);
 logger.info(LogCategory.SYSTEM, 'Mapping Required automatically set to', { data: !checked });
 }}
 />
 </div>

 {mappingRequired && (
 <>
 <div className="border-t pt-6">
 <div className="space-y-4">
 {/* Show all mappings in sync mode, only primary in async mode */}
 {!isAsynchronous ? (
 <>
 {/* Request Mapping */}
 <div className="p-4 border rounded-lg">
 <div className="flex items-center justify-between">
 <div className="flex-1">
 <p className="text-sm font-medium">Request Mapping</p>
 <span className="text-xs px-2 py-1 bg-primary/10 text-primary rounded-md mr-2">
 Request
 </span>
 <p className="text-sm text-muted-foreground">
 {fieldMappings.length > 0
 ? `${fieldMappings.length} mappings configured`
 : 'No mappings configured yet'}
 </p>
 </div>
 <Button
 variant="outline"
 size="sm"
 onClick={() => handleCreateMapping('request')}
 disabled={!mappingRequired || !sourceBusinessComponent || !targetBusinessComponent || !inboundAdapter || !outboundAdapter || !sourceStructure || !targetStructure || convertingStructures}
 >
 Configure
 </Button>
 </div>
 </div>

 {/* Response Mapping */}
 <div className="p-4 border rounded-lg">
 <div className="flex items-center justify-between">
 <div className="flex-1">
 <p className="text-sm font-medium">Response Mapping</p>
 <span className="text-xs px-2 py-1 bg-primary/10 text-primary rounded-md mr-2">
 Response
 </span>
 <p className="text-sm text-muted-foreground">
 {(() => {
 const responseMapping = additionalMappings.find(m => m.messageType === 'response');
 return responseMapping?.fieldMappings?.length > 0
 ? `${responseMapping.fieldMappings.length} mappings configured`
 : 'No mappings configured yet';
 })()}
 </p>
 </div>
 <Button
 variant="outline"
 size="sm"
 onClick={() => {
 // Check if response mapping already exists
 const existingResponseMapping = additionalMappings.find(m => m.messageType === 'response');
 if (!existingResponseMapping) {
 const newMapping = {
 id: `response_mapping_${Date.now()}`,
 name: flowName ? `${flowName} Response` : 'Response Mapping',
 fieldMappings: [],
 messageType: 'response'
 };
 setAdditionalMappings([...additionalMappings, newMapping]);
 }
 // Handle editing the mapping
 handleCreateMapping('response');
 }}
 disabled={!mappingRequired || !sourceBusinessComponent || !targetBusinessComponent || !inboundAdapter || !outboundAdapter || !sourceStructure || !targetStructure || convertingStructures}
 >
 Configure
 </Button>
 </div>
 </div>

 {/* Fault Mapping */}
 <div className="p-4 border rounded-lg">
 <div className="flex items-center justify-between">
 <div className="flex-1">
 <p className="text-sm font-medium">Fault Mapping</p>
 <span className="text-xs px-2 py-1 bg-destructive/10 text-destructive rounded-md mr-2">
 Fault
 </span>
 <p className="text-sm text-muted-foreground">
 {(() => {
 const faultMapping = additionalMappings.find(m => m.messageType === 'fault');
 return faultMapping?.fieldMappings?.length > 0
 ? `${faultMapping.fieldMappings.length} mappings configured`
 : 'No mappings configured yet';
 })()}
 </p>
 </div>
 <Button
 variant="outline"
 size="sm"
 onClick={() => {
 // Check if fault mapping already exists
 const existingFaultMapping = additionalMappings.find(m => m.messageType === 'fault');
 if (!existingFaultMapping) {
 const newMapping = {
 id: `fault_mapping_${Date.now()}`,
 name: flowName ? `${flowName} Fault` : 'Fault Mapping',
 fieldMappings: [],
 messageType: 'fault'
 };
 setAdditionalMappings([...additionalMappings, newMapping]);
 }
 // Handle editing the mapping
 handleCreateMapping('fault');
 }}
 disabled={!mappingRequired || !sourceBusinessComponent || !targetBusinessComponent || !inboundAdapter || !outboundAdapter || !sourceStructure || !targetStructure || convertingStructures}
 >
 Configure
 </Button>
 </div>
 </div>
 </>
 ) : (
 /* Async mode - single mapping */
 <div className="p-4 border rounded-lg">
 <div className="flex items-center justify-between">
 <div className="flex-1">
 <p className="text-sm font-medium">Field Mapping</p>
 <p className="text-sm text-muted-foreground">
 {fieldMappings.length > 0
 ? `${fieldMappings.length} mappings configured`
 : 'No mappings configured yet'}
 </p>
 </div>
 <Button
 variant="outline"
 size="sm"
 onClick={handleCreateMapping}
 disabled={!mappingRequired || !sourceBusinessComponent || !targetBusinessComponent || !inboundAdapter || !outboundAdapter || !sourceStructure || !targetStructure || convertingStructures}
 >
 Configure
 </Button>
 </div>
 </div>
 )}
 </div>
 </div>

 {!mappingRequired ? (
 <p className="text-sm text-blue-600 text-center">
 Mapping is disabled in passthrough mode. Data will be transferred without transformation.
 </p>
 ) : (!sourceStructure || !targetStructure) && (
 <p className="text-sm text-orange-600 text-center">
 Please select both source and target data structures to enable mapping
 </p>
 )}
 </>
 )}

 {!mappingRequired && (
 <div className="text-center py-6 text-muted-foreground">
 <p className="text-sm">Pass-through mode enabled - data will flow without transformation</p>
 </div>
 )}
 </CardContent>
 </Card>
 </div>

 {/* Right Sidebar */}
 <div className="lg:col-span-1 space-y-6">
 <FlowActionsCard
 onSaveFlow={() => handleSave()}
 isLoading={saving}
 disabled={loading || !flowName.trim() || !inboundAdapter || !outboundAdapter}
 />

 <FlowSummaryCard
 inboundAdapter={inboundAdapter}
 outboundAdapter={outboundAdapter}
 sourceStructure={sourceStructure}
 targetStructure={targetStructure}
 selectedTransformations={[]}
 adapters={adapters.map(adapter => ({
 id: adapter.id || adapter.name,
 name: adapter.name,
 icon: getAdapterIcon(adapter.type || adapter.name),
 category: adapter.type || 'General'
 }))}
 sampleStructures={dataStructures}
 />

 <QuickTipsCard />

 {loading && (
 <Card>
 <CardContent className="flex items-center justify-center py-6">
 <div className="flex items-center gap-2 text-sm text-muted-foreground">
 <Loader2 className="h-4 w-4 animate-spin" />
 Loading component data...
 </div>
 </CardContent>
 </Card>
 )}
 </div>
 </div>
 </div>
 </div>
 );
}
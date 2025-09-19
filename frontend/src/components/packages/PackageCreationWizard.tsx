import { useState, useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Spinner } from '@/components/loading-states';
import { useToast } from '@/hooks/use-toast';
import { packageService } from '@/services/packageService';
import { communicationAdapterService } from '@/services/communicationAdapterService';
import { dataStructureService } from '@/services/dataStructureService';
import { integrationFlowService } from '@/services/integrationFlowService';
import { flowService } from '@/services/flowService';
import { OrchestrationTargetService } from '@/services/orchestrationTargetService';
import { TargetFieldMappingService } from '@/services/targetFieldMappingService';
import type { IntegrationPackage, CreatePackageRequest } from '@/types/package';
import type { AdapterType } from '@/types/communicationAdapter';
import { FieldMappingScreen } from '../FieldMappingScreen';
import { OrchestrationTargetManager } from '../orchestration/OrchestrationTargetManager';
import { WsdlValidator } from '../structure/WsdlValidator';
import { JsonSchemaEditor } from '../structure/JsonSchemaEditor';
import { convertStructureToXml } from '@/utils/xmlStructureConverter';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { isApiResponse } from '@/lib/api-response-utils';
import { logger, LogCategory } from '@/lib/logger';
import {
 Package,
 Settings,
 Database,
 GitBranch,
 CheckCircle2,
 AlertCircle,
 ArrowRight,
 ArrowLeft,
 X,
 FileCode,
 Webhook,
 Layers,
 Send,
 MessageSquare,
 Globe,
 Server,
 Plus,
 Trash2,
 Copy,
 Check,
 Clock
} from 'lucide-react';

interface PackageCreationWizardProps {
 isOpen: boolean;
 onClose: () => void;
 onSuccess: () => void;
 existingPackage?: IntegrationPackage | null;
}

type WizardStep =
 | 'package-info'
 | 'flow-type-selection'
 | 'source-adapter'
 | 'source-flow-structure'
 | 'source-message-structure'
 | 'target-adapter'
 | 'target-flow-structure'
 | 'target-message-structure'
 | 'response-structure'
 | 'field-mapping'
 | 'orchestration-targets'
 | 'orchestration-structures'
 | 'orchestration-mappings'
 | 'integration-flow'
 | 'review';

interface WizardData {
 // Package info
 packageName: string;
 packageDescription: string;
 transformationRequired: boolean;
 syncType: 'SYNCHRONOUS' | 'ASYNCHRONOUS';
 sourceNamespace: string;
 targetNamespace: string;

 // Flow type
 flowType: 'DIRECT_INTEGRATION' | 'ORCHESTRATION' | '';

 // Adapters
 inboundAdapterName: string;
 inboundAdapterType: AdapterType | '';
 inboundAdapterConfig: any;

 outboundAdapterName: string;
 outboundAdapterType: AdapterType | '';
 outboundAdapterConfig: any;

 // Structures
 sourceFlowStructure?: any;
 sourceMessageStructure?: any;
 targetFlowStructure?: any;
 targetMessageStructure?: any;
 responseStructure?: any;

 // Flow
 flowName: string;
 flowDescription: string;

 // Field mappings
 fieldMappings?: any[];
 responseMappings?: any[];
 sourceXml?: string;
 targetXml?: string;

 // Orchestration specific
 orchestrationTargets?: Array<{
 adapterName: string;
 adapterType: AdapterType | '';
 adapterConfig: any;
 structure?: any;
 mapping?: any[];
 }>;
}

const ADAPTER_TYPES: { value: AdapterType; label: string }[] = [
 { value: 'FILE', label: 'File' },
 { value: 'FTP', label: 'FTP' },
 { value: 'SFTP', label: 'SFTP' },
 { value: 'HTTP', label: 'HTTP' },
 { value: 'REST', label: 'REST' },
 { value: 'SOAP', label: 'SOAP' },
 { value: 'IBMMQ', label: 'IBM MQ' },
 { value: 'JDBC', label: 'JDBC' },
 { value: 'IDOC', label: 'IDoc' },
 { value: 'ODATA', label: 'OData' },
 { value: 'MAIL', label: 'Mail' },
 { value: 'RFC', label: 'RFC' }
];

export default function PackageCreationWizard({
 isOpen,
 onClose,
 onSuccess,
 existingPackage
}: PackageCreationWizardProps) {
 const { toast } = useToast();
 const [currentStep, setCurrentStep] = useState<WizardStep>('package-info');
 const [isLoading, setIsLoading] = useState(false);
 const [isSaving, setIsSaving] = useState(false);
 const [convertingStructures, setConvertingStructures] = useState(false);
 const [showFullScreenMapping, setShowFullScreenMapping] = useState(false);
 const [selectedTargetIndex, setSelectedTargetIndex] = useState<number | null>(null);

 const [wizardData, setWizardData] = useState<WizardData>({
 packageName: '',
 packageDescription: '',
 transformationRequired: true,
 syncType: 'ASYNCHRONOUS',
 sourceNamespace: '',
 targetNamespace: '',
 flowType: '',
 inboundAdapterName: '',
 inboundAdapterType: '',
 inboundAdapterConfig: {},
 outboundAdapterName: '',
 outboundAdapterType: '',
 outboundAdapterConfig: {},
 flowName: '',
 flowDescription: '',
 orchestrationTargets: []
 });

 // Determine which steps to show based on selections
 const getStepSequence = useCallback((): WizardStep[] => {
 const steps: WizardStep[] = ['package-info', 'flow-type-selection'];

 // If no flow type selected yet, stop here
 if (!wizardData.flowType) {
 return steps;
 }

 if (wizardData.flowType === 'DIRECT_INTEGRATION') {
 // Direct Integration flow steps
 // Source adapter
 steps.push('source-adapter');

 // Source structures
 if (wizardData.inboundAdapterType === 'SOAP') {
 steps.push('source-flow-structure');
 } else if (wizardData.transformationRequired) {
 steps.push('source-message-structure');
 }

 // Target adapter
 steps.push('target-adapter');

 // Target structures
 if (wizardData.outboundAdapterType === 'SOAP') {
 steps.push('target-flow-structure');
 } else if (wizardData.transformationRequired) {
 steps.push('target-message-structure');
 }

 // Response structure for synchronous
 if (wizardData.syncType === 'SYNCHRONOUS' && wizardData.transformationRequired) {
 steps.push('response-structure');
 }

 // Field mapping step for transformation
 if (wizardData.transformationRequired) {
 steps.push('field-mapping');
 }
 } else if (wizardData.flowType === 'ORCHESTRATION') {
 // Orchestration flow steps
 // Source adapter (single)
 steps.push('source-adapter')
 if (wizardData.inboundAdapterType === 'SOAP') {
 steps.push('source-flow-structure');
 } else if (wizardData.transformationRequired) {
 steps.push('source-message-structure');
 }

 // Orchestration-specific steps
 steps.push('orchestration-targets'); // Configure multiple target adapters
 steps.push('orchestration-structures'); // Configure structures for each target
 if (wizardData.transformationRequired) {
 steps.push('orchestration-mappings'); // Configure mappings for each target
 }
 }

 // Integration flow
 steps.push('integration-flow');

 // Review
 steps.push('review');

 return steps;
 }, [wizardData]);

 const handleNext = () => {
 const steps = getStepSequence();
 const currentIndex = steps.indexOf(currentStep);
 if (currentIndex < steps.length - 1) {
 setCurrentStep(steps[currentIndex + 1]);
 }
 };

 const handleBack = () => {
 const steps = getStepSequence();
 const currentIndex = steps.indexOf(currentStep);
 if (currentIndex > 0) {
 setCurrentStep(steps[currentIndex - 1]);
 }
 };

 const getStepNumber = () => {
 const steps = getStepSequence();
 return {
 current: steps.indexOf(currentStep) + 1,
 total: steps.length
 };
 };

 const handleCreatePackage = async () => {
    try {
 setIsSaving(true);

 // Create package
 const packageData: CreatePackageRequest = {
 name: wizardData.packageName,
 description: wizardData.packageDescription,
 transformationRequired: wizardData.transformationRequired,
 syncType: wizardData.syncType,
 sourceNamespace: wizardData.sourceNamespace,
 targetNamespace: wizardData.targetNamespace
 };

 const packageResponse = await packageService.createPackage(packageData);

 let packageId: string;
 if (isApiResponse<IntegrationPackage>(packageResponse)) {
 if (!packageResponse.success || !packageResponse.data) {
 throw new Error('Failed to create package');
 }
 packageId = packageResponse.data.id;
 } else {
 packageId = packageResponse.id;
 }

 // Create source adapter
 const inboundAdapterResponse = await communicationAdapterService.createAdapter({
 name: wizardData.inboundAdapterName,
 type: wizardData.inboundAdapterType as AdapterType,
 mode: 'INBOUND',
 configuration: wizardData.inboundAdapterConfig,
 isActive: true,
 packageId
 });

 if (!inboundAdapterResponse.success || !inboundAdapterResponse.data) {
 throw new Error('Failed to create source adapter');
 }

 // Create target adapter(s)
 let outboundAdapterResponse;
 const outboundAdapterIds: string[] = [];

 if (wizardData.flowType === 'ORCHESTRATION') {
 // Create multiple target adapters for orchestration
 for (const target of wizardData.orchestrationTargets || []) {
 const response = await communicationAdapterService.createAdapter({
 name: target.adapterName,
 type: target.adapterType as AdapterType,
 mode: 'OUTBOUND',
 configuration: target.adapterConfig,
 isActive: true,
 packageId
 });

 if (!response.success || !response.data) {
 throw new Error(`Failed to create target adapter: ${target.adapterName}`);
 }

 outboundAdapterIds.push(response.data.id);

 // Store the first target as the primary for flow creation
 if (!outboundAdapterResponse) {
 outboundAdapterResponse = response;
 }
 }
 } else {
 // Create single target adapter for direct integration
 outboundAdapterResponse = await communicationAdapterService.createAdapter({
 name: wizardData.outboundAdapterName,
 type: wizardData.outboundAdapterType as AdapterType,
 mode: 'OUTBOUND',
 configuration: wizardData.outboundAdapterConfig,
 isActive: true,
 packageId
 });

 if (!outboundAdapterResponse.success || !outboundAdapterResponse.data) {
 throw new Error('Failed to create target adapter');
 }
 }

 // Create structures based on requirements
 let sourceStructureId: string | undefined;
 let targetStructureId: string | undefined;
 let responseStructureId: string | undefined;

 if (wizardData.inboundAdapterType === 'SOAP' && wizardData.sourceFlowStructure) {
 // Create source flow structure
 const structureResponse = await dataStructureService.createStructure({
 name: `${wizardData.packageName}_Source_Flow`,
 type: 'wsdl',
 content: wizardData.sourceFlowStructure,
 packageId
 });
 if (structureResponse.success && structureResponse.data) {
 sourceStructureId = structureResponse.data.id;
 }
 } else if (wizardData.transformationRequired && wizardData.sourceMessageStructure) {
 // Create source message structure
 const structureResponse = await dataStructureService.createStructure({
 name: `${wizardData.packageName}_Source_Message`,
 type: 'json',
 content: wizardData.sourceMessageStructure,
 packageId
 });
 if (structureResponse.success && structureResponse.data) {
 sourceStructureId = structureResponse.data.id;
 }
 }

 // Handle structure creation based on flow type
 const targetStructureIds: Record<string, string> = {};

 if (wizardData.flowType === 'ORCHESTRATION') {
 // Create structures for each orchestration target
 for (let i = 0; i < (wizardData.orchestrationTargets?.length || 0); i++) {
 const target = wizardData.orchestrationTargets![i];
 const adapterId = outboundAdapterIds[i];

 if (target.structure) {
 const structureType = target.adapterType === 'SOAP' ? 'wsdl' : 'json';
 const structureResponse = await dataStructureService.createStructure({
 name: `${wizardData.packageName}_${target.adapterName}_Structure`,
 type: structureType,
 content: target.structure,
 packageId
 });
 if (structureResponse.success && structureResponse.data) {
 targetStructureIds[adapterId] = structureResponse.data.id;
 // Set the first one as the primary target structure
 if (i === 0) {
 targetStructureId = structureResponse.data.id;
 }
 }
 }
 }
 } else {
 // Direct integration flow - single target
 if (wizardData.outboundAdapterType === 'SOAP' && wizardData.targetFlowStructure) {
 // Create target flow structure
 const structureResponse = await dataStructureService.createStructure({
 name: `${wizardData.packageName}_Target_Flow`,
 type: 'wsdl',
 content: wizardData.targetFlowStructure,
 packageId
 });
 if (structureResponse.success && structureResponse.data) {
 targetStructureId = structureResponse.data.id;
 }
 } else if (wizardData.transformationRequired && wizardData.targetMessageStructure) {
 // Create target message structure
 const structureResponse = await dataStructureService.createStructure({
 name: `${wizardData.packageName}_Target_Message`,
 type: 'json',
 content: wizardData.targetMessageStructure,
 packageId
 });
 if (structureResponse.success && structureResponse.data) {
 targetStructureId = structureResponse.data.id;
 }
 }
 }

 if (wizardData.syncType === 'SYNCHRONOUS' && wizardData.transformationRequired && wizardData.responseStructure) {
 // Create response structure
 const structureResponse = await dataStructureService.createStructure({
 name: `${wizardData.packageName}_Response`,
 type: 'json',
 content: wizardData.responseStructure,
 packageId
 });
 if (structureResponse.success && structureResponse.data) {
 responseStructureId = structureResponse.data.id;
 }
 }

 // Create integration flow
 const flowType = wizardData.flowType === 'ORCHESTRATION' ? 'ORCHESTRATION' : 'DIRECT_MAPPING';
 const flowResponse = await integrationFlowService.createFlow({
 name: wizardData.flowName,
 description: wizardData.flowDescription,
 type: flowType,
 isActive: true,
 inboundAdapterId: inboundAdapterResponse.data.id,
 outboundAdapterId: outboundAdapterResponse?.data?.id || '',
 sourceStructureId,
 targetStructureId,
 responseStructureId,
 packageId
 });

 if (!flowResponse.success || !flowResponse.data) {
 throw new Error('Failed to create integration flow');
 }

 const flowId = flowResponse.data.id;
 
 // Handle orchestration flow targets and mappings
 if (wizardData.flowType === 'ORCHESTRATION') {
 // Create orchestration targets using the new API
 for (let i = 0; i < wizardData.orchestrationTargets!.length; i++) {
 const target = wizardData.orchestrationTargets![i];
 const adapterId = outboundAdapterIds[i];
 const structureId = targetStructureIds[adapterId];
 
 // Create orchestration target
 const targetResponse = await OrchestrationTargetService.addTarget(flowId, {
 targetAdapterId: adapterId,
 executionOrder: i,
 parallel: false,
 conditionType: 'ALWAYS',
 structureId: structureId,
 awaitResponse: false,
 timeoutMs: 30000,
 retryPolicy: OrchestrationTargetService.getDefaultRetryPolicy(),
 errorStrategy: 'FAIL_FLOW',
 active: true,
 description: `Target adapter: ${target.adapterName}`
 });
 
 // Create field mappings for this target if transformation is required
 if (wizardData.transformationRequired && target.mapping && target.mapping.length > 0) {
 const mappingRequests = target.mapping.map((mapping: any, idx: number) => ({
 sourceFieldPath: mapping.sourcePaths?.[0] || mapping.sourceFields?.[0] || '',
 targetFieldPath: mapping.targetPath || mapping.targetField || '',
 mappingType: mapping.functionNode ? 'FUNCTION' : 'DIRECT',
 transformationExpression: mapping.javaFunction || mapping.functionNode ? JSON.stringify(mapping.functionNode) : undefined,
 required: mapping.required || false,
 mappingOrder: idx,
 active: true,
 description: mapping.name || `Mapping ${idx + 1}`,
 visualFlowData: mapping.visualFlowData ? JSON.stringify(mapping.visualFlowData) : undefined
 }));
 
 await TargetFieldMappingService.createMappings(
 flowId, 
 targetResponse.id, 
 mappingRequests
 );
 }
 }
 } else {
 // Handle direct integration mappings (unchanged)
 if (wizardData.transformationRequired && wizardData.fieldMappings && wizardData.fieldMappings.length > 0) {
 await flowService.saveFieldMappings(flowId, wizardData.fieldMappings);
 }
 }

 toast({
 title: 'Success',
 description: 'Package created successfully with all components'
 });

 onSuccess();
 
} catch (error) {
 logger.error(LogCategory.UI, 'Error creating package', { error: error });
 toast({
 title: 'Error',
 description: error instanceof Error ? error.message : 'Failed to create package',
 variant: 'destructive'
 });
 } finally {
 setIsSaving(false);
 }
 };

 const renderStepContent = () => {
 switch (currentStep) {
 case 'package-info':
 return (
 <div className="space-y-6">
 <div className="grid gap-4">
 <div>
 <Label htmlFor="packageName">Package Name *</Label>
 <Input
 id="packageName"
 value={wizardData.packageName}
 onChange={(e) => setWizardData({ ...wizardData, packageName: e.target.value })}
 placeholder="Enter package name"
 className="mt-1"
 />
 </div>

 <div>
 <Label htmlFor="packageDescription">Description</Label>
 <Textarea
 id="packageDescription"
 value={wizardData.packageDescription}
 onChange={(e) => setWizardData({ ...wizardData, packageDescription: e.target.value })}
 placeholder="Enter package description"
 className="mt-1"
 rows={3}
 />
 </div>

 <div className="space-y-3">
 <Label>Transformation Required?</Label>
 <RadioGroup
 value={wizardData.transformationRequired.toString()}
 onValueChange={(value) =>
 setWizardData({ ...wizardData, transformationRequired: value === 'true' })
 }
 >
 <div className="flex items-center space-x-2">
 <RadioGroupItem value="true" id="transform-yes" />
 <Label htmlFor="transform-yes">Yes - Data transformation needed</Label>
 </div>
 <div className="flex items-center space-x-2">
 <RadioGroupItem value="false" id="transform-no" />
 <Label htmlFor="transform-no">No - Pass through only</Label>
 </div>
 </RadioGroup>
 </div>

 <div className="space-y-3">
 <Label>Integration Type</Label>
 <RadioGroup
 value={wizardData.syncType}
 onValueChange={(value: 'SYNCHRONOUS' | 'ASYNCHRONOUS') =>
 setWizardData({ ...wizardData, syncType: value })
 }
 >
 <div className="flex items-center space-x-2">
 <RadioGroupItem value="SYNCHRONOUS" id="sync" />
 <Label htmlFor="sync">Synchronous - Response required</Label>
 </div>
 <div className="flex items-center space-x-2">
 <RadioGroupItem value="ASYNCHRONOUS" id="async" />
 <Label htmlFor="async">Asynchronous - Fire and forget</Label>
 </div>
 </RadioGroup>
 </div>

 {wizardData.transformationRequired && (
 <div className="space-y-4 pt-4 border-t">
 <h4 className="font-medium">Namespace Configuration</h4>
 <div>
 <Label htmlFor="sourceNamespace">Source Namespace</Label>
 <Input
 id="sourceNamespace"
 value={wizardData.sourceNamespace}
 onChange={(e) => setWizardData({ ...wizardData, sourceNamespace: e.target.value })}
 placeholder="http://example.com/source"
 className="mt-1"
 />
 </div>
 <div>
 <Label htmlFor="targetNamespace">Target Namespace</Label>
 <Input
 id="targetNamespace"
 value={wizardData.targetNamespace}
 onChange={(e) => setWizardData({ ...wizardData, targetNamespace: e.target.value })}
 placeholder="http://example.com/target"
 className="mt-1"
 />
 </div>
 </div>
 )}
 </div>
 </div>
 );

 case 'flow-type-selection':
 return (
 <div className="space-y-6">
 <div className="bg-blue-50 dark:bg-blue-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Select Flow Type</h4>
 <p className="text-sm text-muted-foreground">
 Choose the type of integration flow for this package
 </p>
 </div>

 <div className="space-y-3">
 <Label>Flow Type *</Label>
 <RadioGroup
 value={wizardData.flowType}
 onValueChange={(value: 'DIRECT_INTEGRATION' | 'ORCHESTRATION') =>
 setWizardData({ ...wizardData, flowType: value })
 }
                >
                <Card className={`cursor-pointer transition-all hover:shadow-md ${wizardData.flowType === 'DIRECT_INTEGRATION' ? 'ring-2 ring-primary shadow-md' : ''}`}>
 <CardContent className="p-6">
 <div className="flex items-start space-x-4">
 <RadioGroupItem value="DIRECT_INTEGRATION" id="direct-integration" className="mt-1" />
 <div className="flex-1">
 <Label htmlFor="direct-integration" className="cursor-pointer">
 <div className="flex items-center gap-2 mb-2">
 <ArrowRight className="w-5 h-5 text-primary" />
 <span className="font-semibold text-lg">Direct Integration Flow</span>
 </div>
 <div className="text-sm text-muted-foreground mb-3">
 Simple point-to-point integration with one source adapter, one target adapter, and optional field mapping
 </div>
 <div className="flex items-center gap-4 text-xs text-muted-foreground">
 <div className="flex items-center gap-1">
 <Globe className="w-3 h-3" />
 <span>1 Source</span>
 </div>
 <div className="flex items-center gap-1">
 <Server className="w-3 h-3" />
 <span>1 Target</span>
 </div>
 <div className="flex items-center gap-1">
 <Settings className="w-3 h-3" />
 <span>1 Mapping</span>
 </div>
 </div>
 </Label>
 </div>
 </div>
 </CardContent>
 </Card>
                <Card className={`cursor-pointer transition-all hover:shadow-md ${wizardData.flowType === 'ORCHESTRATION' ? 'ring-2 ring-primary shadow-md' : ''}`}>
 <CardContent className="p-6">
 <div className="flex items-start space-x-4">
 <RadioGroupItem value="ORCHESTRATION" id="orchestration" className="mt-1" />
 <div className="flex-1">
 <Label htmlFor="orchestration" className="cursor-pointer">
 <div className="flex items-center gap-2 mb-2">
 <GitBranch className="w-5 h-5 text-primary" />
 <span className="font-semibold text-lg">Orchestration Flow</span>
 </div>
 <div className="text-sm text-muted-foreground mb-3">
 Complex integration with one source adapter, multiple target adapters, and multiple field mappings
 </div>
 <div className="flex items-center gap-4 text-xs text-muted-foreground">
 <div className="flex items-center gap-1">
 <Globe className="w-3 h-3" />
 <span>1 Source</span>
 </div>
 <div className="flex items-center gap-1">
 <Server className="w-3 h-3" />
 <span>N Targets</span>
 </div>
 <div className="flex items-center gap-1">
 <Settings className="w-3 h-3" />
 <span>N Mappings</span>
 </div>
 </div>
 </Label>
 </div>
 </div>
 </CardContent>
 </Card>
 </RadioGroup>
 </div>
 </div>
 );

 case 'source-adapter':
 return (
 <div className="space-y-6">
 <div className="bg-blue-50 dark:bg-blue-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Source Adapter (Inbound)</h4>
 <p className="text-sm text-muted-foreground">
 This adapter receives data FROM external systems (outbound/receiver in traditional terms)
 </p>
 </div>

 <div className="space-y-4">
 <div>
 <Label htmlFor="inboundAdapterName">Adapter Name *</Label>
 <Input
 id="inboundAdapterName"
 value={wizardData.inboundAdapterName}
 onChange={(e) => setWizardData({ ...wizardData, inboundAdapterName: e.target.value })}
 placeholder="Enter source adapter name"
 className="mt-1"
 />
 </div>

 <div>
 <Label htmlFor="inboundAdapterType">Adapter Type *</Label>
 <Select
 value={wizardData.inboundAdapterType}
 onValueChange={(value) => setWizardData({ ...wizardData, inboundAdapterType: value as AdapterType })}
 >
 <SelectTrigger className="mt-1">
 <SelectValue placeholder="Select adapter type" />
 </SelectTrigger>
 <SelectContent>
 {ADAPTER_TYPES.map((type) => (
 <SelectItem key={type.value} value={type.value}>
 {type.label}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 {wizardData.inboundAdapterType === 'SOAP' && (
 <p className="text-sm text-blue-600 mt-2">
 ℹ️ SOAP adapter selected - Flow Structure (WSDL) will be required in the next step
 </p>
 )}
 </div>

 <div>
 <Label>Basic Configuration</Label>
 <Textarea
 value={JSON.stringify(wizardData.inboundAdapterConfig, null, 2)}
 onChange={(e) => {
 try {
 const config = JSON.parse(e.target.value);
 setWizardData({ ...wizardData, inboundAdapterConfig: config });
  } catch (error) {
 // Invalid JSON, don't update
 }
 }}
 placeholder={'{\n "endpoint": "http://example.com"\n}'}
 className="mt-1 font-mono text-sm"
 rows={6}
 />
 </div>
 </div>
 </div>
 );

 case 'source-flow-structure':
 return (
 <div className="space-y-6">
 <div className="bg-blue-50 dark:bg-blue-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Source Flow Structure (WSDL)</h4>
 <p className="text-sm text-muted-foreground">
 Define the WSDL structure for the SOAP source adapter
 </p>
 </div>

 <WsdlValidator
 initialContent={wizardData.sourceFlowStructure || ''}
 onChange={(content, isValid) => {
 setWizardData({ ...wizardData, sourceFlowStructure: content });
 }}
 height="500px"
 autoValidate={true}
 />
 </div>
 );

 case 'source-message-structure':
 return (
 <div className="space-y-6">
 <div className="bg-blue-50 dark:bg-blue-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Source Message Structure</h4>
 <p className="text-sm text-muted-foreground">
 Define the structure for incoming messages
 </p>
 </div>

 <JsonSchemaEditor
 initialContent={wizardData.sourceMessageStructure || ''}
 onChange={(content, isValid) => {
 setWizardData({ ...wizardData, sourceMessageStructure: content });
 }}
 height="500px"
 autoValidate={true}
 />
 </div>
 );

 case 'target-adapter':
 return (
 <div className="space-y-6">
 <div className="bg-green-50 dark:bg-green-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Target Adapter (Outbound)</h4>
 <p className="text-sm text-muted-foreground">
 This adapter sends data TO external systems (inbound/sender in traditional terms)
 </p>
 </div>

 <div className="space-y-4">
 <div>
 <Label htmlFor="outboundAdapterName">Adapter Name *</Label>
 <Input
 id="outboundAdapterName"
 value={wizardData.outboundAdapterName}
 onChange={(e) => setWizardData({ ...wizardData, outboundAdapterName: e.target.value })}
 placeholder="Enter target adapter name"
 className="mt-1"
 />
 </div>

 <div>
 <Label htmlFor="outboundAdapterType">Adapter Type *</Label>
 <Select
 value={wizardData.outboundAdapterType}
 onValueChange={(value) => setWizardData({ ...wizardData, outboundAdapterType: value as AdapterType })}
 >
 <SelectTrigger className="mt-1">
 <SelectValue placeholder="Select adapter type" />
 </SelectTrigger>
 <SelectContent>
 {ADAPTER_TYPES.map((type) => (
 <SelectItem key={type.value} value={type.value}>
 {type.label}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 {wizardData.outboundAdapterType === 'SOAP' && (
 <p className="text-sm text-blue-600 mt-2">
 ℹ️ SOAP adapter selected - Flow Structure (WSDL) will be required in the next step
 </p>
 )}
 </div>

 <div>
 <Label>Basic Configuration</Label>
 <Textarea
 value={JSON.stringify(wizardData.outboundAdapterConfig, null, 2)}
 onChange={(e) => {
 try {
 const config = JSON.parse(e.target.value);
 setWizardData({ ...wizardData, outboundAdapterConfig: config });
  } catch (error) {
 // Invalid JSON, don't update
 }
 }}
 placeholder={'{\n "endpoint": "http://example.com"\n}'}
 className="mt-1 font-mono text-sm"
 rows={6}
 />
 </div>
 </div>
 </div>
 );

 case 'target-flow-structure':
 return (
 <div className="space-y-6">
 <div className="bg-green-50 dark:bg-green-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Target Flow Structure (WSDL)</h4>
 <p className="text-sm text-muted-foreground">
 Define the WSDL structure for the SOAP target adapter
 </p>
 </div>

 <WsdlValidator
 initialContent={wizardData.targetFlowStructure || ''}
 onChange={(content, isValid) => {
 setWizardData({ ...wizardData, targetFlowStructure: content });
 }}
 height="500px"
 autoValidate={true}
 />
 </div>
 );

 case 'target-message-structure':
 return (
 <div className="space-y-6">
 <div className="bg-green-50 dark:bg-green-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Target Message Structure</h4>
 <p className="text-sm text-muted-foreground">
 Define the structure for outgoing messages
 </p>
 </div>

 <JsonSchemaEditor
 initialContent={wizardData.targetMessageStructure || ''}
 onChange={(content, isValid) => {
 setWizardData({ ...wizardData, targetMessageStructure: content });
 }}
 height="500px"
 autoValidate={true}
 />
 </div>
 );

 case 'response-structure':
 return (
 <div className="space-y-6">
 <div className="bg-amber-50 dark:bg-amber-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Response Structure</h4>
 <p className="text-sm text-muted-foreground">
 Define the structure for synchronous response messages
 </p>
 </div>

 <JsonSchemaEditor
 initialContent={wizardData.responseStructure || ''}
 onChange={(content, isValid) => {
 setWizardData({ ...wizardData, responseStructure: content });
 }}
 height="500px"
 autoValidate={true}
 />
 </div>
 );

 case 'field-mapping': {
 const handlePrepareMapping = async () => {
    try {
 setConvertingStructures(true);

 // Get the source and target structures
 let sourceContent = '';
 let targetContent = '';

 if (wizardData.inboundAdapterType === 'SOAP' && wizardData.sourceFlowStructure) {
 sourceContent = wizardData.sourceFlowStructure;
 } else if (wizardData.sourceMessageStructure) {
 sourceContent = wizardData.sourceMessageStructure;
 }

 if (wizardData.outboundAdapterType === 'SOAP' && wizardData.targetFlowStructure) {
 targetContent = wizardData.targetFlowStructure;
 } else if (wizardData.targetMessageStructure) {
 targetContent = wizardData.targetMessageStructure;
 }

 // Convert structures to XML
 const [sourceXmlResult, targetXmlResult] = await Promise.all([
 convertStructureToXml(sourceContent, {
 rootElementName: 'SourceMessage',
 includeXmlDeclaration: true,
 prettyPrint: true,
 convertPropertyNames: true
 }),
 convertStructureToXml(targetContent, {
 rootElementName: 'TargetMessage',
 includeXmlDeclaration: true,
 prettyPrint: true,
 convertPropertyNames: true
 })
 ]);

 setWizardData({
 ...wizardData,
 sourceXml: sourceXmlResult.xmlContent,
 targetXml: targetXmlResult.xmlContent
 });

 setShowFullScreenMapping(true);
  } catch (error) {
 logger.error(LogCategory.UI, 'Error converting structures', { error: error });
 toast({
 title: 'Conversion Failed',
 description: 'Failed to convert structures for field mapping',
 variant: 'destructive'
 });
 } finally {
 setConvertingStructures(false);
 }
 };

 return (
 <div className="space-y-6">
 <div className="bg-indigo-50 dark:bg-indigo-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Field Mapping</h4>
 <p className="text-sm text-muted-foreground">
 Map fields between source and target structures for data transformation
 </p>
 </div>

 {wizardData.fieldMappings && wizardData.fieldMappings.length > 0 ? (
 <Card>
 <CardHeader>
 <CardTitle className="text-lg">Current Mappings</CardTitle>
 </CardHeader>
 <CardContent>
 <div className="space-y-2">
 <p className="text-sm text-muted-foreground">
 {wizardData.fieldMappings.length} field mapping(s) configured
 </p>
 <Button
 variant="outline"
 onClick={handlePrepareMapping}
 disabled={convertingStructures}
 >
 {convertingStructures ? (
 <>
 <Spinner size="sm" className="mr-2" />
 Preparing...
 </>
 ) : (
 'Edit Mappings'
 )}
 </Button>
 </div>
 </CardContent>
 </Card>
 ) : (
 <Card>
 <CardContent className="pt-6">
 <div className="text-center space-y-4">
 <MessageSquare className="w-12 h-12 mx-auto text-muted-foreground" />
 <div>
 <h3 className="font-medium">No Field Mappings Configured</h3>
 <p className="text-sm text-muted-foreground mt-1">
 Click below to create field mappings between source and target
 </p>
 </div>
 <Button
 onClick={handlePrepareMapping}
 disabled={convertingStructures}
 >
 {convertingStructures ? (
 <>
 <Spinner size="sm" className="mr-2" />
 Preparing...
 </>
 ) : (
 'Create Field Mappings'
 )}
 </Button>
 </div>
 </CardContent>
 </Card>
 )}

 {!wizardData.transformationRequired && (
 <Alert>
 <AlertCircle className="h-4 w-4" />
 <AlertDescription>
 Field mapping is only available when transformation is required.
 Go back to Package Information to enable transformation.
 </AlertDescription>
 </Alert>
 )}
 </div>
 );
 }

 case 'orchestration-targets': {
 // For now, we'll use mock flow ID - in real scenario, the flow would be created first
 const mockFlowId = 'temp-flow-' + Date.now();
 
 // Transform ADAPTER_TYPES into format expected by OrchestrationTargetManager
 const availableAdapters = ADAPTER_TYPES.map((type, index) => ({
 id: `new-adapter-${type.value}-${index}`,
 name: `New ${type.label} Adapter`,
 type: type.value
 }));
 
 return (
 <div className="space-y-6">
 <div className="bg-green-50 dark:bg-green-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Target Adapters Configuration</h4>
 <p className="text-sm text-muted-foreground">
 Configure multiple target adapters for orchestration flow with drag-and-drop reordering
 </p>
 </div>
 
 <OrchestrationTargetManager
 flowId={mockFlowId}
 availableAdapters={availableAdapters}
 onTargetsChange={(targets) => {
 // Convert targets to wizard format
 const wizardTargets = targets.map(target => ({
 adapterName: target.adapterName || '',
 adapterType: (target.adapterType || '') as AdapterType | '',
 adapterConfig: {},
 structure: null,
 mapping: []
 }));
 setWizardData({ ...wizardData, orchestrationTargets: wizardTargets });
 }}
 />
 </div>
 );
 }

 case 'orchestration-structures':
 return (
 <div className="space-y-6">
 <div className="bg-blue-50 dark:bg-blue-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Target Structures Configuration</h4>
 <p className="text-sm text-muted-foreground">
 Define data structures for each target adapter
 </p>
 </div>

 <div className="space-y-4">
 {wizardData.orchestrationTargets?.map((target, index) => (
 <Card key={index}>
 <CardHeader>
 <CardTitle className="text-base">{target.adapterName}</CardTitle>
 <CardDescription>
 Configure structure for {target.adapterType} adapter
 </CardDescription>
 <div className="flex items-center gap-2 mt-2">
 {target.structure ? (
 <Badge variant="outline" className="text-green-600 border-green-600">
 <Check className="w-3 h-3 mr-1" />
 Structure Configured
 </Badge>
 ) : (
 <Badge variant="outline" className="text-yellow-600 border-yellow-600">
 <Clock className="w-3 h-3 mr-1" />
 Pending Configuration
 </Badge>
 )}
 </div>
 </CardHeader>
 <CardContent>
 {target.adapterType === 'SOAP' ? (
 <div>
 <Label htmlFor={`wsdl-${index}`}>WSDL Content</Label>
 <Textarea
 id={`wsdl-${index}`}
 value={target.structure || ''}
 onChange={(e) => {
 const targets = [...(wizardData.orchestrationTargets || [])];
 targets[index] = { ...targets[index], structure: e.target.value };
 setWizardData({ ...wizardData, orchestrationTargets: targets });
 }}
 placeholder="Paste WSDL content here..."
 className="mt-1 font-mono text-sm"
 rows={8}
 />
 </div>
 ) : wizardData.transformationRequired ? (
 <div>
 <Label htmlFor={`json-${index}`}>JSON Schema</Label>
 <Textarea
 id={`json-${index}`}
 value={target.structure || ''}
 onChange={(e) => {
 const targets = [...(wizardData.orchestrationTargets || [])];
 targets[index] = { ...targets[index], structure: e.target.value };
 setWizardData({ ...wizardData, orchestrationTargets: targets });
 }}
 placeholder={'{\n "type": "object",\n "properties": {\n \n }\n}'}
 className="mt-1 font-mono text-sm"
 rows={8}
 />
 </div>
 ) : (
 <p className="text-sm text-muted-foreground">
 No structure needed for pass-through mode
 </p>
 )}
 </CardContent>
 </Card>
 ))}
 </div>
 </div>
 );

 case 'orchestration-mappings': {
 const handlePrepareOrchestrationMapping = async (targetIndex: number) => {
 try {
 setConvertingStructures(true);
 setSelectedTargetIndex(targetIndex);

 const target = wizardData.orchestrationTargets?.[targetIndex];
 if (!target) return;

 // Get source structure
 let sourceContent = ''
 if (wizardData.inboundAdapterType === 'SOAP' && wizardData.sourceFlowStructure) {
 sourceContent = wizardData.sourceFlowStructure;
 } else if (wizardData.sourceMessageStructure) {
 sourceContent = wizardData.sourceMessageStructure;
 }

 // Convert structures to XML
 const [sourceXmlResult, targetXmlResult] = await Promise.all([
 convertStructureToXml(sourceContent, {
 rootElementName: 'SourceMessage',
 includeXmlDeclaration: true,
 prettyPrint: true,
 convertPropertyNames: true
 }),
 convertStructureToXml(target.structure || '', {
 rootElementName: 'TargetMessage',
 includeXmlDeclaration: true,
 prettyPrint: true,
 convertPropertyNames: true
 })
 ]);

 setWizardData({
 ...wizardData,
 sourceXml: sourceXmlResult.xmlContent,
 targetXml: targetXmlResult.xmlContent
 });

 setShowFullScreenMapping(true);
  } catch (error) {
 logger.error(LogCategory.UI, 'Error converting structures', { error: error });
 toast({
 title: 'Conversion Failed',
 description: 'Failed to convert structures for field mapping',
 variant: 'destructive'
 });
 } finally {
 setConvertingStructures(false);
 }
 };

 return (
 <div className="space-y-6">
 <div className="bg-indigo-50 dark:bg-indigo-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Field Mappings Configuration</h4>
 <p className="text-sm text-muted-foreground">
 Configure field mappings for each target adapter
 </p>
 <div className="mt-3 flex items-center gap-4 text-sm">
 <div className="flex items-center gap-2">
 <div className="w-2 h-2 rounded-full bg-green-500"></div>
 <span>Configured</span>
 </div>
 <div className="flex items-center gap-2">
 <div className="w-2 h-2 rounded-full bg-yellow-500"></div>
 <span>Pending</span>
 </div>
 </div>
 </div>

 <div className="space-y-4">
 {wizardData.orchestrationTargets?.map((target, index) => (
 <Card key={index}>
 <CardHeader>
 <CardTitle className="text-base flex items-center justify-between">
 <span>{target.adapterName}</span>
 <div className={`w-2 h-2 rounded-full ${
 target.mapping && target.mapping.length > 0 ? 'bg-green-500' : 'bg-yellow-500'
 }`} />
 </CardTitle>
 <CardDescription>
 Field mapping from source to {target.adapterType}
 </CardDescription>
 </CardHeader>
 <CardContent>
 {target.mapping && target.mapping.length > 0 ? (
 <div className="space-y-2">
 <p className="text-sm text-muted-foreground">
 {target.mapping.length} mapping(s) configured
 </p>
 <Button
 variant="outline"
 size="sm"
 onClick={() => handlePrepareOrchestrationMapping(index)}
 disabled={convertingStructures}
 >
 Edit Mappings
 </Button>
 </div>
 ) : (
 <Button
 size="sm"
 onClick={() => handlePrepareOrchestrationMapping(index)}
 disabled={convertingStructures}
 >
 {convertingStructures ? (
 <>
 <Spinner size="sm" className="mr-2" />
 Preparing...
 </>
 ) : (
 'Configure Mappings'
 )}
 </Button>
 )}
 </CardContent>
 </Card>
 ))}
 </div>

 {/* Store selected target index for mapping save */}
 {showFullScreenMapping && selectedTargetIndex !== null && (
 <script dangerouslySetInnerHTML={{ __html: 'window.__selectedTargetIndex = ' + selectedTargetIndex }} />
 )}
 </div>
 );
 }

 case 'integration-flow':
 return (
 <div className="space-y-6">
 <div className="bg-purple-50 dark:bg-purple-950/20 p-4 rounded-lg">
 <h4 className="font-medium mb-2">Integration Flow</h4>
 <p className="text-sm text-muted-foreground">
 Define the integration flow that orchestrates the adapters and transformations
 </p>
 </div>

 <div className="space-y-4">
 <div>
 <Label htmlFor="flowName">Flow Name *</Label>
 <Input
 id="flowName"
 value={wizardData.flowName}
 onChange={(e) => setWizardData({ ...wizardData, flowName: e.target.value })}
 placeholder="Enter flow name"
 className="mt-1"
 />
 </div>

 <div>
 <Label htmlFor="flowDescription">Flow Description</Label>
 <Textarea
 id="flowDescription"
 value={wizardData.flowDescription}
 onChange={(e) => setWizardData({ ...wizardData, flowDescription: e.target.value })}
 placeholder="Describe what this flow does..."
 className="mt-1"
 rows={4}
 />
 </div>

 <Card className="p-4">
 <div className="text-sm space-y-2">
 <p><strong>Flow Type:</strong> {wizardData.flowType === 'ORCHESTRATION' ? 'Orchestration' : 'Direct Integration'}</p>
 <p><strong>Source Adapter:</strong> {wizardData.inboundAdapterName} ({wizardData.inboundAdapterType})</p>
 {wizardData.flowType === 'DIRECT_INTEGRATION' ? (
 <p><strong>Target Adapter:</strong> {wizardData.outboundAdapterName} ({wizardData.outboundAdapterType})</p>
 ) : (
 <div>
 <p><strong>Target Adapters:</strong></p>
 <ul className="ml-4 list-disc">
 {wizardData.orchestrationTargets?.map((target, idx) => (
 <li key={idx}>{target.adapterName} ({target.adapterType})</li>
 ))}
 </ul>
 </div>
 )}
 <p><strong>Transformation:</strong> {wizardData.transformationRequired ? 'Enabled' : 'Disabled'}</p>
 <p><strong>Type:</strong> {wizardData.syncType}</p>
 </div>
 </Card>
 </div>
 </div>
 );

 case 'review':
 return (
 <div className="space-y-6">
 <div className="bg-gradient-to-r from-blue-50 to-purple-50 dark:from-blue-950/20 dark:to-purple-950/20 p-6 rounded-lg">
 <h3 className="text-lg font-semibold mb-2">Ready to Create Package</h3>
 <p className="text-sm text-muted-foreground">
 Review your configuration below. Click "Create Package" to proceed.
 </p>
 </div>

 <div className="grid gap-4 md:grid-cols-2">
 <Card>
 <CardHeader className="pb-3">
 <CardTitle className="text-base flex items-center gap-2">
 <Package className="w-4 h-4" />
 Package Details
 </CardTitle>
 </CardHeader>
 <CardContent className="space-y-2">
 <div className="space-y-2 text-sm">
 <div className="flex justify-between">
 <span className="text-muted-foreground">Name:</span>
 <span className="font-medium">{wizardData.packageName}</span>
 </div>
 <div className="flex justify-between">
 <span className="text-muted-foreground">Description:</span>
 <span className="font-medium text-right max-w-[200px]">{wizardData.packageDescription || 'None'}</span>
 </div>
 <div className="flex justify-between">
 <span className="text-muted-foreground">Flow Type:</span>
 <Badge variant={wizardData.flowType === 'ORCHESTRATION' ? 'default' : 'secondary'}>
 {wizardData.flowType === 'ORCHESTRATION' ? 'Orchestration' : 'Direct Integration'}
 </Badge>
 </div>
 <div className="flex justify-between">
 <span className="text-muted-foreground">Sync Type:</span>
 <span className="font-medium">{wizardData.syncType}</span>
 </div>
 <div className="flex justify-between">
 <span className="text-muted-foreground">Transformation:</span>
 <Badge variant={wizardData.transformationRequired ? 'default' : 'outline'}>
 {wizardData.transformationRequired ? 'Enabled' : 'Disabled'}
 </Badge>
 </div>
 </div>
 </CardContent>
 </Card>

 <Card>
 <CardHeader className="pb-3">
 <CardTitle className="text-base flex items-center gap-2">
 <Server className="w-4 h-4" />
 Adapters Configuration
 </CardTitle>
 </CardHeader>
 <CardContent>
 <div className="space-y-3">
 <div className="flex items-center gap-2">
 <Badge variant="outline"><Globe className="w-3 h-3 mr-1" />Source</Badge>
 <span className="text-sm">{wizardData.inboundAdapterName} ({wizardData.inboundAdapterType})</span>
 </div>
 {wizardData.flowType === 'DIRECT_INTEGRATION' ? (
 <div className="flex items-center gap-2">
 <Badge variant="outline"><Server className="w-3 h-3 mr-1" />Target</Badge>
 <span className="text-sm">{wizardData.outboundAdapterName} ({wizardData.outboundAdapterType})</span>
 </div>
 ) : (
 wizardData.orchestrationTargets?.map((target, index) => (
 <div key={index} className="flex items-center gap-2">
 <Badge variant="outline"><Server className="w-3 h-3 mr-1" />Target {index + 1}</Badge>
 <span className="text-sm">{target.adapterName} ({target.adapterType})</span>
 </div>
 ))
 )}
 </div>
 </CardContent>
 </Card>
 </div>

 <Card>
 <CardHeader className="pb-3">
 <CardTitle className="text-base flex items-center gap-2">
 <FileCode className="w-4 h-4" />
 Configuration Status
 </CardTitle>
 </CardHeader>
 <CardContent>
 <div className="grid gap-4 md:grid-cols-2">
 <div>
 <h4 className="font-medium mb-2 text-sm">Structures</h4>
 <div className="space-y-2 text-sm">
 {wizardData.inboundAdapterType === 'SOAP' && (
 <div className="flex items-center gap-2">
 <Check className="w-4 h-4 text-green-600" />
 <span>Source Flow Structure (WSDL)</span>
 </div>
 )}
 {wizardData.transformationRequired && wizardData.inboundAdapterType !== 'SOAP' && (
 <div className="flex items-center gap-2">
 <Check className="w-4 h-4 text-green-600" />
 <span>Source Message Structure</span>
 </div>
 )}
 {wizardData.flowType === 'DIRECT_INTEGRATION' && (
 <>
 {wizardData.outboundAdapterType === 'SOAP' && (
 <div className="flex items-center gap-2">
 <Check className="w-4 h-4 text-green-600" />
 <span>Target Flow Structure (WSDL)</span>
 </div>
 )}
 {wizardData.transformationRequired && wizardData.outboundAdapterType !== 'SOAP' && (
 <div className="flex items-center gap-2">
 <Check className="w-4 h-4 text-green-600" />
 <span>Target Message Structure</span>
 </div>
 )}
 </>
 )}
 {wizardData.syncType === 'SYNCHRONOUS' && wizardData.transformationRequired && (
 <div className="flex items-center gap-2">
 <Check className="w-4 h-4 text-green-600" />
 <span>Response Structure</span>
 </div>
 )}
 </div>
 </div>

 <div>
 <h4 className="font-medium mb-2 text-sm">Mappings & Flow</h4>
 <div className="space-y-2 text-sm">
 {wizardData.flowType === 'DIRECT_INTEGRATION' && wizardData.transformationRequired && (
 <div className="flex items-center gap-2">
 <Check className="w-4 h-4 text-green-600" />
 <span>Field Mappings Configured</span>
 </div>
 )}
 {wizardData.flowType === 'ORCHESTRATION' && (
 wizardData.orchestrationTargets?.map((target, idx) => (
 <div key={idx} className="flex items-center gap-2">
 {target.mapping && target.mapping.length > 0 ? (
 <Check className="w-4 h-4 text-green-600" />
 ) : (
 <Clock className="w-4 h-4 text-yellow-600" />
 )}
 <span>Target {idx + 1} Mappings</span>
 </div>
 ))
 )}
 <div className="flex items-center gap-2">
 <Check className="w-4 h-4 text-green-600" />
 <span>Integration Flow: {wizardData.flowName}</span>
 </div>
 </div>
 </div>
 </div>
 </CardContent>
 </Card>
 </div>
 );

 default:
 return null;
 }
 };

 const getStepIcon = (step: WizardStep) => {
 switch (step) {
 case 'package-info': return <Package className="w-4 h-4" />;
 case 'flow-type-selection': return <GitBranch className="w-4 h-4" />;
 case 'source-adapter': return <Webhook className="w-4 h-4" />;
 case 'source-flow-structure': return <FileCode className="w-4 h-4" />;
 case 'source-message-structure': return <MessageSquare className="w-4 h-4" />;
 case 'target-adapter': return <Send className="w-4 h-4" />;
 case 'target-flow-structure': return <FileCode className="w-4 h-4" />;
 case 'target-message-structure': return <MessageSquare className="w-4 h-4" />;
 case 'response-structure': return <Layers className="w-4 h-4" />;
 case 'field-mapping': return <Settings className="w-4 h-4" />;
 case 'orchestration-targets': return <Send className="w-4 h-4" />;
 case 'orchestration-structures': return <FileCode className="w-4 h-4" />;
 case 'orchestration-mappings': return <Settings className="w-4 h-4" />;
 case 'integration-flow': return <GitBranch className="w-4 h-4" />;
 case 'review': return <CheckCircle2 className="w-4 h-4" />;
 }
 };

 const getStepTitle = (step: WizardStep) => {
 switch (step) {
 case 'package-info': return 'Package Information';
 case 'flow-type-selection': return 'Flow Type';
 case 'source-adapter': return 'Source Adapter';
 case 'source-flow-structure': return 'Source Flow Structure';
 case 'source-message-structure': return 'Source Message Structure';
 case 'target-adapter': return 'Target Adapter';
 case 'target-flow-structure': return 'Target Flow Structure';
 case 'target-message-structure': return 'Target Message Structure';
 case 'response-structure': return 'Response Structure';
 case 'field-mapping': return 'Field Mapping';
 case 'orchestration-targets': return 'Target Adapters';
 case 'orchestration-structures': return 'Target Structures';
 case 'orchestration-mappings': return 'Target Mappings';
 case 'integration-flow': return 'Integration Flow';
 case 'review': return 'Review & Create';
 }
 };

 if (!isOpen) return null;

 const stepNumber = getStepNumber();
 // Show full-screen field mapping when needed
 if (showFullScreenMapping && wizardData.sourceXml && wizardData.targetXml) {
 return (
 <div className="fixed inset-0 bg-background z-50 overflow-hidden">
 <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
 <div className="w-full px-6 py-4">
 <div className="flex items-center gap-4">
 <Button
 variant="ghost"
 size="sm"
 onClick={() => setShowFullScreenMapping(false)}
 className="gap-2"
 >
 <ArrowLeft className="h-4 w-4" />
 Back to Package Creation
 </Button>
 <div>
 <h1 className="text-2xl font-bold tracking-tight">Field Mapping - {wizardData.packageName}</h1>
 <p className="text-muted-foreground">
 Map fields between {wizardData.inboundAdapterName} and {wizardData.outboundAdapterName}
 </p>
 </div>
 </div>
 </div>
 </div>

 <FieldMappingScreen
 onClose={() => setShowFullScreenMapping(false)}
 onSave={(mappings) => {
 if (wizardData.flowType === 'ORCHESTRATION' && selectedTargetIndex !== null) {
 // Save mappings to the selected orchestration target
 const targets = [...(wizardData.orchestrationTargets || [])];
 targets[selectedTargetIndex] = { ...targets[selectedTargetIndex], mapping: mappings };
 setWizardData({ ...wizardData, orchestrationTargets: targets });
 toast({
 title: "Mappings Saved",
 description: mappings.length + ' mappings configured for ' + targets[selectedTargetIndex].adapterName + '.',
 });
 } else {
 // Save mappings for direct integration
 setWizardData({ ...wizardData, fieldMappings: mappings });
 toast({
 title: "Mappings Saved",
 description: mappings.length + ' field mappings have been configured.',
 });
 }
 setShowFullScreenMapping(false);
 setSelectedTargetIndex(null);
 }}
 initialMappingName={(() => {
 if (wizardData.flowType === 'ORCHESTRATION' && selectedTargetIndex !== null) {
 const target = wizardData.orchestrationTargets?.[selectedTargetIndex];
 return wizardData.packageName + ' - ' + (target?.adapterName || 'Target') + ' Mapping';
 }
 return wizardData.packageName + ' Mapping';
 })()}
 initialMappings={(() => {
 if (wizardData.flowType === 'ORCHESTRATION' && selectedTargetIndex !== null) {
 return wizardData.orchestrationTargets?.[selectedTargetIndex]?.mapping || [];
 }
 return wizardData.fieldMappings || [];
 })()}
 inboundAdapterType={wizardData.inboundAdapterType}
 outboundAdapterType={(() => {
 if (wizardData.flowType === 'ORCHESTRATION' && selectedTargetIndex !== null) {
 return wizardData.orchestrationTargets?.[selectedTargetIndex]?.adapterType || '';
 }
 return wizardData.outboundAdapterType;
 })()}
 sourceXml={wizardData.sourceXml}
 targetXml={wizardData.targetXml}
 preConverted={true}
 />
 </div>
 );
 }

 return (
 <div className="fixed inset-0 bg-background z-50 overflow-hidden">
 {/* Header */}
 <div className="h-16 border-b flex items-center justify-between px-6">
 <div className="flex items-center gap-4">
 <h1 className="text-2xl font-semibold">Create Integration Package</h1>
 <Badge variant="outline">
 Step {stepNumber.current} of {stepNumber.total}
 </Badge>
 </div>
 <Button
 variant="ghost"
 size="icon"
 onClick={onClose}
 >
 <X className="w-4 h-4" />
 </Button>
 </div>

 {/* Main Content */}
 <div className="h-[calc(100vh-4rem-5rem)] overflow-y-auto">
 <div className="max-w-4xl mx-auto p-6">
 {/* Step indicator */}
 <div className="mb-8">
 <div className="flex items-center justify-between">
              {getStepSequence().map((step, index) => {
                const isActive = currentStep === step;
                const isCompleted = getStepSequence().indexOf(currentStep) > index;
                const stepClasses = isActive 
                  ? 'bg-primary text-primary-foreground' 
                  : isCompleted 
                  ? 'bg-primary/20 text-primary' 
                  : 'bg-muted text-muted-foreground';
                const textClasses = isActive ? 'text-primary' : 'text-muted-foreground';
                const lineClasses = isCompleted ? 'bg-primary/20' : 'bg-muted';
                
                return (
                  <div key={step} className="flex items-center flex-1">
                    <div className="flex items-center">
                      <div className={`flex items-center justify-center w-10 h-10 rounded-full ${stepClasses}`}>
                        {getStepIcon(step)}
                      </div>
                      <div className="ml-2">
                        <p className={`text-sm font-medium ${textClasses}`}>
                          {getStepTitle(step)}
                        </p>
                      </div>
                    </div>
                    {index < getStepSequence().length - 1 && (
                      <div className={`flex-1 h-0.5 mx-4 ${lineClasses}`} />
                    )}
                  </div>
                );
              })}
            </div>
          </div>

 {/* Step content */}
 <div key={currentStep} className="animate-in fade-in-0 slide-in-from-right-4 duration-300">
 {renderStepContent()}
 </div>
 </div>
 </div>

 <div className="h-20 border-t flex items-center justify-between px-6">
 <Button
 variant="outline"
 onClick={handleBack}
 disabled={currentStep === 'package-info'}
 >
 <ArrowLeft className="w-4 h-4 mr-2" />
 Back
 </Button>

 <div className="flex gap-2">
 <Button variant="outline" onClick={onClose}>
 Cancel
 </Button>

 {currentStep === 'review' ? (
 <Button onClick={handleCreatePackage} disabled={isSaving}>
 {isSaving ? (
 <>
 <Spinner size="sm" className="mr-2" />
 Creating...
 </>
 ) : (
 <>
 <CheckCircle2 className="w-4 h-4 mr-2" />
 Create Package
 </>
 )}
 </Button>
 ) : (
 <Button
 onClick={handleNext}
 disabled={
 (currentStep === 'package-info' && !wizardData.packageName) ||
 (currentStep === 'flow-type-selection' && !wizardData.flowType) ||
 (currentStep === 'source-adapter' && (!wizardData.inboundAdapterName || !wizardData.inboundAdapterType)) ||
 (currentStep === 'target-adapter' && (!wizardData.outboundAdapterName || !wizardData.outboundAdapterType)) ||
 (currentStep === 'orchestration-targets' && (!wizardData.orchestrationTargets || wizardData.orchestrationTargets.length === 0 ||
 wizardData.orchestrationTargets.some(t => !t.adapterName || !t.adapterType))) ||
 (currentStep === 'integration-flow' && !wizardData.flowName)
 }
 >
 Next
 <ArrowRight className="w-4 h-4 ml-2" />
 </Button>
 )}
 </div>
 </div>
 </div>
 );
}

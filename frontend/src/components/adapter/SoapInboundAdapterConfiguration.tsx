import { useEffect, useState, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { api } from '@/services/api';
import { extractWsdlSoapActions } from '@/utils/structureParsers';
import { logger, LogCategory } from '@/lib/logger';

interface SoapInboundAdapterConfigurationProps {
 configuration: any;
 onConfigurationChange: (field: string, value: any) => void;
 businessComponentId?: string;
}

export function SoapInboundAdapterConfiguration({
 configuration,
 onConfigurationChange,
 businessComponentId
}: SoapInboundAdapterConfigurationProps) {
 const [wsdls, setWsdls] = useState<any[]>([]);
 const [loadingWsdls, setLoadingWsdls] = useState(false);
 const [wsdlStructureDetails, setWsdlStructureDetails] = useState<{
 request?: any;
 response?: any;
 fault?: any;
 }>({});

 const [soapActions, setSoapActions] = useState<{ operationName: string, soapAction: string }[]>([]);
 const [loadingSoapActions, setLoadingSoapActions] = useState(false);

 // Set default processing mode to asynchronous
 useEffect(() => {
 if (!configuration.processingMode) {
 onConfigurationChange('processingMode', 'ASYNCHRONOUS');
 }
 }, [configuration.processingMode, onConfigurationChange]);

 // Fetch WSDLs when business component changes
 useEffect(() => {
 if (businessComponentId) {
 fetchWsdls();
 }
 }, [businessComponentId, fetchWsdls]);

 // Check WSDL structure and set processing mode
 useEffect(() => {
 if (configuration.selectedWsdl) {
 checkWsdlStructure(configuration.selectedWsdl);
 } else {
 // Clear SOAP actions when no WSDL is selected
 setSoapActions([]);
 setWsdlStructureDetails({});
 }
 }, [configuration.selectedWsdl, checkWsdlStructure]);

 const fetchWsdls = useCallback(async () => {
        try {
            setLoadingWsdls(true);
            // Fetch WSDLs as DataStructures filtered by type, usage and business component
            const response = await api.get('/structures', {
                params: {
                    type: 'wsdl',
                    usage: 'source',
                    businessComponentId: businessComponentId,
                    limit: 100
                }
            } as any);

            if (response.data && response.data.structures) {
                logger.info(LogCategory.UI, 'SOAP Inbound - Raw structures from API', { data: response.data.structures });
                // Transform the structures to match our expected format
                // Filter to ensure we only get WSDL type structures with source usage
                const wsdlList = response.data.structures
                    .filter((structure: any) => {
                        // Must be WSDL type
                        const isWsdl = structure.type?.toLowerCase() === 'wsdl';
                        // Check usage - source for sender adapter
                        const isSource = structure.usage === 'source' ||
                            structure.metadata?.usage === 'source' ||
                            !structure.usage; // Include if usage is not set

                        const included = isWsdl && isSource;
                        logger.info(LogCategory.UI, `SOAP Inbound - Structure ${structure.name}: type=${structure.type}, usage=${structure.usage}, metadata.usage=${structure.metadata?.usage}, included=${included}`);
                        return included;
                    })
                    .map((structure: any) => ({
                        id: structure.id,
                        name: structure.name,
                        endpointUrl: structure.metadata?.endpointUrl || ''
                    }));

                logger.info(LogCategory.UI, 'SOAP Inbound - Filtered WSDL list', { data: wsdlList });
                setWsdls(wsdlList);
            } else {
                setWsdls([]);
            }
        } catch (error) {
            logger.error(LogCategory.UI, 'Error fetching WSDLs', { error: error });
            setWsdls([]);
        } finally {
            setLoadingWsdls(false);
        }
    }, [businessComponentId]);

    const checkWsdlStructure = useCallback(async (wsdlId: string) => {
        try {
            setLoadingSoapActions(true);
            // Get the WSDL structure details
            const response = await api.get(`/structures/${wsdlId}`);
            if (response.data) {
                const wsdlStructure = response.data;
 logger.info(LogCategory.UI, 'SOAP Inbound - WSDL Structure', { data: wsdlStructure });
 logger.info(LogCategory.UI, 'SOAP Inbound - WSDL Metadata', { data: wsdlStructure.metadata });
 // Extract SOAP actions from the original WSDL content
 if (wsdlStructure.originalContent) {
 const extractedActions = extractWsdlSoapActions(wsdlStructure.originalContent);
 setSoapActions(extractedActions);
 logger.info(LogCategory.UI, 'SOAP Inbound - Extracted SOAP actions', { data: extractedActions });
 // If there's only one action, auto-select it
 if (extractedActions.length === 1) {
 onConfigurationChange('soapAction', extractedActions[0].soapAction);
 }

 // Check sync/async based on the selected operation's pattern
 // When user selects a specific SOAP action, we'll check that operation's input/output
 // For now, if ANY operation in the WSDL has both input and output, consider it potentially synchronous
 try {
 const parser = new DOMParser();
                    const doc = parser.parseFromString(wsdlStructure.originalContent, 'text/xml');
 // Find all operations in portType
 const portTypes = doc.querySelectorAll('portType, wsdl\\:portType');
                    let hasSynchronousOperations = false;
 portTypes.forEach((portType) => {
 const operations = portType.querySelectorAll('operation, wsdl\\:operation');
 operations.forEach((op) => {
 const hasInput = !!op.querySelector('input, wsdl\\:input');
 const hasOutput = !!op.querySelector('output, wsdl\\:output');
 if (hasInput && hasOutput) {
 hasSynchronousOperations = true;
                            }
                        });
                    });

 // If WSDL has synchronous operations, default to synchronous mode
 // User can still change it if needed
 if (hasSynchronousOperations) {
                        logger.info(LogCategory.UI, 'SOAP Inbound - WSDL has synchronous operations (request/response), setting to SYNCHRONOUS');
 onConfigurationChange('processingMode', 'SYNCHRONOUS');
 } else {
 logger.info(LogCategory.UI, 'SOAP Inbound - WSDL appears to have one-way operations only, setting to ASYNCHRONOUS');
                        onConfigurationChange('processingMode', 'ASYNCHRONOUS');
                    }
        } catch (e) {
 logger.error(LogCategory.UI, 'Error parsing WSDL for sync/async detection', { error: e });
 // Default to synchronous for SOAP as most SOAP services are request/response
 onConfigurationChange('processingMode', 'SYNCHRONOUS');
 }
 } else {
 setSoapActions([]);
 logger.warn(LogCategory.UI, 'SOAP Inbound - No originalContent in WSDL structure');
 // Extract structure details from metadata
 const structureDetails: any = {};

 // Extract request structure
 if (wsdlStructure.metadata?.requestStructure || wsdlStructure.metadata?.operationInfo?.request) {
                        structureDetails.request = wsdlStructure.metadata?.requestStructure || wsdlStructure.metadata?.operationInfo?.request;
                    }

 // Extract response structure
 if (wsdlStructure.metadata?.responseStructure || wsdlStructure.metadata?.operationInfo?.response) {
                        structureDetails.response = wsdlStructure.metadata?.responseStructure || wsdlStructure.metadata?.operationInfo?.response;
                    }

 // Extract fault structure
 if (wsdlStructure.metadata?.faultStructure || wsdlStructure.metadata?.operationInfo?.fault) {
                        structureDetails.fault = wsdlStructure.metadata?.faultStructure || wsdlStructure.metadata?.operationInfo?.fault;
                    }

 setWsdlStructureDetails(structureDetails);
 logger.info(LogCategory.UI, 'SOAP Inbound - Extracted structures', { data: structureDetails });
 // Check if metadata contains sync/async information
 const hasInput = wsdlStructure.metadata?.hasInput || wsdlStructure.metadata?.operationInfo?.hasInput;
 const hasOutput = wsdlStructure.metadata?.hasOutput || wsdlStructure.metadata?.operationInfo?.hasOutput;
                    const hasFault = wsdlStructure.metadata?.hasFault || wsdlStructure.metadata?.operationInfo?.hasFault;
                    
                    logger.info(LogCategory.UI, `SOAP Inbound - WSDL Analysis from metadata: hasInput=${hasInput}, hasOutput=${hasOutput}, hasFault=${hasFault}`);
                    // Note: We've already determined sync/async based on WSDL parsing above
                    // This metadata check is just for logging purposes - don't override the earlier decision
                }
            }
        } catch (error) {
            logger.error(LogCategory.UI, 'Error checking WSDL structure', { error: error });
            // Default to async on error
            onConfigurationChange('processingMode', 'ASYNCHRONOUS');
            setWsdlStructureDetails({});
            setSoapActions([]);
        } finally {
            setLoadingSoapActions(false);
        }
    }, [onConfigurationChange]);

 return (
 <Card>
 <CardHeader>
 <CardTitle>SOAP Inbound Configuration</CardTitle>
 <CardDescription>Configure your SOAP sender adapter settings</CardDescription>
 </CardHeader>
 <CardContent>
 <Tabs defaultValue="source" className="w-full">
 <TabsList className="grid w-full grid-cols-2">
 <TabsTrigger value="source">Source</TabsTrigger>
 <TabsTrigger value="processing">Processing</TabsTrigger>
 </TabsList>

 <TabsContent value="source" className="space-y-6">
 {/* Connection Mode Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Connection Mode</h3>
 <div className="flex items-center justify-between space-x-4 p-4 border rounded-lg">
 <div className="space-y-1">
 <Label htmlFor="connectionMode" className="text-sm font-medium">
 {(configuration.connectionMode || 'PUSH') === 'PUSH'
 ? 'Push Mode (Inbound)'
 : 'Poll Mode (Outbound)'
 }
 </Label>
 <p className="text-sm text-muted-foreground">
 {(configuration.connectionMode || 'PUSH') === 'PUSH'
 ? 'Receive SOAP requests at the configured endpoint'
 : 'Poll and send requests to external SOAP service'
 }
 </p>
 </div>
 <Switch
 id="connectionMode"
 checked={(configuration.connectionMode || 'PUSH') === 'PUSH'}
                    onCheckedChange={(checked) =>
                        onConfigurationChange('connectionMode', checked ? 'PUSH' : 'POLL')
                    }
 />
 </div>
 </div>

 {/* Endpoint Information Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Endpoint Information</h3>
 <div className="grid grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="service-endpoint">
 {(configuration.connectionMode || 'PUSH') === 'PUSH'
 ? 'Service Endpoint Path'
 : 'External Service URL'
 }
 </Label>
 <Input
 id="service-endpoint"
 value={configuration.serviceEndpointUrl || ''}
 onChange={(e) => onConfigurationChange('serviceEndpointUrl', e.target.value)}
 placeholder={(configuration.connectionMode || 'PUSH') === 'PUSH'
 ? "/tempconvert or /soap/tempconvert"
 : "https://api.external.com/soap/service"
 }
 />
 {(configuration.connectionMode || 'PUSH') === 'PUSH' && (
 <p className="text-xs text-muted-foreground">
 This path will be used as the service endpoint (e.g., http://localhost:8080/soap/tempconvert)
 </p>
 )}
 </div>
 <div className="space-y-2">
 <Label htmlFor="soap-action">SOAP Action</Label>
 {soapActions.length > 0 ? (
 <>
 <Select
 value={configuration.soapAction || 'none'}
 onValueChange={(value) => onConfigurationChange('soapAction', value === 'none' ? '' : value)}
 >
 <SelectTrigger>
 <SelectValue placeholder={loadingSoapActions ? "Loading SOAP actions..." : "Select SOAP action"} />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="none">None (Leave blank)</SelectItem>
                                {soapActions.map((action, index) => (
 <SelectItem key={index} value={action.soapAction || `action_${index}`}>
 {action.operationName} - {action.soapAction || '(empty)'}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 {soapActions.length > 1 && (
 <p className="text-xs text-muted-foreground">
 This WSDL contains {soapActions.length} operations. Select the operation you want to use.
 </p>
 )}
 </>
 ) : (
 <Input
 id="soap-action"
 value={configuration.soapAction || ''}
 onChange={(e) => onConfigurationChange('soapAction', e.target.value)}
 placeholder="urn:getOrderDetails"
 disabled={loadingSoapActions}
 />
 )}
 </div>
 <div className="space-y-2">
 <Label htmlFor="content-type">Content-Type</Label>
 <Select
 value={configuration.contentType || ''}
 onValueChange={(value) => onConfigurationChange('contentType', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select content type" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="text/xml">text/xml</SelectItem>
 <SelectItem value="application/soap+xml">application/soap+xml</SelectItem>
 </SelectContent>
 </Select>
 </div>
 <div className="space-y-2">
 <Label htmlFor="soap-version">SOAP Version</Label>
 <Select
 value={configuration.soapVersion || ''}
 onValueChange={(value) => {
 onConfigurationChange('soapVersion', value);
 // Automatically set content type based on SOAP version
 if (value === '1.1') {
 onConfigurationChange('contentType', 'text/xml');
 } else if (value === '1.2') {
 onConfigurationChange('contentType', 'application/soap+xml');
 }
 }}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select SOAP version" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="1.1">SOAP 1.1</SelectItem>
 <SelectItem value="1.2">SOAP 1.2</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </div>
 </div>

 {/* Source Message Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Source Message</h3>
 <div className="space-y-2">
 <Label htmlFor="source-wsdl">Select WSDL</Label>
 <Select
 value={configuration.selectedWsdl || ''}
 onValueChange={(value) => onConfigurationChange('selectedWsdl', value)}
 disabled={loadingWsdls || !businessComponentId}
 >
 <SelectTrigger>
 <SelectValue placeholder={
 !businessComponentId
 ? "Select a business component first"
 : loadingWsdls
 ? "Loading WSDLs..."
 : "Select a WSDL"
 } />
 </SelectTrigger>
 <SelectContent>
 {wsdls.map((wsdl) => (
 <SelectItem key={wsdl.id} value={wsdl.id}>
 {wsdl.name}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </div>
 </div>
 </TabsContent>

 <TabsContent value="processing" className="space-y-6">
 {/* Processing Mode Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Processing Mode</h3>

 <div className="flex items-center justify-between space-x-4 p-4 border rounded-lg">
 <div className="space-y-1">
 <Label htmlFor="processingMode" className="text-sm font-medium">
 {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS'
 ? 'Asynchronous Processing'
 : 'Synchronous Processing'
 }
 </Label>
 <p className="text-sm text-muted-foreground">
 {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS'
 ? 'Return immediate acknowledgment (HTTP 202), process in background'
 : 'Wait for complete processing before sending response back to caller'
 }
 </p>
 </div>
 <Switch
 id="processingMode"
 checked={(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS'}
                    onCheckedChange={(checked) =>
                        onConfigurationChange('processingMode', checked ? 'ASYNCHRONOUS' : 'SYNCHRONOUS')
                    }
 />
 </div>

 {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS' && (
 <div className="space-y-4 pl-4 border-l-2 border-muted">
 <div className="space-y-2">
 <Label htmlFor="asyncResponseTimeout">Async Response Timeout (ms)</Label>
 <Input
 id="asyncResponseTimeout"
 type="number"
 placeholder="30000"
 value={configuration.asyncResponseTimeout || ''}
 onChange={(e) => onConfigurationChange('asyncResponseTimeout', parseInt(e.target.value) || 30000)}
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="asyncResponseFormat">Async Response Format</Label>
 <Select
 value={configuration.asyncResponseFormat || 'HTTP_202'}
 onValueChange={(value) => onConfigurationChange('asyncResponseFormat', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select response format" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="HTTP_202">HTTP 202 Accepted</SelectItem>
 <SelectItem value="CUSTOM_RESPONSE">Custom Response</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="asyncCallbackUrl">Callback URL (Optional)</Label>
 <Input
 id="asyncCallbackUrl"
 type="url"
 placeholder="https://callback-endpoint.example.com/webhook"
 value={configuration.asyncCallbackUrl || ''}
 onChange={(e) => onConfigurationChange('asyncCallbackUrl', e.target.value)}
 />
 </div>
 </div>
 )}
 </div>

 {/* Response Format and Error Handling Section - Only show in ASYNC mode */}
 {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS' && (
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Response Format and Error Handling</h3>
 <div className="grid grid-cols-1 gap-4">
 <div className="space-y-2">
 <Label htmlFor="response-message">Response Message</Label>
 <Textarea
 id="response-message"
 value={configuration.responseMessage || ''}
 onChange={(e) => onConfigurationChange('responseMessage', e.target.value)}
 placeholder="Expected SOAP response XML schema"
 rows={4}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="error-handling">Error Handling</Label>
 <Textarea
 id="error-handling"
 value={configuration.errorHandling || ''}
 onChange={(e) => onConfigurationChange('errorHandling', e.target.value)}
 placeholder="Expected SOAP fault codes and error handling"
 rows={3}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="timeout">Timeout Settings (seconds)</Label>
 <Input
 id="timeout"
 type="number"
 value={configuration.timeout || ''}
 onChange={(e) => onConfigurationChange('timeout', e.target.value)}
 placeholder="60"
 />
 </div>
 </div>
 </div>
 )}

 {/* Custom Headers Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Custom Headers</h3>
 <div className="space-y-2">
 <Label htmlFor="custom-headers">Custom SOAP Headers</Label>
 <Textarea
 id="custom-headers"
 value={configuration.customHeaders || ''}
 onChange={(e) => onConfigurationChange('customHeaders', e.target.value)}
 placeholder="Additional SOAP headers in XML format (e.g., WS-Addressing, Custom security headers)"
 rows={4}
 />
 <p className="text-sm text-muted-foreground">
 Enter custom SOAP headers that will be included in the request envelope
 </p>
 </div>
 </div>

 {/* Additional Settings Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Additional Settings</h3>
 <div className="grid grid-cols-1 gap-4">
 <div className="space-y-2">
 <Label htmlFor="retry-count">Retry Count</Label>
 <Input
 id="retry-count"
 type="number"
 value={configuration.retryCount || ''}
 onChange={(e) => onConfigurationChange('retryCount', e.target.value)}
 placeholder="3"
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="retry-interval">Retry Interval (seconds)</Label>
 <Input
 id="retry-interval"
 type="number"
 value={configuration.retryInterval || ''}
 onChange={(e) => onConfigurationChange('retryInterval', e.target.value)}
 placeholder="5"
 />
 </div>
 </div>
 </div>

 {/* WSDL Structure Details Section */}
 {configuration.selectedWsdl && (
 <div className="space-y-4">
 <h3 className="text-lg font-medium">WSDL Structure Details</h3>

 {/* Request Structure - Always shown */}
 {wsdlStructureDetails.request && (
 <div className="space-y-2">
 <Label>Request Structure</Label>
 <div className="bg-muted p-4 rounded-md">
 <pre className="text-sm overflow-auto max-h-40">
 {typeof wsdlStructureDetails.request === 'string'
 ? wsdlStructureDetails.request
 : JSON.stringify(wsdlStructureDetails.request, null, 2)}
 </pre>
 </div>
 </div>
 )}

 {/* Response Structure - Only in SYNCHRONOUS mode */}
 {(configuration.processingMode || 'ASYNCHRONOUS') === 'SYNCHRONOUS' && wsdlStructureDetails.response && (
 <div className="space-y-2">
 <Label>Response Structure</Label>
 <div className="bg-muted p-4 rounded-md">
 <pre className="text-sm overflow-auto max-h-40">
 {typeof wsdlStructureDetails.response === 'string'
 ? wsdlStructureDetails.response
 : JSON.stringify(wsdlStructureDetails.response, null, 2)}
 </pre>
 </div>
 </div>
 )}

 {/* Fault Structure - Always shown */}
 {wsdlStructureDetails.fault && (
 <div className="space-y-2">
 <Label>Fault Structure</Label>
 <div className="bg-muted p-4 rounded-md">
 <pre className="text-sm overflow-auto max-h-40">
 {typeof wsdlStructureDetails.fault === 'string'
 ? wsdlStructureDetails.fault
 : JSON.stringify(wsdlStructureDetails.fault, null, 2)}
 </pre>
 </div>
 </div>
 )}

 {/* If no structures found */}
 {!wsdlStructureDetails.request && !wsdlStructureDetails.response && !wsdlStructureDetails.fault && (
 <p className="text-sm text-muted-foreground">No structure details available in WSDL metadata</p>
 )}
 </div>
 )}
 </TabsContent>
 </Tabs>
 </CardContent>
 </Card>
 );
};
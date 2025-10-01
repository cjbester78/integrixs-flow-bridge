import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';
import { useDataStructures } from '@/hooks/useDataStructures';
import { XmlConversionTab } from '../adapters/XmlConversionTab';
import { TransformationToggle, TransformationConfig } from '../adapters/TransformationToggle';
import { PayloadStructureDefinition, PayloadStructureConfig } from '../adapters/PayloadStructureDefinition';

interface HttpInboundAdapterConfigurationProps {
 configuration: any;
 onConfigurationChange: (field: string, value: string | number | TransformationConfig | PayloadStructureConfig | any) => void
}

export function HttpInboundAdapterConfiguration({
 configuration,
 onConfigurationChange
}: HttpInboundAdapterConfigurationProps) {
 const { businessComponents, loading } = useBusinessComponentAdapters();
 const { structures } = useDataStructures();
 const [selectedBusinessComponentId, setSelectedBusinessComponentId] = useState<string>('');
 const [sourceStructures, setSourceStructures] = useState<any[]>([]);

 // Initialize transformation and payload configs
 const [transformationConfig, setTransformationConfig] = useState<TransformationConfig>(
 configuration.transformationConfig || { mode: 'transform', requiresStructure: true }
 );
 const [payloadConfig, setPayloadConfig] = useState<PayloadStructureConfig>(
 configuration.payloadStructureConfig || { format: 'JSON' }
 );

 // Set default processing mode to asynchronous
 useEffect(() => {
 if (!configuration.processingMode) {
 onConfigurationChange('processingMode', 'ASYNCHRONOUS');
 }
 }, [configuration.processingMode, onConfigurationChange]);

 // Filter structures based on selected business component
 useEffect(() => {
 if (selectedBusinessComponentId) {
 const filteredStructures = structures.filter(
 structure => structure.usage === 'source'
 );
 setSourceStructures(filteredStructures);
 } else {
 setSourceStructures([]);
 }
 }, [selectedBusinessComponentId, structures]);

 // Handle transformation config changes
 const handleTransformationChange = (config: TransformationConfig) => {
 setTransformationConfig(config);
 onConfigurationChange('transformationConfig', config);
 };

 // Handle payload config changes
 const handlePayloadChange = (config: PayloadStructureConfig) => {
 setPayloadConfig(config);
 onConfigurationChange('payloadStructureConfig', config);
 };

 return (
 <Card>
 <CardHeader>
 <CardTitle>HTTP Inbound Configuration</CardTitle>
 <CardDescription>Configure your HTTP sender adapter settings</CardDescription>
 </CardHeader>
 <CardContent>
 <Tabs defaultValue="source" className="w-full">
 <TabsList className="grid w-full grid-cols-5">
 <TabsTrigger value="source">Source</TabsTrigger>
 <TabsTrigger value="processing">Processing</TabsTrigger>
 <TabsTrigger value="transformation">Transformation</TabsTrigger>
 <TabsTrigger value="payload" disabled={transformationConfig.mode !== 'transform'}>
 Payload Structure
 </TabsTrigger>
 <TabsTrigger value="xmlConversion" disabled={transformationConfig.mode !== 'transform'}>
 XML Conversion
 </TabsTrigger>
 </TabsList>

 <TabsContent value="source" className="space-y-6">
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Connection Details</h3>

 <div className="space-y-2">
 <Label htmlFor="endpointUrl">Endpoint URL</Label>
 <Input
 id="endpointUrl"
 type="text"
 placeholder="The URL where the 3rd party sends requests"
 value={configuration.endpointUrl || ''}
 onChange={(e) => onConfigurationChange('endpointUrl', e.target.value)}
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="httpMethod">HTTP Method</Label>
 <Select
 value={configuration.httpMethod || ''}
 onValueChange={(value) => onConfigurationChange('httpMethod', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select HTTP method" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="GET">GET</SelectItem>
 <SelectItem value="POST">POST</SelectItem>
 <SelectItem value="PUT">PUT</SelectItem>
 <SelectItem value="DELETE">DELETE</SelectItem>
 <SelectItem value="PATCH">PATCH</SelectItem>
 <SelectItem value="HEAD">HEAD</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="contentType">Content-Type</Label>
 <Select
 value={configuration.contentType || ''}
 onValueChange={(value) => onConfigurationChange('contentType', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select media type" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="application/json">application/json</SelectItem>
 <SelectItem value="application/xml">application/xml</SelectItem>
 <SelectItem value="text/xml">text/xml</SelectItem>
 <SelectItem value="application/x-www-form-urlencoded">application/x-www-form-urlencoded</SelectItem>
 <SelectItem value="text/plain">text/plain</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="sslConfig">SSL/TLS Configuration</Label>
 <Textarea
 id="sslConfig"
 placeholder="SSL certificates/truststore info for HTTPS"
 value={configuration.sslConfig || ''}
 onChange={(e) => onConfigurationChange('sslConfig', e.target.value)}
 className="min-h-[80px]"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="apiVersion">API Version</Label>
 <Input
 id="apiVersion"
 type="text"
 placeholder="Version of the API being called"
 value={configuration.apiVersion || ''}
 onChange={(e) => onConfigurationChange('apiVersion', e.target.value)}
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="rateLimits">Throttling/Rate Limits</Label>
 <Input
 id="rateLimits"
 type="text"
 placeholder="Limits on calls per time period"
 value={configuration.rateLimits || ''}
 onChange={(e) => onConfigurationChange('rateLimits', e.target.value)}
 />
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

 {/* Data Handling Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Data Handling</h3>

 <div className="space-y-2">
 <Label htmlFor="businessComponent">Business Component</Label>
 <Select
 value={selectedBusinessComponentId}
 onValueChange={(value) => {
 setSelectedBusinessComponentId(value);
 onConfigurationChange('businessComponentId', value);
 }}
 disabled={loading}
 >
 <SelectTrigger className="bg-background">
 <SelectValue placeholder="Select a business component" />
 </SelectTrigger>
 <SelectContent className="bg-background border z-50">
 {businessComponents.map((businessComponent) => (
 <SelectItem key={businessComponent.id} value={businessComponent.id}>
 {businessComponent.name}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="requestPayloadFormat">Request Payload Format</Label>
 <Select
 value={configuration.requestPayloadFormat || ''}
 onValueChange={(value) => onConfigurationChange('requestPayloadFormat', value)}
 disabled={!selectedBusinessComponentId || sourceStructures.length === 0}
 >
 <SelectTrigger className="bg-background">
 <SelectValue placeholder="Select request structure" />
 </SelectTrigger>
 <SelectContent className="bg-background border z-50">
 {sourceStructures.map((structure) => (
 <SelectItem key={structure.id} value={structure.id}>
 {structure.name} ({structure.type})
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="responseFormat">Response Format</Label>
 <Select
 value={configuration.responseFormat || ''}
 onValueChange={(value) => onConfigurationChange('responseFormat', value)}
 disabled={!selectedBusinessComponentId || sourceStructures.length === 0}
 >
 <SelectTrigger className="bg-background">
 <SelectValue placeholder="Select response structure" />
 </SelectTrigger>
 <SelectContent className="bg-background border z-50">
 {sourceStructures.map((structure) => (
 <SelectItem key={structure.id} value={structure.id}>
 {structure.name} ({structure.type})
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="customHeaders">Custom Headers</Label>
 <Textarea
 id="customHeaders"
 placeholder="Additional HTTP headers needed (e.g., correlation ID)"
 value={configuration.customHeaders || ''}
 onChange={(e) => onConfigurationChange('customHeaders', e.target.value)}
 className="min-h-[80px]"
 />
 </div>
 </div>

 {/* Security Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Security</h3>

 <div className="space-y-2">
 <Label htmlFor="authenticationType">Authentication Type</Label>
 <Select
 value={configuration.authenticationType || ''}
 onValueChange={(value) => onConfigurationChange('authenticationType', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select authentication method" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="basic">Basic Auth</SelectItem>
 <SelectItem value="oauth2-bearer">OAuth2 Bearer Token</SelectItem>
 <SelectItem value="api-key">API Key</SelectItem>
 <SelectItem value="jwt">JWT</SelectItem>
 </SelectContent>
 </Select>
 </div>

 {configuration.authenticationType === 'basic' && (
 <>
 <div className="space-y-2">
 <Label htmlFor="basicUsername">Username</Label>
 <Input
 id="basicUsername"
 type="text"
 placeholder="Basic auth username"
 value={configuration.basicUsername || ''}
 onChange={(e) => onConfigurationChange('basicUsername', e.target.value)}
 />
 </div>
 <div className="space-y-2">
 <PasswordConfirmation
 name="basicPassword"
 label="Password"
 placeholder="Basic auth password"
 value={configuration.basicPassword || ''}
 onValueChange={(value) => onConfigurationChange('basicPassword', value)}
 showConfirmation={false}
 />
 </div>
 </>
 )}

 {configuration.authenticationType === 'oauth2-bearer' && (
 <div className="space-y-2">
 <PasswordConfirmation
 name="bearerToken"
 label="Bearer Token"
 placeholder="OAuth2 bearer token"
 value={configuration.bearerToken || ''}
 onValueChange={(value) => onConfigurationChange('bearerToken', value)}
 showConfirmation={false}
 />
 </div>
 )}

 {configuration.authenticationType === 'api-key' && (
 <>
 <div className="space-y-2">
 <Label htmlFor="apiKeyName">API Key Name</Label>
 <Input
 id="apiKeyName"
 type="text"
 placeholder="API key header name (e.g., X-API-Key)"
 value={configuration.apiKeyName || ''}
 onChange={(e) => onConfigurationChange('apiKeyName', e.target.value)}
 />
 </div>
 <div className="space-y-2">
 <PasswordConfirmation
 name="apiKeyValue"
 label="API Key Value"
 placeholder="API key value"
 value={configuration.apiKeyValue || ''}
 onValueChange={(value) => onConfigurationChange('apiKeyValue', value)}
 showConfirmation={false}
 />
 </div>
 </>
 )}

 {configuration.authenticationType === 'jwt' && (
 <>
 <div className="space-y-2">
 <Label htmlFor="jwtToken">JWT Token</Label>
 <Textarea
 id="jwtToken"
 placeholder="JWT token or signing key"
 value={configuration.jwtToken || ''}
 onChange={(e) => onConfigurationChange('jwtToken', e.target.value)}
 className="min-h-[80px]"
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="jwtAlgorithm">JWT Algorithm</Label>
 <Select
 value={configuration.jwtAlgorithm || ''}
 onValueChange={(value) => onConfigurationChange('jwtAlgorithm', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select JWT algorithm" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="HS256">HS256</SelectItem>
 <SelectItem value="HS384">HS384</SelectItem>
 <SelectItem value="HS512">HS512</SelectItem>
 <SelectItem value="RS256">RS256</SelectItem>
 <SelectItem value="RS384">RS384</SelectItem>
 <SelectItem value="RS512">RS512</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </>
 )}
 </div>

 {/* Response & Error Handling Section */}
 <div className="space-y-4">
 <h3 className="text-lg font-medium">Response & Error Handling</h3>

 <div className="space-y-2">
 <Label htmlFor="errorHandling">Error Handling</Label>
 <Textarea
 id="errorHandling"
 placeholder="Expected error codes and their meanings"
 value={configuration.errorHandling || ''}
 onChange={(e) => onConfigurationChange('errorHandling', e.target.value)}
 className="min-h-[80px]"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="retryPolicy">Retry Policy</Label>
 <Textarea
 id="retryPolicy"
 placeholder="Instructions on retries or idempotency"
 value={configuration.retryPolicy || ''}
 onChange={(e) => onConfigurationChange('retryPolicy', e.target.value)}
 className="min-h-[80px]"
 />
 </div>
 </div>
 </TabsContent>

 {/* Transformation Tab */}
 <TabsContent value="transformation" className="mt-6">
 <TransformationToggle
 config={transformationConfig}
 onChange={handleTransformationChange}
 adapterType="HTTP"
 dataStructures={sourceStructures}
 disabled={false}
 />
 </TabsContent>

 {/* Payload Structure Tab - Only show when transformation is enabled */}
 {transformationConfig.mode === 'transform' && (
 <TabsContent value="payload" className="mt-6">
 <PayloadStructureDefinition
 config={payloadConfig}
 onChange={handlePayloadChange}
 adapterType="HTTP"
 mode="sender"
 disabled={false}
 />
 </TabsContent>
 )}

 {/* XML Conversion Tab - Only show when transformation is enabled */}
 {transformationConfig.mode === 'transform' && (
 <TabsContent value="xmlConversion" className="mt-6">
 <XmlConversionTab
 mode="INBOUND"
 config={configuration.xmlConversion || {
 rootElementName: 'Message',
 encoding: 'UTF-8',
 includeXmlDeclaration: true,
 prettyPrint: true
 }}
 onChange={(xmlConversion) => onConfigurationChange('xmlConversion', xmlConversion)}
 />
 </TabsContent>
 )}
 </Tabs>
 </CardContent>
 </Card>
 )
}
import React, { useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle, CheckCircle2, Server, XCircle } from 'lucide-react';
import { AdapterConfiguration } from '@/types/adapter';
import { useJarFiles } from '@/hooks/useJarFiles';

interface IdocOutboundAdapterConfigurationProps {
 configuration: AdapterConfiguration;
 onConfigurationChange: (key: string, value: any) => void;
}

export const IdocOutboundAdapterConfiguration: React.FC<IdocOutboundAdapterConfigurationProps> = ({
 configuration,
 onConfigurationChange,
}) => {
 // Fetch SAP JAR files and validate dependencies
 const { 
   jarFiles, 
   isLoading, 
   validateSapJcoDependencies,
   validateSapIdocDependencies,
   getSapConnectionInfo 
 } = useJarFiles({
   driverType: 'SAP_IDOC',
   enabled: true
 });

 const jcoValidation = validateSapJcoDependencies();
 const idocValidation = validateSapIdocDependencies();
 const sapInfo = getSapConnectionInfo();

 return (
 <div className="space-y-6">
 {/* SAP Dependency Validation */}
 {!isLoading && (!jcoValidation.valid || !idocValidation.valid) && (
   <Alert variant="destructive">
     <AlertCircle className="h-4 w-4" />
     <AlertTitle>Missing SAP Dependencies</AlertTitle>
     <AlertDescription className="space-y-2">
       <p>SAP IDoc requires the following components:</p>
       <ul className="list-disc list-inside text-sm">
         {!idocValidation.hasIdocJar && (
           <li>SAP IDoc Library: sapidoc3.jar</li>
         )}
         {!jcoValidation.hasJar && (
           <li>SAP Java Connector: sapjco3.jar</li>
         )}
         {!jcoValidation.hasNativeLibrary && (
           <li>Native library for {jcoValidation.platform}: {jcoValidation.missingNativeLibrary}</li>
         )}
       </ul>
       <p className="text-sm mt-2">
         Please upload these files in the Admin settings before configuring this adapter.
         The IDoc library requires the JCo library to function properly.
       </p>
     </AlertDescription>
   </Alert>
 )}

 {!isLoading && jcoValidation.valid && idocValidation.valid && (
   <Alert variant="default" className="border-green-500">
     <CheckCircle2 className="h-4 w-4 text-green-500" />
     <AlertTitle>SAP Dependencies Verified</AlertTitle>
     <AlertDescription>
       SAP IDoc {sapInfo.idocVersion} and JCo {sapInfo.jcoVersion} are available for {jcoValidation.platform}.
     </AlertDescription>
   </Alert>
 )}

 <Tabs defaultValue="target" className="w-full">
 <TabsList className="grid w-full grid-cols-2">
 <TabsTrigger value="target">Target</TabsTrigger>
 <TabsTrigger value="processing">Processing</TabsTrigger>
 </TabsList>

 <TabsContent value="target" className="space-y-6">
 <Card>
 <CardHeader>
 <CardTitle>SAP System Identification</CardTitle>
 <CardDescription>SAP system connection details</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
 <div className="space-y-2">
 <Label htmlFor="sapSystemId">SAP System ID (SID) *</Label>
 <Input
 id="sapSystemId"
 value={configuration.sapSystemId || ''}
 onChange={(e) => onConfigurationChange('sapSystemId', e.target.value)}
 placeholder="PRD, DEV"
 required
 />
 <p className="text-xs text-muted-foreground">SAP system identifier</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="sapClientNumber">SAP Client Number *</Label>
 <Input
 id="sapClientNumber"
 value={configuration.sapClientNumber || ''}
 onChange={(e) => onConfigurationChange('sapClientNumber', e.target.value)}
 placeholder="100"
 required
 />
 <p className="text-xs text-muted-foreground">SAP client number</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="sapSystemNumber">SAP System Number *</Label>
 <Input
 id="sapSystemNumber"
 value={configuration.sapSystemNumber || ''}
 onChange={(e) => onConfigurationChange('sapSystemNumber', e.target.value)}
 placeholder="00"
 required
 />
 <p className="text-xs text-muted-foreground">Two-digit system number</p>
 </div>
 </div>
 </CardContent>
 </Card>

 <Card>
 <CardHeader>
 <CardTitle>Connection Details</CardTitle>
 <CardDescription>SAP application server and gateway configuration</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="sapApplicationServerHost">SAP Application Server Host *</Label>
 <Input
 id="sapApplicationServerHost"
 value={configuration.sapApplicationServerHost || ''}
 onChange={(e) => onConfigurationChange('sapApplicationServerHost', e.target.value)}
 placeholder="sapserver.example.com"
 required
 />
 <p className="text-sm text-muted-foreground">Hostname or IP of SAP application server</p>
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="sapGatewayHost">SAP Gateway Host</Label>
 <Input
 id="sapGatewayHost"
 value={configuration.sapGatewayHost || ''}
 onChange={(e) => onConfigurationChange('sapGatewayHost', e.target.value)}
 placeholder="sapgw.example.com"
 />
 <p className="text-xs text-muted-foreground">Gateway host (optional)</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="sapGatewayService">SAP Gateway Service</Label>
 <Input
 id="sapGatewayService"
 value={configuration.sapGatewayService || ''}
 onChange={(e) => onConfigurationChange('sapGatewayService', e.target.value)}
 placeholder="sapgw00"
 />
 <p className="text-xs text-muted-foreground">Gateway service/port (optional)</p>
 </div>
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="portNumber">Port Number</Label>
 <Input
 id="portNumber"
 type="number"
 value={configuration.portNumber || ''}
 onChange={(e) => onConfigurationChange('portNumber', parseInt(e.target.value) || 0)}
 placeholder="3300"
 />
 <p className="text-xs text-muted-foreground">Network port if applicable</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="connectionType">Connection Type</Label>
 <Select
   value={configuration.connectionType || 'TCP/IP'}
   onValueChange={(value) => onConfigurationChange('connectionType', value)}
 >
   <SelectTrigger>
     <SelectValue placeholder="Select connection type" />
   </SelectTrigger>
   <SelectContent>
     <SelectItem value="TCP/IP">TCP/IP</SelectItem>
     <SelectItem value="Gateway">Gateway</SelectItem>
   </SelectContent>
 </Select>
 <p className="text-xs text-muted-foreground">Connection protocol</p>
 </div>
 </div>
 </CardContent>
 </Card>

 <Card>
 <CardHeader>
 <CardTitle>Credentials</CardTitle>
 <CardDescription>SAP authentication</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="sapUser">SAP Username *</Label>
 <Input
 id="sapUser"
 value={configuration.sapUser || ''}
 onChange={(e) => onConfigurationChange('sapUser', e.target.value)}
 placeholder="SAP Username"
 required
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="sapPassword">SAP Password *</Label>
 <Input
 id="sapPassword"
 type="password"
 value={configuration.sapPassword || ''}
 onChange={(e) => onConfigurationChange('sapPassword', e.target.value)}
 placeholder="••••••••"
 required
 />
 </div>
 </div>
 </CardContent>
 </Card>

 <Card>
 <CardHeader>
 <CardTitle>RFC Destination</CardTitle>
 <CardDescription>RFC destination for IDoc transmission</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="rfcDestinationName">RFC Destination Name</Label>
 <Input
 id="rfcDestinationName"
 value={configuration.rfcDestinationName || ''}
 onChange={(e) => onConfigurationChange('rfcDestinationName', e.target.value)}
 placeholder="MIDDLEWARE_RFC"
 />
 <p className="text-sm text-muted-foreground">SAP RFC destination (optional)</p>
 </div>
 </CardContent>
 </Card>
 </TabsContent>

 <TabsContent value="processing" className="space-y-6">
 <Card>
 <CardHeader>
 <CardTitle>IDoc Transmission</CardTitle>
 <CardDescription>Configure IDoc transmission settings</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="idocPort">IDoc Port</Label>
 <Input
 id="idocPort"
 value={configuration.idocPort || ''}
 onChange={(e) => onConfigurationChange('idocPort', e.target.value)}
 placeholder="SAPDEV"
 />
 <p className="text-xs text-muted-foreground">Logical port configured in SAP</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="listenerServiceName">Listener Service Name</Label>
 <Input
 id="listenerServiceName"
 value={configuration.listenerServiceName || ''}
 onChange={(e) => onConfigurationChange('listenerServiceName', e.target.value)}
 placeholder="MIDDLEWARE_IDOC_SERVICE"
 />
 <p className="text-xs text-muted-foreground">Middleware service name</p>
 </div>
 </div>
 </CardContent>
 </Card>

 <Card>
 <CardHeader>
 <CardTitle>IDoc Identification</CardTitle>
 <CardDescription>IDoc type and message configuration</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
 <div className="space-y-2">
 <Label htmlFor="idocType">IDoc Type *</Label>
 <Input
 id="idocType"
 value={configuration.idocType || ''}
 onChange={(e) => onConfigurationChange('idocType', e.target.value)}
 placeholder="ORDERS05"
 required
 />
 <p className="text-xs text-muted-foreground">IDoc structure type</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="messageType">Message Type *</Label>
 <Input
 id="messageType"
 value={configuration.messageType || ''}
 onChange={(e) => onConfigurationChange('messageType', e.target.value)}
 placeholder="ORDERS"
 required
 />
 <p className="text-xs text-muted-foreground">Logical message type</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="processCode">Process Code</Label>
 <Input
 id="processCode"
 value={configuration.processCode || ''}
 onChange={(e) => onConfigurationChange('processCode', e.target.value)}
 placeholder="ORDE"
 />
 <p className="text-xs text-muted-foreground">SAP process code</p>
 </div>
 </div>

 <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
 <div className="space-y-2">
 <Label htmlFor="senderPartnerNumber">Sender Partner Number</Label>
 <Input
 id="senderPartnerNumber"
 value={configuration.senderPartnerNumber || ''}
 onChange={(e) => onConfigurationChange('senderPartnerNumber', e.target.value)}
 placeholder="MIDDLEWARE"
 />
 <p className="text-xs text-muted-foreground">Logical sender system</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="senderPartnerType">Sender Partner Type</Label>
 <Select
   value={configuration.senderPartnerType || 'LS'}
   onValueChange={(value) => onConfigurationChange('senderPartnerType', value)}
 >
   <SelectTrigger>
     <SelectValue />
   </SelectTrigger>
   <SelectContent>
     <SelectItem value="LS">LS - Logical System</SelectItem>
     <SelectItem value="LI">LI - Vendor</SelectItem>
     <SelectItem value="KU">KU - Customer</SelectItem>
     <SelectItem value="B">B - Bank</SelectItem>
   </SelectContent>
 </Select>
 <p className="text-xs text-muted-foreground">Partner type</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="receiverPartnerNumber">Receiver Partner Number</Label>
 <Input
 id="receiverPartnerNumber"
 value={configuration.receiverPartnerNumber || ''}
 onChange={(e) => onConfigurationChange('receiverPartnerNumber', e.target.value)}
 placeholder="SAPSYSTEM"
 />
 <p className="text-xs text-muted-foreground">Target SAP system</p>
 </div>
 </div>
 </CardContent>
 </Card>

 <Card>
 <CardHeader>
 <CardTitle>Security / Encryption</CardTitle>
 <CardDescription>Secure Network Communications (SNC) settings</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="sncSecuritySettings">SNC / Security Settings</Label>
 <Textarea
 id="sncSecuritySettings"
 value={configuration.sncSecuritySettings || ''}
 onChange={(e) => onConfigurationChange('sncSecuritySettings', e.target.value)}
 placeholder="p:CN=username@REALM"
 rows={4}
 className="font-mono text-sm"
 />
 <p className="text-sm text-muted-foreground">
   Secure Network Communications parameters (optional)
 </p>
 </div>
 </CardContent>
 </Card>

 {/* SAP Library Information */}
 {sapInfo.hasJco && sapInfo.hasIdoc && (
   <Card>
     <CardHeader>
       <CardTitle>SAP Library Information</CardTitle>
       <CardDescription>
         Loaded SAP IDoc and JCo libraries
       </CardDescription>
     </CardHeader>
     <CardContent>
       <div className="space-y-2">
         <div className="flex items-center justify-between text-sm">
           <span className="font-medium">JCo Version:</span>
           <span className="font-mono">{sapInfo.jcoVersion}</span>
         </div>
         <div className="flex items-center justify-between text-sm">
           <span className="font-medium">IDoc Version:</span>
           <span className="font-mono">{sapInfo.idocVersion}</span>
         </div>
         <div className="flex items-center justify-between text-sm">
           <span className="font-medium">Platform:</span>
           <span className="font-mono">{jcoValidation.platform}</span>
         </div>
         <div className="flex items-center gap-2 text-sm">
           <Server className="h-4 w-4 text-muted-foreground" />
           <span className="text-muted-foreground">
             Ready for IDoc communication
           </span>
         </div>
       </div>
     </CardContent>
   </Card>
 )}
 </TabsContent>
 </Tabs>
 </div>
 );
};
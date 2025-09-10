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

interface RfcOutboundAdapterConfigurationProps {
 configuration: AdapterConfiguration;
 onConfigurationChange: (key: string, value: any) => void;
}

export const RfcOutboundAdapterConfiguration: React.FC<RfcOutboundAdapterConfigurationProps> = ({
 configuration,
 onConfigurationChange,
}) => {
 // Fetch SAP JAR files and validate dependencies
 const { 
   jarFiles, 
   isLoading, 
   validateSapJcoDependencies,
   getSapConnectionInfo 
 } = useJarFiles({
   driverType: 'SAP_JCO',
   enabled: true
 });

 const jcoValidation = validateSapJcoDependencies();
 const sapInfo = getSapConnectionInfo();

 return (
 <div className="space-y-6">
 {/* SAP JCo Dependency Validation */}
 {!isLoading && !jcoValidation.valid && (
   <Alert variant="destructive">
     <AlertCircle className="h-4 w-4" />
     <AlertTitle>Missing SAP JCo Dependencies</AlertTitle>
     <AlertDescription className="space-y-2">
       <p>SAP RFC requires the following components:</p>
       <ul className="list-disc list-inside text-sm">
         {!jcoValidation.hasJar && (
           <li>SAP Java Connector: {jcoValidation.missingJar}</li>
         )}
         {!jcoValidation.hasNativeLibrary && (
           <li>Native library for {jcoValidation.platform}: {jcoValidation.missingNativeLibrary}</li>
         )}
       </ul>
       <p className="text-sm mt-2">
         Please upload these files in the Admin settings before configuring this adapter.
       </p>
     </AlertDescription>
   </Alert>
 )}

 {!isLoading && jcoValidation.valid && (
   <Alert variant="default" className="border-green-500">
     <CheckCircle2 className="h-4 w-4 text-green-500" />
     <AlertTitle>SAP JCo Dependencies Verified</AlertTitle>
     <AlertDescription>
       SAP Java Connector {sapInfo.jcoVersion} is available for {jcoValidation.platform}.
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
 <Label htmlFor="sapLanguage">SAP Language</Label>
 <Select
   value={configuration.sapLanguage || 'EN'}
   onValueChange={(value) => onConfigurationChange('sapLanguage', value)}
 >
   <SelectTrigger>
     <SelectValue placeholder="Select language" />
   </SelectTrigger>
   <SelectContent>
     <SelectItem value="EN">English</SelectItem>
     <SelectItem value="DE">German</SelectItem>
     <SelectItem value="FR">French</SelectItem>
     <SelectItem value="ES">Spanish</SelectItem>
     <SelectItem value="PT">Portuguese</SelectItem>
     <SelectItem value="ZH">Chinese</SelectItem>
     <SelectItem value="JA">Japanese</SelectItem>
   </SelectContent>
 </Select>
 <p className="text-xs text-muted-foreground">Login language</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="sapLogonGroup">SAP Logon Group</Label>
 <Input
 id="sapLogonGroup"
 value={configuration.sapLogonGroup || ''}
 onChange={(e) => onConfigurationChange('sapLogonGroup', e.target.value)}
 placeholder="PUBLIC"
 />
 <p className="text-xs text-muted-foreground">For load balancing</p>
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
 <Label htmlFor="sapUsername">SAP Username *</Label>
 <Input
 id="sapUsername"
 value={configuration.sapUsername || ''}
 onChange={(e) => onConfigurationChange('sapUsername', e.target.value)}
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
 </TabsContent>

 <TabsContent value="processing" className="space-y-6">
 <Card>
 <CardHeader>
 <CardTitle>RFC Processing</CardTitle>
 <CardDescription>Remote Function Call execution details</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="functionName">RFC Function Name *</Label>
 <Input
 id="functionName"
 value={configuration.functionName || ''}
 onChange={(e) => onConfigurationChange('functionName', e.target.value)}
 placeholder="BAPI_CUSTOMER_GETDETAIL2"
 required
 />
 <p className="text-sm text-muted-foreground">Name of the RFC function module to call</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="importParameters">Import Parameters (JSON)</Label>
 <Textarea
 id="importParameters"
 value={configuration.importParameters || ''}
 onChange={(e) => onConfigurationChange('importParameters', e.target.value)}
 placeholder='{"CUSTOMER": "0000001234", "COMPANYCODE": "1000"}'
 className="min-h-[100px] font-mono text-sm"
 />
 <p className="text-sm text-muted-foreground">Input parameters for the RFC function</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="tableParameters">Table Parameters (JSON)</Label>
 <Textarea
 id="tableParameters"
 value={configuration.tableParameters || ''}
 onChange={(e) => onConfigurationChange('tableParameters', e.target.value)}
 placeholder='{"ADDRESSES": [], "SALES_AREAS": []}'
 className="min-h-[100px] font-mono text-sm"
 />
 <p className="text-sm text-muted-foreground">Table parameters for the RFC function</p>
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="maxPoolSize">Connection Pool Size</Label>
 <Input
 id="maxPoolSize"
 type="number"
 value={configuration.maxPoolSize || '5'}
 onChange={(e) => onConfigurationChange('maxPoolSize', parseInt(e.target.value))}
 placeholder="5"
 />
 <p className="text-xs text-muted-foreground">Maximum connections</p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="peakLimit">Peak Limit</Label>
 <Input
 id="peakLimit"
 type="number"
 value={configuration.peakLimit || '10'}
 onChange={(e) => onConfigurationChange('peakLimit', parseInt(e.target.value))}
 placeholder="10"
 />
 <p className="text-xs text-muted-foreground">Peak connection limit</p>
 </div>
 </div>
 </CardContent>
 </Card>

 {/* SAP Library Information */}
 {sapInfo.hasJco && (
   <Card>
     <CardHeader>
       <CardTitle>SAP JCo Information</CardTitle>
       <CardDescription>
         Loaded SAP Java Connector libraries
       </CardDescription>
     </CardHeader>
     <CardContent>
       <div className="space-y-2">
         <div className="flex items-center justify-between text-sm">
           <span className="font-medium">JCo Version:</span>
           <span className="font-mono">{sapInfo.jcoVersion}</span>
         </div>
         <div className="flex items-center justify-between text-sm">
           <span className="font-medium">Platform:</span>
           <span className="font-mono">{jcoValidation.platform}</span>
         </div>
         <div className="flex items-center gap-2 text-sm">
           <Server className="h-4 w-4 text-muted-foreground" />
           <span className="text-muted-foreground">
             Ready for RFC communication
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
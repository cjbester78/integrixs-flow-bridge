import { useEffect, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle, CheckCircle2, XCircle } from 'lucide-react';
import { CommunicationAdapter } from '@/types/adapter';
import { CertificateSelection } from '@/components/ui/certificate-selection';
import { useJarFiles } from '@/hooks/useJarFiles';

interface IbmmqOutboundAdapterConfigurationProps {
 adapter: CommunicationAdapter;
 onUpdate: (adapter: CommunicationAdapter) => void;
 businessComponentId?: string;
}

export function IbmmqOutboundAdapterConfiguration({
 adapter,
 onUpdate,
 businessComponentId
}: IbmmqOutboundAdapterConfigurationProps) {
 
 // Fetch IBM MQ JAR files and validate dependencies
 const { 
   jarFiles, 
   isLoading, 
   validateIbmMqDependencies, 
   getIbmMqConnectionInfo 
 } = useJarFiles({
   driverType: 'IBMMQ',
   enabled: true
 });

 const dependencyCheck = validateIbmMqDependencies();
 const connectionInfo = getIbmMqConnectionInfo();

 const handleInputChange = useCallback((field: string, value: string | number | boolean) => {
 onUpdate({
 ...adapter,
 configuration: {
 ...adapter.configuration,
 [field]: value
 }
 });
 }, [adapter, onUpdate]);

 // Auto-populate connection classes when component mounts
 useEffect(() => {
   if (connectionInfo && !adapter.configuration.connectionFactoryClass) {
     handleInputChange('connectionFactoryClass', connectionInfo.connectionFactoryClass);
     handleInputChange('queueClass', connectionInfo.queueClass);
   }
 }, [jarFiles, connectionInfo, adapter.configuration.connectionFactoryClass, handleInputChange]);

 return (
 <div className="space-y-6">
 {/* JAR Dependency Validation */}
 {!isLoading && !dependencyCheck.valid && (
   <Alert variant="destructive">
     <AlertCircle className="h-4 w-4" />
     <AlertTitle>Missing IBM MQ Dependencies</AlertTitle>
     <AlertDescription className="space-y-2">
       <p>IBM MQ requires {dependencyCheck.required} JAR files. Currently {dependencyCheck.uploaded} uploaded.</p>
       <p className="text-sm">Missing JARs:</p>
       <ul className="list-disc list-inside text-sm">
         {dependencyCheck.missing.map((jar) => (
           <li key={jar}>{jar}</li>
         ))}
       </ul>
     </AlertDescription>
   </Alert>
 )}

 {!isLoading && dependencyCheck.valid && (
   <Alert variant="default" className="border-green-500">
     <CheckCircle2 className="h-4 w-4 text-green-500" />
     <AlertTitle>IBM MQ Dependencies Verified</AlertTitle>
     <AlertDescription>
       All required IBM MQ JAR files are available. Version: {connectionInfo.version}
     </AlertDescription>
   </Alert>
 )}

 <Tabs defaultValue="target" className="w-full">
 <TabsList className="grid w-full grid-cols-2">
 <TabsTrigger value="target">Target</TabsTrigger>
 <TabsTrigger value="processing">Processing Details</TabsTrigger>
 </TabsList>

 <TabsContent value="target" className="space-y-6">
 <Card>
 <CardHeader>
 <CardTitle>Connection Details</CardTitle>
 <CardDescription>
   Configure IBM MQ connection parameters
 </CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="connectionFactoryClass">Connection Factory class</Label>
 <Input
 id="connectionFactoryClass"
 placeholder={connectionInfo.connectionFactoryClass}
 value={adapter.configuration.connectionFactoryClass || connectionInfo.connectionFactoryClass}
 onChange={(e) => handleInputChange('connectionFactoryClass', e.target.value)}
 readOnly={!!connectionInfo.connectionFactoryClass}
 className={connectionInfo.connectionFactoryClass ? "bg-muted" : ""}
 />
 {connectionInfo.connectionFactoryClass && (
   <p className="text-xs text-muted-foreground flex items-center gap-1">
     <CheckCircle2 className="h-3 w-3" />
     Auto-detected from IBM MQ {connectionInfo.version}
   </p>
 )}
 </div>

 <div className="space-y-2">
 <Label htmlFor="queueClass">Queue class</Label>
 <Input
 id="queueClass"
 placeholder={connectionInfo.queueClass}
 value={adapter.configuration.queueClass || connectionInfo.queueClass}
 onChange={(e) => handleInputChange('queueClass', e.target.value)}
 readOnly={!!connectionInfo.queueClass}
 className={connectionInfo.queueClass ? "bg-muted" : ""}
 />
 {connectionInfo.queueClass && (
   <p className="text-xs text-muted-foreground flex items-center gap-1">
     <CheckCircle2 className="h-3 w-3" />
     Auto-detected from IBM MQ {connectionInfo.version}
   </p>
 )}
 </div>

 <div className="space-y-2">
 <Label htmlFor="queueManager">Queue Manager Name *</Label>
 <Input
 id="queueManager"
 placeholder="QM1"
 value={adapter.configuration.queueManager || ''}
 onChange={(e) => handleInputChange('queueManager', e.target.value)}
 required
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="host">Host Name / IP *</Label>
 <Input
 id="host"
 placeholder="mqserver.example.com"
 value={adapter.configuration.host || ''}
 onChange={(e) => handleInputChange('host', e.target.value)}
 required
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="port">Port *</Label>
 <Input
 id="port"
 type="number"
 placeholder="1414"
 value={adapter.configuration.port || ''}
 onChange={(e) => handleInputChange('port', parseInt(e.target.value))}
 required
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="channel">Channel Name *</Label>
 <Input
 id="channel"
 placeholder="CHANNEL.SVRCONN"
 value={adapter.configuration.channel || ''}
 onChange={(e) => handleInputChange('channel', e.target.value)}
 required
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="connectionFactory">Connection Factory JNDI Name</Label>
 <Input
 id="connectionFactory"
 placeholder="jms/ConnectionFactory"
 value={adapter.configuration.connectionFactory || ''}
 onChange={(e) => handleInputChange('connectionFactory', e.target.value)}
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="transportType">Transport Type *</Label>
 <Select
 value={adapter.configuration.transportType || 'CLIENT'}
 onValueChange={(value) => handleInputChange('transportType', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select transport type" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="CLIENT">Remote TCP/IP</SelectItem>
 <SelectItem value="BINDINGS">Local (in-memory)</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="username">User Name</Label>
 <Input
 id="username"
 placeholder="MQ Username"
 value={adapter.configuration.username || ''}
 onChange={(e) => handleInputChange('username', e.target.value)}
 />
 </div>

 <div className="space-y-2">
 <PasswordConfirmation
 name="password"
 label="Password"
 placeholder="MQ Password"
 required={false}
 value={adapter.configuration.password || ''}
 onValueChange={(value) => handleInputChange('password', value)}
 />
 </div>
 </div>

 <div className="flex items-center space-x-2">
 <Checkbox
 id="useSSL"
 checked={adapter.configuration.useSSL || false}
 onCheckedChange={(checked) => handleInputChange('useSSL', checked)}
 />
 <Label htmlFor="useSSL">Use SSL/TLS</Label>
 </div>

 {adapter.configuration.useSSL && (
 <div className="space-y-4">
 <CertificateSelection
 id="sslCertificate"
 label="SSL Certificate"
 value={adapter.configuration.sslCertificateId || ''}
 onChange={(value) => handleInputChange('sslCertificateId', value)}
 businessComponentId={businessComponentId}
 placeholder="Select SSL certificate"
 required
 />
 </div>
 )}
 </CardContent>
 </Card>
 </TabsContent>

 <TabsContent value="processing" className="space-y-6">
 <Card>
 <CardHeader>
 <CardTitle>Processing Details</CardTitle>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="queueName">Queue Name *</Label>
 <Input
 id="queueName"
 placeholder="INBOUND.FILE.QUEUE"
 value={adapter.configuration.queueName || ''}
 onChange={(e) => handleInputChange('queueName', e.target.value)}
 required
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="destinationType">JMS Destination Type *</Label>
 <Select
 value={adapter.configuration.destinationType || ''}
 onValueChange={(value) => handleInputChange('destinationType', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select destination type" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="Queue">Queue</SelectItem>
 <SelectItem value="Topic">Topic</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="messageSelector">Message Selector</Label>
 <Input
 id="messageSelector"
 placeholder="JMSType= 'FileMessage'"
 value={adapter.configuration.messageSelector || ''}
 onChange={(e) => handleInputChange('messageSelector', e.target.value)}
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="clientId">Client ID</Label>
 <Input
 id="clientId"
 placeholder="Optional Client ID"
 value={adapter.configuration.clientId || ''}
 onChange={(e) => handleInputChange('clientId', e.target.value)}
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="ackMode">Acknowledgement Mode *</Label>
 <Select
 value={adapter.configuration.ackMode || 'AUTO_ACKNOWLEDGE'}
 onValueChange={(value) => handleInputChange('ackMode', value)}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select acknowledgement mode" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="AUTO_ACKNOWLEDGE">AUTO_ACKNOWLEDGE</SelectItem>
 <SelectItem value="CLIENT_ACKNOWLEDGE">CLIENT_ACKNOWLEDGE</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </div>
 </CardContent>
 </Card>

 {/* JAR Information Card */}
 {jarFiles.length > 0 && (
   <Card>
     <CardHeader>
       <CardTitle>Loaded IBM MQ Libraries</CardTitle>
       <CardDescription>
         {jarFiles.length} JAR files available for IBM MQ connectivity
       </CardDescription>
     </CardHeader>
     <CardContent>
       <div className="space-y-2">
         {jarFiles.map((jar) => (
           <div key={jar.id} className="flex items-center justify-between text-sm">
             <span className="font-mono">{jar.file_name}</span>
             <span className="text-muted-foreground">{jar.version}</span>
           </div>
         ))}
       </div>
     </CardContent>
   </Card>
 )}
 </TabsContent>
 </Tabs>
 </div>
 );
}
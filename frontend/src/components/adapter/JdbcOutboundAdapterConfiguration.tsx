import React, { useEffect, useState, useCallback } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle, Database, CheckCircle2 } from 'lucide-react';
import { AdapterConfiguration } from '@/types/adapter';
import { useJarFiles } from '@/hooks/useJarFiles';

interface JdbcOutboundAdapterConfigurationProps {
 configuration: AdapterConfiguration;
 onChange: (configuration: AdapterConfiguration) => void;
}

export const JdbcOutboundAdapterConfiguration: React.FC<JdbcOutboundAdapterConfigurationProps> = ({
 configuration,
 onChange,
}) => {
 const [selectedVendor, setSelectedVendor] = useState<string>(configuration.properties?.jdbcVendor || '');
 
 // Fetch available JDBC drivers
 const { jarFiles, isLoading, getAvailableVendors, getDriverInfo } = useJarFiles({
   driverType: 'JDBC',
   enabled: true
 });

 const availableVendors = getAvailableVendors();

 const updateConfiguration = useCallback((updates: Partial<AdapterConfiguration>) => {
 onChange({ ...configuration, ...updates });
 }, [configuration, onChange]);

 const updateProperties = useCallback((key: string, value: string) => {
 const properties = configuration.properties || {};
 updateConfiguration({
 properties: { ...properties, [key]: value }
 });
 }, [configuration.properties, updateConfiguration]);

 const getProperty = useCallback((key: string) => {
 return configuration.properties?.[key] || '';
 }, [configuration.properties]);

 // Auto-populate driver info when vendor changes
 useEffect(() => {
   if (selectedVendor) {
     const driverInfo = getDriverInfo(selectedVendor);
     if (driverInfo) {
       // Auto-populate driver class and URL format
       updateProperties('jdbcDriverClass', driverInfo.driverClass);
       updateProperties('jdbcUrlFormat', driverInfo.urlFormat);
       updateProperties('databasePort', driverInfo.defaultPort.toString());
       
       // Update the JDBC URL if host and database are provided
       const host = getProperty('databaseHost');
       const database = getProperty('databaseName');
       if (host && database) {
         const url = driverInfo.urlFormat
           .replace('{host}', host)
           .replace('{port}', driverInfo.defaultPort.toString())
           .replace('{database}', database);
         updateProperties('jdbcUrl', url);
       }
     }
   }
 }, [selectedVendor, getDriverInfo, getProperty, updateProperties]);

 // Update JDBC URL when host, port, or database changes
 useEffect(() => {
   const host = getProperty('databaseHost');
   const port = getProperty('databasePort');
   const database = getProperty('databaseName');
   const urlFormat = getProperty('jdbcUrlFormat');
   
   if (host && database && urlFormat) {
     const url = urlFormat
       .replace('{host}', host)
       .replace('{port}', port || '3306')
       .replace('{database}', database);
     updateProperties('jdbcUrl', url);
   }
 }, [configuration.properties?.databaseHost, configuration.properties?.databasePort, configuration.properties?.databaseName, getProperty, updateProperties]);

 return (
 <div className="space-y-6">
 <Tabs defaultValue="target" className="w-full">
 <TabsList className="grid w-full grid-cols-2">
 <TabsTrigger value="target">Target</TabsTrigger>
 <TabsTrigger value="processing">Processing</TabsTrigger>
 </TabsList>

 <TabsContent value="target" className="space-y-6">
 {/* Driver Selection Alert */}
 {availableVendors.length === 0 && !isLoading && (
   <Alert variant="warning">
     <AlertCircle className="h-4 w-4" />
     <AlertTitle>No JDBC Drivers Available</AlertTitle>
     <AlertDescription>
       Please upload JDBC driver JAR files in the Admin settings before configuring this adapter.
     </AlertDescription>
   </Alert>
 )}

 {/* Database Connection Section */}
 <Card>
 <CardHeader>
 <CardTitle>Target Database Connection</CardTitle>
 <CardDescription>
 Configure connection to the target database for data insertion and updates
 </CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="jdbcVendor">Database Vendor *</Label>
 <Select
   value={selectedVendor}
   onValueChange={(value) => {
     setSelectedVendor(value);
     updateProperties('jdbcVendor', value);
   }}
   disabled={isLoading || availableVendors.length === 0}
 >
   <SelectTrigger>
     <SelectValue placeholder={isLoading ? "Loading drivers..." : "Select database vendor"} />
   </SelectTrigger>
   <SelectContent>
     {availableVendors.map((vendor) => (
       <SelectItem key={vendor} value={vendor.toLowerCase()}>
         <div className="flex items-center gap-2">
           <Database className="h-4 w-4" />
           {vendor}
         </div>
       </SelectItem>
     ))}
   </SelectContent>
 </Select>
 {selectedVendor && (
   <div className="flex items-center gap-2 text-sm text-muted-foreground">
     <CheckCircle2 className="h-3 w-3 text-green-500" />
     Driver available
   </div>
 )}
 </div>
 <div className="space-y-2">
 <Label htmlFor="jdbcDriverClass">JDBC Driver Class</Label>
 <Input
 id="jdbcDriverClass"
 placeholder="Auto-populated based on vendor"
 value={getProperty('jdbcDriverClass')}
 readOnly
 className="bg-muted"
 />
 </div>
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="databaseHost">Database Host</Label>
 <Input
 id="databaseHost"
 placeholder="dbhost.example.com"
 value={getProperty('databaseHost')}
 onChange={(e) => updateProperties('databaseHost', e.target.value)}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="databasePort">Database Port</Label>
 <Input
 id="databasePort"
 type="number"
 placeholder={getProperty('databasePort') || "3306"}
 value={getProperty('databasePort')}
 onChange={(e) => updateProperties('databasePort', e.target.value)}
 />
 </div>
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="databaseName">Database Name</Label>
 <Input
 id="databaseName"
 placeholder="ordersdb"
 value={getProperty('databaseName')}
 onChange={(e) => updateProperties('databaseName', e.target.value)}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="username">Username</Label>
 <Input
 id="username"
 placeholder="dbuser"
 value={getProperty('username')}
 onChange={(e) => updateProperties('username', e.target.value)}
 />
 </div>
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="password">Password</Label>
 <Input
 id="password"
 type="password"
 placeholder="dbpassword"
 value={getProperty('password')}
 onChange={(e) => updateProperties('password', e.target.value)}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="connectionPoolSettings">Connection Pool Settings</Label>
 <Input
 id="connectionPoolSettings"
 placeholder="maxPoolSize=10, connectionTimeout=30000"
 value={getProperty('connectionPoolSettings')}
 onChange={(e) => updateProperties('connectionPoolSettings', e.target.value)}
 />
 </div>
 </div>

 {/* Auto-generated JDBC URL */}
 <div className="space-y-2">
 <Label htmlFor="jdbcUrl">Generated JDBC URL</Label>
 <Input
 id="jdbcUrl"
 placeholder="Auto-generated from connection details"
 value={getProperty('jdbcUrl')}
 onChange={(e) => updateProperties('jdbcUrl', e.target.value)}
 className="font-mono text-sm"
 />
 <p className="text-xs text-muted-foreground">
 Format: {getProperty('jdbcUrlFormat') || 'Select a vendor to see URL format'}
 </p>
 </div>
 </CardContent>
 </Card>

 {/* Data Insertion and Update Operations */}
 <Card>
 <CardHeader>
 <CardTitle>Data Operations</CardTitle>
 <CardDescription>
 Configure how data is inserted and updated in the target database
 </CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="insertQuery">Insert Query</Label>
 <Textarea
 id="insertQuery"
 placeholder="INSERT INTO orders (order_id, customer_id, total) VALUES (?, ?, ?)"
 value={getProperty('insertQuery')}
 onChange={(e) => updateProperties('insertQuery', e.target.value)}
 className="min-h-[100px]"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="updateQuery">Update Query</Label>
 <Textarea
 id="updateQuery"
 placeholder="UPDATE orders SET status = ?, updated_at = ? WHERE order_id = ?"
 value={getProperty('updateQuery')}
 onChange={(e) => updateProperties('updateQuery', e.target.value)}
 className="min-h-[100px]"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="deleteQuery">Delete Query</Label>
 <Textarea
 id="deleteQuery"
 placeholder="DELETE FROM orders WHERE order_id = ?"
 value={getProperty('deleteQuery')}
 onChange={(e) => updateProperties('deleteQuery', e.target.value)}
 className="min-h-[100px]"
 />
 </div>
 </CardContent>
 </Card>
 </TabsContent>

 <TabsContent value="processing" className="space-y-6">
 {/* Batch Processing Settings */}
 <Card>
 <CardHeader>
 <CardTitle>Batch Processing</CardTitle>
 <CardDescription>
 Configure batch processing for better performance
 </CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="batchSize">Batch Size</Label>
 <Input
 id="batchSize"
 type="number"
 placeholder="100"
 value={getProperty('batchSize') || '100'}
 onChange={(e) => updateProperties('batchSize', e.target.value)}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="batchCommit">Batch Commit</Label>
 <Select
   value={getProperty('batchCommit') || 'true'}
   onValueChange={(value) => updateProperties('batchCommit', value)}
 >
   <SelectTrigger>
     <SelectValue placeholder="Select batch commit" />
   </SelectTrigger>
   <SelectContent>
     <SelectItem value="true">Enabled</SelectItem>
     <SelectItem value="false">Disabled</SelectItem>
   </SelectContent>
 </Select>
 </div>
 </div>
 </CardContent>
 </Card>

 {/* Error Handling Settings */}
 <Card>
 <CardHeader>
 <CardTitle>Error Handling</CardTitle>
 <CardDescription>
 Configure how to handle database errors
 </CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="errorHandling">Error Handling Strategy</Label>
 <Select
   value={getProperty('errorHandling') || 'stop'}
   onValueChange={(value) => updateProperties('errorHandling', value)}
 >
   <SelectTrigger>
     <SelectValue placeholder="Select error handling" />
   </SelectTrigger>
   <SelectContent>
     <SelectItem value="stop">Stop on Error</SelectItem>
     <SelectItem value="continue">Continue Processing</SelectItem>
     <SelectItem value="rollback">Rollback Transaction</SelectItem>
   </SelectContent>
 </Select>
 </div>
 <div className="space-y-2">
 <Label htmlFor="retryCount">Retry Count</Label>
 <Input
 id="retryCount"
 type="number"
 placeholder="3"
 value={getProperty('retryCount') || '3'}
 onChange={(e) => updateProperties('retryCount', e.target.value)}
 />
 </div>
 </div>
 </CardContent>
 </Card>
 </TabsContent>
 </Tabs>
 </div>
 );
};
import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';
import { FileFormatTab, FileFormatConfig } from '../adapters/FileFormatTab';
import { XmlConversionTab } from '../adapters/XmlConversionTab';
import { TransformationToggle, TransformationConfig } from '../adapters/TransformationToggle';

interface FtpAdapterConfigurationProps {
 mode: 'sender' | 'receiver';
 onConfigChange?: (config: FtpAdapterConfig) => void;
}

interface FileAccessAdvancedEntry {
directory: string;
 fileName: string;
 exclusionMask: string;
}

interface FtpAdapterConfig {
 // Common Connection Parameters
 serverAddress: string;
 port: string;
 timeout: string;
 connectionSecurity: string;
 userName: string;
 password: string;
 connectionMode: string;

 // Inbound-specific fields
 sourceDirectory: string;
 fileName: string;
 sorting: string;
 advancedSelection: boolean;
 exclusionMask: string;
 advancedEntries: FileAccessAdvancedEntry[];
 pollingInterval: string;
 processingMode: string;
 emptyFileHandling: string;
 enableDuplicateHandling: boolean;
 duplicateMessageAlertThreshold: string;
 disableChannelOnExceed: boolean;

 // Outbound-specific fields
 targetDirectory: string;
 targetFileName: string;
 fileConstructionMode: string;
 overwriteExistingFile: boolean;
 createFileDirectory: boolean;
 filePlacement: string;
 emptyMessageHandling_receiver: string;
 maxConcurrentConnections: string;

 // File Format Configuration
 fileFormatConfig?: FileFormatConfig;

 // XML Conversion Configuration
 xmlConversion?: {
 rootElementName?: string;
 encoding?: string;
 includeXmlDeclaration?: boolean;
 prettyPrint?: boolean;
 targetNamespace?: string;
 namespacePrefix?: string;
 removeRootElement?: boolean;
 handleNamespaces?: boolean;
 preserveAttributes?: boolean;
 };

 // Transformation Configuration
 transformationConfig?: TransformationConfig;
}

export const FtpAdapterConfiguration = ({ mode, onConfigChange }: FtpAdapterConfigurationProps) => {
 const [config, setConfig] = useState<FtpAdapterConfig>({
 // Common connection parameters
 serverAddress: '',
 port: '',
 timeout: '',
 connectionSecurity: '',
 userName: '',
 password: '',
 connectionMode: '',

 // Inbound-specific fields
 sourceDirectory: '',
 fileName: '',
 sorting: '',
 advancedSelection: false,
 exclusionMask: '',
 advancedEntries: [{ directory: '', fileName: '', exclusionMask: '' }],
 pollingInterval: '',
 processingMode: '',
 emptyFileHandling: '',
 enableDuplicateHandling: false,
 duplicateMessageAlertThreshold: '',
 disableChannelOnExceed: false,

 // Outbound-specific fields
 targetDirectory: '',
 targetFileName: '',
 fileConstructionMode: '',
 overwriteExistingFile: false,
 createFileDirectory: false,
 filePlacement: '',
 emptyMessageHandling_receiver: '',
 maxConcurrentConnections: '',

 // File Format Configuration
 fileFormatConfig: {
 fileFormat: 'TEXT'
 },

 // XML Conversion Configuration
 xmlConversion: {
 rootElementName: 'Message',
 encoding: 'UTF-8',
 includeXmlDeclaration: true,
 prettyPrint: true
 },

 // Transformation Configuration
 transformationConfig: {
 mode: 'passthrough',
 requiresStructure: false
 }
 });


 const updateConfig = (updates: Partial<FtpAdapterConfig>) => {
 const newConfig = { ...config, ...updates };
 setConfig(newConfig);
 onConfigChange?.(newConfig);
 };

 const addAdvancedEntry = () => {
 const newEntries = [...config.advancedEntries, { directory: '', fileName: '', exclusionMask: '' }];
 updateConfig({ advancedEntries: newEntries });
 };

 const updateAdvancedEntry = (index: number, field: keyof FileAccessAdvancedEntry, value: string) => {
 const newEntries = [...config.advancedEntries];
 newEntries[index] = { ...newEntries[index], [field]: value };
 updateConfig({ advancedEntries: newEntries });
 };

 const removeAdvancedEntry = (index: number) => {
 if (config.advancedEntries.length > 1) {
 const newEntries = config.advancedEntries.filter((_, i) => i !== index);
 updateConfig({ advancedEntries: newEntries });
 }
 };

 return (
 <Card className="w-full">
 <CardHeader>
 <CardTitle>FTP {mode === 'sender' ? 'Inbound' : 'Outbound'} Adapter Configuration</CardTitle>
 <CardDescription>Configure your FTP {mode} adapter settings</CardDescription>
 </CardHeader>
 <CardContent>
 <Tabs defaultValue={mode === 'sender' ? 'source' : 'target'} className="w-full">
 <TabsList className={`grid w-full ${mode === 'sender' ? 'grid-cols-5' : 'grid-cols-4'}`}>
 <TabsTrigger value={mode === 'sender' ? 'source' : 'target'}>
 {mode === 'sender' ? 'Source' : 'Target'}
 </TabsTrigger>
 <TabsTrigger value="processing">Processing</TabsTrigger>
 {mode === 'sender' && (
 <TabsTrigger value="transformation">Transformation</TabsTrigger>
 )}
 {mode === 'sender' && config.transformationConfig?.mode === 'transform' && (
 <TabsTrigger value="fileFormat">File Format</TabsTrigger>
 )}
 {mode === 'sender' && config.transformationConfig?.mode === 'transform' && (
 <TabsTrigger value="xmlConversion">XML Conversion</TabsTrigger>
 )}
 </TabsList>

 {/* Inbound Source Tab */}
 {mode === 'sender' && (
 <TabsContent value="source" className="space-y-6 mt-6">
 {/* File Access Parameters Section */}
 <div className="space-y-4">
 <div>
 <h3 className="text-lg font-semibold mb-4">File Access Parameters</h3>
 <Separator className="mb-4" />
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="sourceDirectory">Source Directory *</Label>
 <Input
 id="sourceDirectory"
 value={config.sourceDirectory}
 onChange={(e) => updateConfig({ sourceDirectory: e.target.value })}
 placeholder="Enter source directory path"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="fileName">File Name *</Label>
 <Input
 id="fileName"
 value={config.fileName}
 onChange={(e) => updateConfig({ fileName: e.target.value })}
 placeholder="Enter file name pattern"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="sorting">Sorting</Label>
 <Select value={config.sorting} onValueChange={(value) => updateConfig({ sorting: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select sorting option" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="none">None</SelectItem>
 <SelectItem value="timestamp-ascending">Timestamp Ascending</SelectItem>
 <SelectItem value="timestamp-descending">Timestamp Descending</SelectItem>
 <SelectItem value="file-size">File Size</SelectItem>
 <SelectItem value="file-name">File Name</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </div>

 <div className="space-y-4">
 <div className="flex items-center space-x-2">
 <Checkbox
 id="advancedSelection"
 checked={config.advancedSelection}
 onCheckedChange={(checked) => updateConfig({ advancedSelection: checked === true })}
 />
 <Label htmlFor="advancedSelection">Advanced selection for Source files</Label>
 </div>

 {config.advancedSelection && (
 <div className="space-y-4 p-4 border rounded-lg bg-muted/20">
 <div className="space-y-2">
 <Label htmlFor="exclusionMask">Exclusion Mask</Label>
 <Input
 id="exclusionMask"
 value={config.exclusionMask}
 onChange={(e) => updateConfig({ exclusionMask: e.target.value })}
 placeholder="Enter exclusion pattern (e.g., *.tmp, backup_*)"
 />
 </div>

 <div className="flex justify-between items-center">
 <Label className="text-sm font-medium">Multiple File Selection</Label>
 <button
 type="button"
 onClick={addAdvancedEntry}
 className="text-sm text-primary hover:underline"
 >
 + Add Entry
 </button>
 </div>

 {config.advancedEntries.map((entry, index) => (
 <div key={index} className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
 <div className="space-y-1">
 <Label className="text-xs">Directory</Label>
 <Input
 value={entry.directory}
 onChange={(e) => updateAdvancedEntry(index, 'directory', e.target.value)}
 placeholder="Directory path"
 className="text-sm"
 />
 </div>
 <div className="space-y-1">
 <Label className="text-xs">File Name</Label>
 <Input
 value={entry.fileName}
 onChange={(e) => updateAdvancedEntry(index, 'fileName', e.target.value)}
 placeholder="File name pattern"
 className="text-sm"
 />
 </div>
 <div className="space-y-1">
 <Label className="text-xs">Exclusion Mask</Label>
 <Input
 value={entry.exclusionMask}
 onChange={(e) => updateAdvancedEntry(index, 'exclusionMask', e.target.value)}
 placeholder="Exclusion pattern"
 className="text-sm"
 />
 </div>
 {config.advancedEntries.length > 1 && (
 <button
 type="button"
 onClick={() => removeAdvancedEntry(index)}
 className="text-xs text-destructive hover:underline self-end pb-2"
 >
 Remove
 </button>
 )}
 </div>
 ))}
 </div>
 )}
 </div>
 </div>

 {/* Connection Parameters Section */}
 <div className="space-y-4">
 <div>
 <h3 className="text-lg font-semibold mb-4">Connection Parameters</h3>
 <Separator className="mb-4" />
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="serverAddress">Server Address *</Label>
 <Input
 id="serverAddress"
 value={config.serverAddress}
 onChange={(e) => updateConfig({ serverAddress: e.target.value })}
 placeholder="Enter server address"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="port">Port *</Label>
 <Input
 id="port"
 value={config.port}
 onChange={(e) => updateConfig({ port: e.target.value })}
 placeholder="Enter port number"
 type="number"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="timeout">Timeout</Label>
 <Input
 id="timeout"
 value={config.timeout}
 onChange={(e) => updateConfig({ timeout: e.target.value })}
 placeholder="Timeout in seconds"
 type="number"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="connectionSecurity">Connection Security *</Label>
 <Select value={config.connectionSecurity} onValueChange={(value) => updateConfig({ connectionSecurity: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select connection security" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="explicit-ftps">Explicit FTPS</SelectItem>
 <SelectItem value="implicit-ftps">Implicit FTPS</SelectItem>
 <SelectItem value="plain-ftp">Plain FTP - no encryption</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="userName">User Name *</Label>
 <Input
 id="userName"
 value={config.userName}
 onChange={(e) => updateConfig({ userName: e.target.value })}
 placeholder="Enter username"
 />
 </div>

 <div className="md:col-span-2">
 <PasswordConfirmation
 name="password"
 label="Password"
 placeholder="Enter password"
 required={true}
 value={config.password}
 onValueChange={(value) => updateConfig({ password: value })}
 />
 </div>

 <div className="space-y-2 md:col-span-2">
 <Label htmlFor="connectionMode">Connection Mode *</Label>
 <Select value={config.connectionMode} onValueChange={(value) => updateConfig({ connectionMode: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select connection mode" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="permanently">Permanently</SelectItem>
 <SelectItem value="per-file-transfer">Per File Transfer</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </div>
 </div>
 </TabsContent>
 )}

 {/* Outbound Target Tab */}
 {mode === 'receiver' && (
 <TabsContent value="target" className="space-y-6 mt-6">
 {/* File Access Parameters Section */}
 <div className="space-y-4">
 <div>
 <h3 className="text-lg font-semibold mb-4">File Access Parameters</h3>
 <Separator className="mb-4" />
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="targetDirectory">Target Directory *</Label>
 <Input
 id="targetDirectory"
 value={config.targetDirectory}
 onChange={(e) => updateConfig({ targetDirectory: e.target.value })}
 placeholder="Enter target directory path"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="targetFileName">File Name *</Label>
 <Input
 id="targetFileName"
 value={config.targetFileName}
 onChange={(e) => updateConfig({ targetFileName: e.target.value })}
 placeholder="Enter file name"
 />
 </div>
 </div>
 </div>

 {/* Connection Parameters Section */}
 <div className="space-y-4">
 <div>
 <h3 className="text-lg font-semibold mb-4">Connection Parameters</h3>
 <Separator className="mb-4" />
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="serverAddress">Server Address *</Label>
 <Input
 id="serverAddress"
 value={config.serverAddress}
 onChange={(e) => updateConfig({ serverAddress: e.target.value })}
 placeholder="Enter server address"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="port">Port *</Label>
 <Input
 id="port"
 value={config.port}
 onChange={(e) => updateConfig({ port: e.target.value })}
 placeholder="Enter port number"
 type="number"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="timeout">Timeout</Label>
 <Input
 id="timeout"
 value={config.timeout}
 onChange={(e) => updateConfig({ timeout: e.target.value })}
 placeholder="Timeout in seconds"
 type="number"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="connectionSecurity">Connection Security *</Label>
 <Select value={config.connectionSecurity} onValueChange={(value) => updateConfig({ connectionSecurity: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select connection security" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="explicit-ftps">Explicit FTPS</SelectItem>
 <SelectItem value="implicit-ftps">Implicit FTPS</SelectItem>
 <SelectItem value="plain-ftp">Plain FTP - no encryption</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="userName">User Name *</Label>
 <Input
 id="userName"
 value={config.userName}
 onChange={(e) => updateConfig({ userName: e.target.value })}
 placeholder="Enter username"
 />
 </div>

 <div className="md:col-span-2">
 <PasswordConfirmation
 name="password"
 label="Password"
 placeholder="Enter password"
 required={true}
 value={config.password}
 onValueChange={(value) => updateConfig({ password: value })}
 />
 </div>

 <div className="space-y-2 md:col-span-2">
 <Label htmlFor="connectionMode">Connection Mode *</Label>
 <Select value={config.connectionMode} onValueChange={(value) => updateConfig({ connectionMode: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select connection mode" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="permanently">Permanently</SelectItem>
 <SelectItem value="per-file-transfer">Per File Transfer</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </div>
 </div>
 </TabsContent>
 )}

 <TabsContent value="processing" className="space-y-6 mt-6">
 {/* Processing Parameters Section */}
 <div className="space-y-4">
 <div>
 <h3 className="text-lg font-semibold mb-4">Processing Parameters</h3>
 <Separator className="mb-4" />
 </div>

 {mode === 'sender' ? (
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="pollingInterval">Polling Interval (Secs) *</Label>
 <Input
 id="pollingInterval"
 value={config.pollingInterval}
 onChange={(e) => updateConfig({ pollingInterval: e.target.value })}
 placeholder="Enter polling interval in seconds"
 type="number"
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="processingMode">Processing Mode *</Label>
 <Select value={config.processingMode} onValueChange={(value) => updateConfig({ processingMode: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select processing mode" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="archive">Archive</SelectItem>
 <SelectItem value="delete">Delete</SelectItem>
 <SelectItem value="test">Test</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2 md:col-span-2">
 <Label htmlFor="emptyFileHandling">Empty File Handling *</Label>
 <Select value={config.emptyFileHandling} onValueChange={(value) => updateConfig({ emptyFileHandling: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select empty file handling" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="do-not-create">Do not create message</SelectItem>
 <SelectItem value="process-empty">Process empty files</SelectItem>
 <SelectItem value="skip-empty">Skip empty files</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </div>
 ) : (
 <div className="space-y-6">
 <div className="space-y-4">
 <div className="flex items-center space-x-2">
 <Checkbox
 id="createFileDirectory"
 checked={config.createFileDirectory}
 onCheckedChange={(checked) => updateConfig({ createFileDirectory: checked === true })}
 />
 <Label htmlFor="createFileDirectory">Create File Directory</Label>
 </div>

 <div className="flex items-center space-x-2">
 <Checkbox
 id="overwriteExistingFile"
 checked={config.overwriteExistingFile}
 onCheckedChange={(checked) => updateConfig({ overwriteExistingFile: checked === true })}
 />
 <Label htmlFor="overwriteExistingFile">Overwrite Existing File</Label>
 </div>
 </div>

 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="fileConstructionMode">File Construction Mode *</Label>
 <Select value={config.fileConstructionMode} onValueChange={(value) => updateConfig({ fileConstructionMode: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select file construction mode" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="create">Create</SelectItem>
 <SelectItem value="append">Append</SelectItem>
 <SelectItem value="add-time-stamp">Add Time Stamp</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="filePlacement">File Placement *</Label>
 <Select value={config.filePlacement} onValueChange={(value) => updateConfig({ filePlacement: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select file placement" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="use-temporary-file">Use Temporary File</SelectItem>
 <SelectItem value="write-out-directly">Write Out Directly</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="emptyMessageHandling_receiver">Empty-Message Handling *</Label>
 <Select value={config.emptyMessageHandling_receiver} onValueChange={(value) => updateConfig({ emptyMessageHandling_receiver: value })}>
 <SelectTrigger>
 <SelectValue placeholder="Select empty message handling" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="write-out-empty-file">Write Out Empty File</SelectItem>
 <SelectItem value="ignore-empty-file">Ignore Empty File</SelectItem>
 </SelectContent>
 </Select>
 </div>

 <div className="space-y-2">
 <Label htmlFor="maxConcurrentConnections">Maximum Concurrent Connections</Label>
 <Input
 id="maxConcurrentConnections"
 value={config.maxConcurrentConnections}
 onChange={(e) => updateConfig({ maxConcurrentConnections: e.target.value })}
 placeholder="Enter maximum concurrent connections"
 type="number"
 />
 </div>
 </div>
 </div>
 )}
 </div>

 {/* Inbound-only Duplicate Handling Section */}
 {mode === 'sender' && (
 <div className="space-y-4">
 <div>
 <h3 className="text-lg font-semibold mb-4">Duplicate Handling</h3>
 <Separator className="mb-4" />
 </div>

 <div className="space-y-4">
 <div className="flex items-center space-x-2">
 <Checkbox
 id="enableDuplicateHandling"
 checked={config.enableDuplicateHandling}
 onCheckedChange={(checked) => updateConfig({ enableDuplicateHandling: checked === true })}
 />
 <Label htmlFor="enableDuplicateHandling">Enable Duplicate Handling</Label>
 </div>

 {config.enableDuplicateHandling && (
 <div className="space-y-4 p-4 border rounded-lg bg-muted/20">
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 <div className="space-y-2">
 <Label htmlFor="duplicateThreshold">Duplicate Message Alert Threshold *</Label>
 <Input
 id="duplicateThreshold"
 value={config.duplicateMessageAlertThreshold}
 onChange={(e) => updateConfig({ duplicateMessageAlertThreshold: e.target.value })}
 placeholder="Enter threshold value"
 type="number"
 />
 </div>
 </div>

 <div className="flex items-center space-x-2">
 <Checkbox
 id="disableChannelOnExceed"
 checked={config.disableChannelOnExceed}
 onCheckedChange={(checked) => updateConfig({ disableChannelOnExceed: checked === true })}
 />
 <Label htmlFor="disableChannelOnExceed">Disable Channel if Duplicate threshold has been exceeded</Label>
 </div>
 </div>
 )}
 </div>
 </div>
 )}
 </TabsContent>

 {/* Transformation Tab - Only for sender mode */}
 {mode === 'sender' && (
 <TabsContent value="transformation" className="mt-6">
 <TransformationToggle
 config={config.transformationConfig || { mode: 'passthrough' }}
 onChange={(transformationConfig) => updateConfig({ transformationConfig })}
 adapterType="FTP"
 disabled={false}
 />
 </TabsContent>
 )}

 {/* File Format Tab - Only show when transformation is enabled and in sender mode */}
 {mode === 'sender' && config.transformationConfig?.mode === 'transform' && (
 <TabsContent value="fileFormat" className="mt-6">
 <FileFormatTab
 config={config.fileFormatConfig || {}}
 onChange={(fileFormatConfig) => updateConfig({ fileFormatConfig })}
 />
 </TabsContent>
 )}

 {/* XML Conversion Tab - Only show when transformation is enabled and in sender mode */}
 {mode === 'sender' && config.transformationConfig?.mode === 'transform' && (
 <TabsContent value="xmlConversion" className="mt-6">
 <XmlConversionTab
 mode={mode === 'sender' ? 'INBOUND' : 'OUTBOUND'}
 config={config.xmlConversion || {}}
 onChange={(xmlConversion) => updateConfig({ xmlConversion })}
 />
 </TabsContent>
 )}
 </Tabs>
 </CardContent>
 </Card>
 );
};
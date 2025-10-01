import React, { useState, useEffect } from 'react';
import { Handle, Position, useReactFlow } from '@xyflow/react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Settings, Play, X } from 'lucide-react';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';
import { logger, LogCategory } from '@/lib/logger';

interface StartProcessNodeProps {
 id: string;
 data: {
 senderComponent?: string;
 inboundAdapter?: string;
 isAsync?: boolean;
 showDeleteButton?: boolean;
 payload?: any;
 onConfigChange: (config: any) => void;
 };
 selected?: boolean;
}

export const StartProcessNode: React.FC<StartProcessNodeProps> = ({ id, data }) => {
 const [configOpen, setConfigOpen] = useState(false);
 const { setNodes, setEdges } = useReactFlow();
 const [senderComponent, setInboundComponent] = useState(data.senderComponent || '');
 const [inboundAdapter, setInboundAdapter] = useState(data.inboundAdapter || '');
 const [isAsync, setIsAsync] = useState(data.isAsync ?? true); // Default to async
 const [availableAdapters, setAvailableAdapters] = useState<string[]>([]);

 const { businessComponents, getAdaptersForBusinessComponent } = useBusinessComponentAdapters();
 const [adapters, setAdapters] = useState<Array<{id: string; name: string}>>([]);

 const isConfigured = data.senderComponent && data.inboundAdapter;
 // Load all adapters on mount
 useEffect(() => {
 const loadAllAdapters = async () => {
    try {
 // For now, just create mock adapters based on IDs
 const mockAdapters = availableAdapters.map(id => ({
 id,
 name: id // Use ID as name for now
 }));
 setAdapters(mockAdapters);
 } catch (error) {
 logger.error(LogCategory.UI, 'Error loading adapters', { error: error });
 }
    };
 loadAllAdapters();
 }, [availableAdapters]);

 // Load available adapters when sender component changes
 useEffect(() => {
 const loadAdapters = async () => {
 if (senderComponent) {
 const adapterIds = await getAdaptersForBusinessComponent(senderComponent);
 setAvailableAdapters(adapterIds);
 } else {
 setAvailableAdapters([]);
 }
 };

 loadAdapters();
 }, [senderComponent, getAdaptersForBusinessComponent]);

 const handleDelete = () => {
 setNodes((nodes) => nodes.filter((node) => node.id !== id));
 setEdges((edges) => edges.filter((edge) => edge.source !== id && edge.target !== id));
 };

 const handleInboundComponentChange = async (value: string) => {
 setInboundComponent(value);
 // Reset adapter if it's not compatible with new component
 const newAvailableAdapters = await getAdaptersForBusinessComponent(value);
 const validAdapter = newAvailableAdapters.includes(inboundAdapter) ? inboundAdapter : '';
 if (!newAvailableAdapters.includes(inboundAdapter)) {
 setInboundAdapter('');
 }

 data.onConfigChange({
 senderComponent: value,
 inboundAdapter: validAdapter,
 isAsync,
 payload: data.payload
 });
 };

 const handleInboundAdapterChange = (value: string) => {
 setInboundAdapter(value);
 data.onConfigChange({
 senderComponent,
 inboundAdapter: value,
 isAsync,
 payload: data.payload
 });
 };

 const handleAsyncChange = (checked: boolean) => {
 setIsAsync(checked);
 data.onConfigChange({
 senderComponent,
 inboundAdapter,
 isAsync: checked,
 payload: data.payload
 });
 };

 return (
 <>
 <Card className="min-w-[200px] shadow-lg border-2 hover:border-primary/20 transition-colors bg-black text-white relative group">
 {/* Delete button - only visible on click */}
 {data.showDeleteButton && (
 <Button
 variant="ghost"
 size="sm"
 onClick={handleDelete}
 className="absolute -top-2 -right-2 h-6 w-6 p-0 bg-destructive text-destructive-foreground opacity-100 transition-opacity rounded-full shadow-md hover:bg-destructive/80"
 title="Delete start process"
 >
 <X className="h-3 w-3" />
 </Button>
 )}

 <CardHeader className="pb-2">
 <div className="flex items-center justify-between">
 <div className="flex items-center gap-2">
 <Play className="h-5 w-5 text-white" />
 <CardTitle className="text-sm font-medium text-white">Start Process</CardTitle>
 </div>
 <Badge variant={isConfigured ? "default" : "secondary"} className="text-xs bg-white text-black">
 {isConfigured ? "Configured" : "Setup Required"}
 </Badge>
 </div>
 </CardHeader>

 <CardContent className="pt-0">
 <Button
 size="sm"
 variant="outline"
 className="w-full bg-gray-800 text-white border-gray-700 hover:bg-gray-700"
 onClick={(e) => {
 e.stopPropagation();
 e.preventDefault();
 setConfigOpen(true);
 }}
 onMouseDown={(e) => e.stopPropagation()}
 >
 <Settings className="h-4 w-4 mr-2" />
 Configure
 </Button>
 </CardContent>

 {/* Only source handle - no target handle for start process */}
 <Handle
 type="source"
 position={Position.Right}
 className="w-3 h-3 bg-green-500 border-2 border-white"
 />
 </Card>

 {/* Configuration Dialog */}
 <Dialog
 open={configOpen}
 onOpenChange={setConfigOpen}
 >
 <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto z-[9999]" style={{ zIndex: 9999 }}>
 <DialogHeader>
 <DialogTitle>Configure Start Process</DialogTitle>
 </DialogHeader>
 <div className="mt-4 space-y-6">
 {/* Inbound Component Selection */}
 <div className="space-y-2">
 <Label htmlFor="sender-component">Inbound Component</Label>
 <Select value={senderComponent} onValueChange={handleInboundComponentChange}>
 <SelectTrigger>
 <SelectValue placeholder="Select a business component..." />
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

 {/* Source Adapter Selection */}
 <div className="space-y-2">
 <Label htmlFor="source-adapter">Source Adapter</Label>
 <Select
 value={inboundAdapter}
 onValueChange={handleInboundAdapterChange}
 disabled={!senderComponent}
 >
 <SelectTrigger>
 <SelectValue placeholder="Select an adapter..." />
 </SelectTrigger>
 <SelectContent>
 {availableAdapters.map((adapterId) => {
 const adapter = adapters.find((a) => a.id === adapterId);
 return adapter ? (
 <SelectItem key={adapter.id} value={adapter.id}>
 {adapter.name}
 </SelectItem>
 ) : (
 <SelectItem key={adapterId} value={adapterId}>
 {adapterId}
 </SelectItem>
 );
 })}
 </SelectContent>
 </Select>
 </div>

 {/* Execution Mode Selection */}
 <div className="space-y-2">
 <Label htmlFor="execution-mode">Execution Mode</Label>
 <div className="flex items-center justify-between p-3 border rounded-lg">
 <div className="space-y-1">
 <div className="font-medium text-sm">
 {isAsync ? 'Asynchronous' : 'Synchronous'}
 </div>
 <div className="text-xs text-muted-foreground">
 {isAsync
 ? 'Process continues without waiting for response'
 : 'Process waits for complete response before continuing'
 }
 </div>
 </div>
 <Switch
 checked={isAsync}
 onCheckedChange={handleAsyncChange}
 aria-label="Toggle execution mode"
 />
 </div>
 </div>

 {/* Payload Output Info */}
 {isConfigured && (
 <div className="bg-muted p-4 rounded-lg">
 <h4 className="font-medium text-sm mb-2">Output Payload</h4>
 <p className="text-sm text-muted-foreground">
 This start process will output the payload from the {senderComponent} component
 using the {adapters.find((a) => a.id === inboundAdapter)?.name || inboundAdapter} adapter.
 </p>
 </div>
 )}
 </div>
 </DialogContent>
 </Dialog>
 </>
 );
};
import React, { useState } from 'react';
import { Handle, Position, useReactFlow } from '@xyflow/react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Settings, Globe, Inbox, Workflow, RefreshCw, X, Play } from 'lucide-react';
import { AdapterConfigurationCard } from '@/components/createFlow/AdapterConfigurationCard';
import { logger, LogCategory } from '@/lib/logger';

interface AdapterNodeProps {
 id: string;
 data: {
 adapterType: string;
 adapterConfig: any;
 showDeleteButton?: boolean;
 onConfigChange: (config: any) => void;
 };
 selected?: boolean;
}

const getAdapterIcon = (type: string) => {
 switch (type) {
 case 'start-process':
 return Play;
 case 'http-sender':
 case 'http-receiver':
 return Globe;
 case 'soap-sender':
 case 'soap-receiver':
 return Workflow;
 case 'rest-sender':
 case 'rest-receiver':
 return RefreshCw;
 default:
 return Inbox;
 }
};

const getAdapterName = (type: string) => {
 if (type === 'start-process') return 'Start Process';
 return type.split('-').map(word =>
 word.charAt(0).toUpperCase() + word.slice(1)
 ).join(' ');
};

export const AdapterNode: React.FC<AdapterNodeProps> = ({ id, data, selected }) => {
 logger.info(LogCategory.UI, '[AdapterNode] Rendering node:' , { id, data });
 const [configOpen, setConfigOpen] = useState(false);
 const { setNodes, setEdges } = useReactFlow();
 const [sourceBusinessComponent, setSourceBusinessComponent] = useState(data.adapterConfig?.sourceBusinessComponent || '');
 const [targetBusinessComponent, setTargetBusinessComponent] = useState(data.adapterConfig?.targetBusinessComponent || '');
 const [inboundAdapter, setInboundAdapter] = useState(data.adapterType);
 const [outboundAdapter, setOutboundAdapter] = useState(data.adapterConfig?.outboundAdapter || '');
 const [inboundAdapterActive, setInboundAdapterActive] = useState(data.adapterConfig?.inboundAdapterActive || true);
 const [outboundAdapterActive, setOutboundAdapterActive] = useState(data.adapterConfig?.outboundAdapterActive || false);

 const Icon = getAdapterIcon(data.adapterType);
 const adapterName = getAdapterName(data.adapterType);
 const isConfigured = data.adapterConfig && Object.keys(data.adapterConfig).length > 0;

 const handleDelete = () => {
 setNodes((nodes) => nodes.filter((node) => node.id !== id));
 setEdges((edges) => edges.filter((edge) => edge.source !== id && edge.target !== id));
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
 title="Delete adapter"
 >
 <X className="h-3 w-3" />
 </Button>
 )}

 <CardHeader className="pb-2">
 <div className="flex items-center justify-between">
 <div className="flex items-center gap-2">
 <Icon className="h-5 w-5 text-white" />
 <CardTitle className="text-sm font-medium text-white">{adapterName}</CardTitle>
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
 logger.info(LogCategory.UI, '[AdapterNode] Configure button clicked:', { id, adapterType: data.adapterType });
 e.stopPropagation();
 e.preventDefault();
 try {
 setConfigOpen(true);
                logger.info(LogCategory.UI, '[AdapterNode] Dialog opened successfully'); 
              } catch (error) {
                logger.error(LogCategory.UI, '[AdapterNode] Error opening dialog:', error);
              }
            }}
 onMouseDown={(e) => {
 logger.info(LogCategory.UI, '[AdapterNode] Configure button mousedown');
 e.stopPropagation();
 }}
 >
 <Settings className="h-4 w-4 mr-2" />
 Configure
 </Button>
 </CardContent>

 {/* Connection handles */}
 <Handle
 type="target"
 position={Position.Left}
 className="w-3 h-3 bg-blue-500 border-2 border-white"
 />
 <Handle
 type="source"
 position={Position.Right}
 className="w-3 h-3 bg-green-500 border-2 border-white"
 />
 </Card>

 {/* Configuration Dialog */}
 <Dialog
 open={configOpen}
 onOpenChange={(open) => {
 logger.info(LogCategory.UI, '[AdapterNode] Dialog state changing:', { open, id });
 setConfigOpen(open);
 }}
 >
 <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto z-[9999]" style={{ zIndex: 9999 }}>
 <DialogHeader>
 <DialogTitle>Configure {adapterName}</DialogTitle>
 </DialogHeader>
 <div className="mt-4">
 {(() => {
 return (
 <AdapterConfigurationCard
 adapters={[{
 id: data.adapterType,
 name: adapterName,
 icon: Icon,
 category: data.adapterType.split('-')[0].toUpperCase()
 }]}
 sourceBusinessComponent={sourceBusinessComponent}
 targetBusinessComponent={targetBusinessComponent}
 inboundAdapter={inboundAdapter}
 outboundAdapter={outboundAdapter}
 inboundAdapterActive={inboundAdapterActive}
 outboundAdapterActive={outboundAdapterActive}
 onSourceBusinessComponentChange={(value) => {
 logger.info(LogCategory.UI, '[AdapterNode] Source business component changed:' , { data: value });
 setSourceBusinessComponent(value);
 }}
 onTargetBusinessComponentChange={(value) => {
 logger.info(LogCategory.UI, '[AdapterNode] Target business component changed:' , { data: value });
 setTargetBusinessComponent(value);
 }}
 onInboundAdapterChange={(value) => {
 logger.info(LogCategory.UI, '[AdapterNode] Source adapter changed:' , { data: value });
 setInboundAdapter(value);
 }}
 onOutboundAdapterChange={(value) => {
 logger.info(LogCategory.UI, '[AdapterNode] Target adapter changed:' , { data: value });
 setOutboundAdapter(value);
 }}
 onInboundAdapterActiveChange={(active) => {
 logger.info(LogCategory.UI, '[AdapterNode] Source adapter active changed:' , { data: active });
 setInboundAdapterActive(active);
 }}
 onOutboundAdapterActiveChange={(active) => {
 logger.info(LogCategory.UI, '[AdapterNode] Target adapter active changed:' , { data: active });
 setOutboundAdapterActive(active);
 }}
 />
 );
 })()}
 </div>
 </DialogContent>
 </Dialog>
 </>
 );
};
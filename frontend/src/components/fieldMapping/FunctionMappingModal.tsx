import React, { useState, useRef, useCallback } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { FieldNode, FieldMapping, FunctionNodeData } from './types';
import { functionsByCategory } from '@/services/transformationFunctions';
import { X, Check } from 'lucide-react';
import { cn } from '@/lib/utils';
import { logger, LogCategory } from '@/lib/logger';

interface FunctionMappingModalProps {
 open: boolean;
 onClose: () => void;
 selectedFunction: string;
 sourceFields: FieldNode[];
 targetField: FieldNode | null;
 onApplyMapping: (mapping: FieldMapping) => void;
}

interface Connection {
 id: string;
 sourceId: string;
 targetId: string;
 sourceX: number;
 sourceY: number;
 targetX: number;
 targetY: number;
}

interface DragState {
 isDragging: boolean;
 draggedItem: FieldNode | null;
 startPosition: { x: number; y: number };
 currentPosition: { x: number; y: number };
}

export const FunctionMappingModal: React.FC<FunctionMappingModalProps> = ({
 open,
 onClose,
 selectedFunction,
 sourceFields,
 targetField,
 onApplyMapping
}) => {
 const canvasRef = useRef<HTMLDivElement>(null);
 const svgRef = useRef<SVGSVGElement>(null);
 const [functionNode, setFunctionNode] = useState<FunctionNodeData>({
 id: 'function_modal',
 functionName: selectedFunction,
 parameters: {},
 sourceConnections: {},
 position: { x: 0, y: 0 }
 });

 const [connections, setConnections] = useState<Connection[]>([]);
 const [dragState, setDragState] = useState<DragState>({
 isDragging: false,
 draggedItem: null,
 startPosition: { x: 0, y: 0 },
 currentPosition: { x: 0, y: 0 }
 });

 const [dropTargets, setDropTargets] = useState<Set<string>>(new Set());
 const [outputConnected, setOutputConnected] = useState(false);

 const getAllFunctions = () => Object.values(functionsByCategory).flat();
 const func = getAllFunctions().find(f => f.name === selectedFunction);
 
 // Mouse-based drag implementation
 const handleMouseDown = useCallback((field: FieldNode, event: React.MouseEvent) => {
 event.preventDefault();
 event.stopPropagation();

 // Prevent text selection during drag
 document.body.style.userSelect = 'none';
 document.body.style.webkitUserSelect = 'none';

 logger.info(LogCategory.UI, '🎯 DRAG START - Field:', { data: field.name, Position: { x: event.clientX, y: event.clientY } });
 logger.info(LogCategory.UI, '🎯 DRAG START - Event target:', { data: event.target });
 logger.info(LogCategory.UI, '🎯 DRAG START - Event currentTarget:', { data: event.currentTarget });
 setDragState({
 isDragging: true,
 draggedItem: field,
 startPosition: { x: event.clientX, y: event.clientY },
 currentPosition: { x: event.clientX, y: event.clientY }
 });

 const targets = new Set<string>();
 func?.parameters.forEach(param => {
 if (!param.name.toLowerCase().includes('delimiter') && !param.name.toLowerCase().includes('separator')) {
 targets.add(`param-${functionNode.id}-${param.name}`);
 logger.info(LogCategory.UI, '🎯 DRAG START - Added drop target:', { data: `param-${functionNode.id}-${param.name}` });
 }
 });
 setDropTargets(targets);
 logger.info(LogCategory.UI, '🎯 DRAG START - Total drop targets:', { data: targets.size });
 }, [func, functionNode.id]);

 const handleMouseMove = useCallback((event: MouseEvent) => {
 if (dragState.isDragging) {
 logger.info(LogCategory.UI, '🎯 DRAG MOVE - Position:', { x: event.clientX, y: event.clientY });
 logger.info(LogCategory.UI, '🎯 DRAG MOVE - Element under mouse', { data: document.elementFromPoint(event.clientX, event.clientY) });
 setDragState(prev => ({
 ...prev,
 currentPosition: { x: event.clientX, y: event.clientY }
 }));
 }
 }, [dragState.isDragging]);

 const handleMouseUp = useCallback((event: MouseEvent) => {
 logger.info(LogCategory.UI, '🎯 DRAG END - Starting mouse up handler');
 logger.info(LogCategory.UI, '🎯 DRAG END - isDragging:', { data: dragState.isDragging });
 logger.info(LogCategory.UI, '🎯 DRAG END - draggedItem:', { data: dragState.draggedItem });
 if (!dragState.isDragging || !dragState.draggedItem) {
 logger.info(LogCategory.UI, '🎯 DRAG END - Early return - no drag in progress');
 return;
 }

 logger.info(LogCategory.UI, '🎯 DRAG END - Position:', { x: event.clientX, y: event.clientY });
 // Check if we're over a drop zone
 const elementUnderMouse = document.elementFromPoint(event.clientX, event.clientY);
 logger.info(LogCategory.UI, '🎯 DRAG END - Element under mouse:', { data: elementUnderMouse });
 logger.info(LogCategory.UI, '🎯 DRAG END - Element tag:', { data: elementUnderMouse?.tagName });
 logger.info(LogCategory.UI, '🎯 DRAG END - Element classes:', { data: elementUnderMouse?.className });
 logger.info(LogCategory.UI, '🎯 DRAG END - Element data-param:', { data: elementUnderMouse?.getAttribute('data-param') });

 const dropZone = elementUnderMouse?.closest('[data-param]');
 logger.info(LogCategory.UI, '🎯 DRAG END - Drop zone found:', { data: dropZone });
 logger.info(LogCategory.UI, '🎯 DRAG END - Drop zone data-param:', { data: dropZone?.getAttribute('data-param') });

 if (dropZone) {
 const paramName = dropZone.getAttribute('data-param');
 if (paramName) {
 logger.info(LogCategory.UI, '🎯 DRAG END - ✅ SUCCESS! Dropped on parameter:', { data: paramName });
 const sourceField = dragState.draggedItem;
 logger.info(LogCategory.UI, '🎯 DRAG END - Source field:', { data: sourceField });
 setFunctionNode(prev => {
 const newNode = {
 ...prev,
 sourceConnections: {
 ...prev.sourceConnections,
 [paramName]: [sourceField.path]
 }
 };
 logger.info(LogCategory.UI, '🎯 DRAG END - Updated function node:', { data: newNode });
 return newNode;
 });

 // Create visual connection
 setTimeout(() => {
 logger.info(LogCategory.UI, '🎯 DRAG END - Creating visual connection');
 const sourceElement = document.querySelector(`[data-field-id="${sourceField.id}"]`);
 const targetElement = dropZone;
 
 logger.info(LogCategory.UI, '🎯 DRAG END - Source element found:', { data: sourceElement });
 logger.info(LogCategory.UI, '🎯 DRAG END - Target element found:', { data: targetElement });
 logger.info(LogCategory.UI, '🎯 DRAG END - Canvas ref:', { data: canvasRef.current });
 if (sourceElement && targetElement && canvasRef.current) {
 const canvasRect = canvasRef.current.getBoundingClientRect();
 const sourceRect = sourceElement.getBoundingClientRect();
 const targetRect = targetElement.getBoundingClientRect();
 
 logger.info(LogCategory.UI, '🎯 DRAG END - Canvas rect:', { data: canvasRect });
 logger.info(LogCategory.UI, '🎯 DRAG END - Source rect:', { data: sourceRect });
 logger.info(LogCategory.UI, '🎯 DRAG END - Target rect:', { data: targetRect });
 const connection: Connection = {
 id: `connection_${Date.now()}`,
 sourceId: sourceField.id,
 targetId: `${functionNode.id}-${paramName}`,
 sourceX: sourceRect.right - canvasRect.left,
 sourceY: sourceRect.top + sourceRect.height / 2 - canvasRect.top,
 targetX: targetRect.left - canvasRect.left,
 targetY: targetRect.top + targetRect.height / 2 - canvasRect.top
 };
 logger.info(LogCategory.UI, '🎯 DRAG END - Connection created:', { data: connection });
 setConnections(prev => {
 const newConnections = [...prev, connection];
 logger.info(LogCategory.UI, '🎯 DRAG END - All connections:', { data: newConnections });
 return newConnections;
 });
 }
 }, 100);
 } else {
 logger.info(LogCategory.UI, '🎯 DRAG END - ❌ No param name found on drop');
 }
 } else {
 logger.info(LogCategory.UI, '🎯 DRAG END - ❌ No drop zone found');
 }

 // Reset user selection styles
 document.body.style.userSelect = '';
 document.body.style.webkitUserSelect = '';

 logger.info(LogCategory.UI, '🎯 DRAG END - Resetting drag state');
 setDragState({
 isDragging: false,
 draggedItem: null,
 startPosition: { x: 0, y: 0 },
 currentPosition: { x: 0, y: 0 }
 });
 setDropTargets(new Set());
 logger.info(LogCategory.UI, '🎯 DRAG END - Complete!');
 }, [dragState, functionNode.id]);

 // Add global mouse event listeners
 React.useEffect(() => {
 logger.info(LogCategory.UI, '🎯 DRAG STATE CHANGE - isDragging:', { data: dragState.isDragging });
 logger.info(LogCategory.UI, '🎯 DRAG STATE CHANGE - draggedItem:', { data: dragState.draggedItem?.name });
 if (dragState.isDragging) {
 logger.info(LogCategory.UI, '🎯 DRAG STATE CHANGE - Adding global mouse listeners');
 document.addEventListener('mousemove', handleMouseMove);
 document.addEventListener('mouseup', handleMouseUp);
 return () => {
 logger.info(LogCategory.UI, '🎯 DRAG STATE CHANGE - Removing global mouse listeners');
 document.removeEventListener('mousemove', handleMouseMove);
 document.removeEventListener('mouseup', handleMouseUp);
 };
 }
 }, [dragState.isDragging, dragState.draggedItem?.name, handleMouseMove, handleMouseUp]);

 const handleFunctionOutputDragStart = useCallback((event: React.DragEvent) => {
 if (!targetField) return;
 event.dataTransfer.setData('text/plain', 'function-output');
 event.dataTransfer.effectAllowed = 'move';

 const targets = new Set<string>();
 targets.add(`target-${targetField.id}`);
 setDropTargets(targets);
 }, [targetField]);

 const handleDragEnd = useCallback(() => {
 setDragState({
 isDragging: false,
 draggedItem: null,
 startPosition: { x: 0, y: 0 },
 currentPosition: { x: 0, y: 0 }
 });
 setDropTargets(new Set());
 }, []);

 const handleDropOnTarget = useCallback((event: React.DragEvent) => {
 event.preventDefault();
 const data = event.dataTransfer.getData('text/plain');
 if (data !== 'function-output' || !targetField) return;

 setOutputConnected(true);

 // Create visual connection with better positioning
 const outputElement = document.querySelector('[data-function-output="true"]');
 const targetElement = document.querySelector(`[data-target-field="${targetField.id}"]`);
 
 if (outputElement && targetElement && canvasRef.current) {
 const canvasRect = canvasRef.current.getBoundingClientRect();
 const outputRect = outputElement.getBoundingClientRect();
 const targetRect = targetElement.getBoundingClientRect();
 
 const connection: Connection = {
 id: `connection_output_${Date.now()}`,
 sourceId: `${functionNode.id}-output`,
 targetId: targetField.id,
 sourceX: outputRect.right - canvasRect.left,
 sourceY: outputRect.top + outputRect.height / 2 - canvasRect.top,
 targetX: targetRect.left - canvasRect.left,
 targetY: targetRect.top + targetRect.height / 2 - canvasRect.top
 };
 setConnections(prev => [...prev, connection]);
 }
 }, [functionNode.id, targetField]);

 const handleApplyFunction = useCallback(() => {
 if (!func || !outputConnected || !targetField) return;

 const connectedSourceFields: string[] = [];
 const connectedSourcePaths: string[] = [];

 Object.values(functionNode.sourceConnections).forEach(paths => {
 paths.forEach(path => {
 connectedSourcePaths.push(path);
 connectedSourceFields.push(path.split('.').pop() || path);
 });
 });

 const newMapping: FieldMapping = {
 id: `mapping_${Date.now()}`,
 name: `${selectedFunction}_to_${targetField.name}`,
 sourceFields: connectedSourceFields,
 targetField: targetField.name,
 sourcePaths: connectedSourcePaths,
 targetPath: targetField.path,
 functionNode: {
 ...functionNode,
 id: `function_${Date.now()}`
 },
 requiresTransformation: true
 };

 onApplyMapping(newMapping);
 onClose();
 }, [func, outputConnected, functionNode, selectedFunction, targetField, onApplyMapping, onClose]);

 if (!func) return null;

 return (
 <Dialog open={open} onOpenChange={onClose}>
 <DialogContent className="max-w-6xl h-[80vh] p-0">
 <DialogHeader className="p-6 pb-0">
 <DialogTitle className="flex items-center justify-between">
 <span>Function Mapping: {func.name}</span>
 <div className="flex items-center gap-2">
 <Button
 onClick={handleApplyFunction}
 disabled={!outputConnected || Object.keys(functionNode.sourceConnections).length === 0}
 className="bg-primary text-primary-foreground"
 >
 <Check className="h-4 w-4 mr-2" />
 Apply Function
 </Button>
 <Button variant="outline" onClick={onClose} className="text-muted-foreground hover:text-foreground">
 <X className="h-4 w-4 mr-2" />
 Cancel
 </Button>
 </div>
 </DialogTitle>
 </DialogHeader>

 <div ref={canvasRef} className="relative w-full flex-1 bg-background/50 overflow-hidden">
 <svg ref={svgRef} className="absolute inset-0 w-full h-full pointer-events-none z-10" style={{ overflow: 'visible' }}>
 <defs>
 <marker id="arrowhead-modal" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto" markerUnits="strokeWidth">
 <polygon points="0 0, 10 3.5, 0 7" fill="hsl(var(--primary))" />
 </marker>
 </defs>
 {connections.map(connection => (
 <path
 key={connection.id}
 d={`M ${connection.sourceX} ${connection.sourceY} C ${connection.sourceX + 100} ${connection.sourceY}, ${connection.targetX - 100} ${connection.targetY}, ${connection.targetX} ${connection.targetY}`}
 stroke="hsl(var(--primary))"
 strokeWidth="2"
 fill="none"
 markerEnd="url(#arrowhead-modal)"
 />
 ))}
 </svg>

 <div className="flex justify-between items-start px-4 pt-4 space-x-4">
 {/* Source Fields */}
 <div className="w-72 h-60 overflow-y-auto">
 <div className="bg-card border rounded-lg p-2 shadow-sm h-full">
 <h3 className="font-semibold text-sm mb-2 text-primary">Source Fields</h3>
 <div className="space-y-2">
 {sourceFields.map(field => (
 <div
 key={field.id}
 data-field-id={field.id}
 className={cn(
 "bg-background border-2 rounded p-3 transition-all hover:shadow-lg hover:border-primary/50 select-none",
 dragState.isDragging && dragState.draggedItem?.id === field.id && "opacity-70 scale-95"
 )}
 style={{
 cursor: dragState.isDragging && dragState.draggedItem?.id === field.id ? 'grabbing' : 'grab'
 }}
 onMouseDown={(e) => {
 e.preventDefault();
 handleMouseDown(field, e);
 }}
 >
 <div className="font-medium text-sm pointer-events-none">{field.name}</div>
 <div className="text-xs text-muted-foreground pointer-events-none">{field.type}</div>
 </div>
 ))}
 </div>
 </div>
 </div>

 {/* Function Node */}
 <div className="bg-card border-2 border-primary/20 rounded-lg shadow-lg overflow-visible w-60" style={{ zIndex: 5 }}>
 <div className="bg-primary text-primary-foreground p-2 rounded-t-lg">
 <div className="font-medium text-sm">{func.name}</div>
 <div className="text-xs opacity-80">{func.description}</div>
 </div>
 <div className="p-2 space-y-2">
 {func.parameters.map(param => (
 <div key={param.name}>
 <div className="font-medium text-xs">{param.name}</div>
 <div className="text-xs text-muted-foreground mb-1">{param.type}</div>

 {/* For delimiter or similar text parameters, show input field */}
 {(param.name.toLowerCase().includes('delimiter') || param.name.toLowerCase().includes('separator')) ? (
 <Input
 placeholder={`Enter ${param.name}`}
 value={functionNode.parameters[param.name] || ''}
 onChange={(e) => setFunctionNode(prev => ({
 ...prev,
 parameters: { ...prev.parameters, [param.name]: e.target.value }
 }))}
 className="h-8 text-xs"
 />
 ) : (
 <div
 data-param={param.name}
 className={cn(
 "border-2 border-dashed border-muted-foreground/30 rounded p-2 text-sm transition-colors min-h-10",
 dropTargets.has(`param-${functionNode.id}-${param.name}`) && "border-primary bg-primary/10",
 "hover:border-primary/50"
 )}
 >
 {functionNode.sourceConnections[param.name]?.map((path, idx) => (
 <div key={idx} className="text-xs bg-primary/10 text-primary rounded px-1 py-0.5 mt-1 inline-block mr-1">
 {path.split('.').pop()}
 </div>
 ))}
 {!functionNode.sourceConnections[param.name]?.length && (
 <div className="text-xs text-muted-foreground italic">Drop here</div>
 )}
 </div>
 )}
 </div>
 ))}
 </div>
 <div
 data-function-output="true"
 className="bg-success/10 border-t border-success/20 p-2 text-center cursor-grab hover:bg-success/20 transition-colors"
 draggable="true"
 onDragStart={handleFunctionOutputDragStart}
 onDragEnd={handleDragEnd}
 >
 <div className="text-xs font-medium text-success">Output</div>
 <div className="text-xs text-success/80">Drag to target</div>
 </div>
 </div>

 {/* Target Field */}
 {targetField && (
 <div className="w-72">
 <div className="bg-card border rounded-lg p-2 shadow-sm">
 <h3 className="font-semibold text-sm mb-2 text-primary">Target Field</h3>
 <div
 data-target-field={targetField.id}
 className={cn("bg-background border rounded p-3 transition-all", dropTargets.has(`target-${targetField.id}`) && "border-primary bg-primary/10", outputConnected && "border-success bg-success/10")}
 onDragOver={(e) => e.preventDefault()}
 onDrop={handleDropOnTarget}
 >
 <div className="font-medium text-sm">{targetField.name}</div>
 <div className="text-sm text-muted-foreground">{targetField.type}</div>
 {outputConnected && <div className="text-xs text-success mt-2 font-medium">✓ Function output connected</div>}
 </div>
 </div>
 </div>
 )}
 </div>

 </div>

 <div className="border-t bg-muted/30 py-1 px-2 text-xs">
 <div className="flex justify-between items-center">
 <div className="text-muted-foreground">Connect source → function → target</div>
 <div className="flex items-center gap-2">
 <span className={cn("px-1.5 py-0.5 rounded", Object.keys(functionNode.sourceConnections).length > 0 ? "bg-success/20 text-success" : "bg-muted text-muted-foreground")}>Params: {Object.keys(functionNode.sourceConnections).length}/{func.parameters.length}</span>
 <span className={cn("px-1.5 py-0.5 rounded", outputConnected ? "bg-success/20 text-success" : "bg-muted text-muted-foreground")}>Output: {outputConnected ? 'Connected' : 'Not connected'}</span>
 </div>
 </div>
 </div>
 </DialogContent>
 </Dialog>
 );
};

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { ArrowRight, X, Zap } from 'lucide-react';
import { FieldMapping, FieldNode } from './types';
import { FunctionMappingModal } from './FunctionMappingModal';
import { VisualFlowEditor } from './VisualFlowEditor';
import { logger, LogCategory } from '@/lib/logger';

interface MappingAreaProps {
 mappings: FieldMapping[];
 sourceFields?: FieldNode[];
 targetFields?: FieldNode[];
 onRemoveMapping: (mappingId: string) => void;

 onUpdateMapping?: (mappingId: string, updates: Partial<FieldMapping>) => void;
 onCreateMapping?: (mapping: FieldMapping) => void;
}

export function MappingArea({
 mappings,
 sourceFields = [],
 targetFields = [],
 onRemoveMapping,
 onUpdateMapping,
 onCreateMapping
}: MappingAreaProps) {
 const [functionMappingModal, setFunctionMappingModal] = useState<{
 open: boolean;
 selectedFunction: string;
 targetField: FieldNode | null;
 existingMappingId?: string;
 filteredSourceFields?: FieldNode[];
 }>({
 open: false,
 selectedFunction: '',
 targetField: null,
 filteredSourceFields: []
 });


 const [visualFlowEditor, setVisualFlowEditor] = useState<{
 open: boolean;
 targetField: FieldNode | null;
 existingMapping?: FieldMapping;
 }>({
 open: false,
 targetField: null
 });

 useEffect(() => {
 logger.info(LogCategory.UI, '🔍 Modal state changed', { data: functionMappingModal });
 }, [functionMappingModal]);

 useEffect(() => {
 logger.info(LogCategory.UI, '🔥 Visual flow editor state changed', { data: visualFlowEditor });
 }, [visualFlowEditor]);


 const handleOpenVisualFlowEditor = (mappingId: string) => {
 logger.info(LogCategory.UI, '🔥 handleOpenVisualFlowEditor called with mappingId', { data: mappingId });
 logger.info(LogCategory.UI, '🔥 Available mappings', { data: mappings });
 logger.info(LogCategory.UI, '🔥 Available targetFields', { data: targetFields });
 logger.info(LogCategory.UI, '🔥 Target fields names', { data: targetFields.map(f => f?.name) })
 ;
 const existingMapping = mappings.find(m => m.id === mappingId);
 logger.info(LogCategory.UI, '🔥 Found existing mapping', { data: existingMapping });
 if (!existingMapping) {
 logger.info(LogCategory.UI, '❌ No existing mapping found for ID', { data: mappingId });
 return;
 }

 // First try to find by exact name match
 let targetField = targetFields.find(field => field?.name === existingMapping.targetField);
 // If not found, try to find by path match
 if (!targetField && existingMapping.targetPath) {
 targetField = targetFields.find(field => field?.path === existingMapping.targetPath);
 }

 // If still not found, search recursively through all fields including nested ones
 if (!targetField) {
 const findFieldRecursively = (fields: FieldNode[], targetName: string, targetPath?: string): FieldNode | undefined => {
 for (const field of fields) {
 if (field.name === targetName || (targetPath && field.path === targetPath)) {
 return field;
 }
 if (field.children && field.children.length > 0) {
 const found = findFieldRecursively(field.children, targetName, targetPath);
 if (found) return found;
 }
 }
 return undefined;
 };

 targetField = findFieldRecursively(targetFields, existingMapping.targetField, existingMapping.targetPath);
 }

 logger.info(LogCategory.UI, '🔥 Looking for target field with name', { data: existingMapping.targetField });
 logger.info(LogCategory.UI, '🔥 Looking for target field with path', { data: existingMapping.targetPath });
 logger.info(LogCategory.UI, '🔥 Found target field', { data: targetField });
 if (!targetField) {
 logger.info(LogCategory.UI, '❌ Target field not found, attempting to create a proper target field from mapping data');
 // Extract the actual field name from the path if available
 let fieldName = existingMapping.targetField;
 const fieldPath = existingMapping.targetPath || existingMapping.targetField;
 // If the targetPath contains dots, extract the last part as the field name
 if (existingMapping.targetPath && existingMapping.targetPath.includes('.')) {
 const pathParts = existingMapping.targetPath.split('.');
 fieldName = pathParts[pathParts.length - 1];
 }

 // Create a proper target field using the extracted information
 const properTargetField: FieldNode = {
 id: `target_${fieldName}_${Date.now()}`,
 name: fieldName,
 type: 'string', // Default type
 path: fieldPath,
 children: [],
 expanded: false
 };

 logger.info(LogCategory.UI, '🔥 Created proper target field', { data: properTargetField });
 setVisualFlowEditor({
 open: true,
 targetField: properTargetField,
 existingMapping
 });
 return;
 }

 logger.info(LogCategory.UI, 'Debug info', { message: '✅ Opening visual flow editor with', targetField: targetField.name,
 existingMapping: existingMapping.id
 });

 setVisualFlowEditor({ open: true,
  targetField: targetField,
 existingMapping
 });
 };

 const handleApplyVisualFlow = (newMapping: FieldMapping) => {
 logger.info(LogCategory.UI, '🔥 handleApplyVisualFlow called with', { data: newMapping });
 if (visualFlowEditor.existingMapping) {
 // Update existing mapping - include ALL fields, especially visualFlowData
 if (onUpdateMapping) {
 onUpdateMapping(visualFlowEditor.existingMapping.id, {
 functionNode: newMapping.functionNode,
 visualFlowData: newMapping.visualFlowData, // This was missing!
 requiresTransformation: true,
 sourceFields: newMapping.sourceFields,
 sourcePaths: newMapping.sourcePaths,
 name: newMapping.name
 });
 }
 } else {
 // Create new mapping
 if (onCreateMapping) {
 onCreateMapping(newMapping);
 }
 }

 // Close the editor
 setVisualFlowEditor({
 open: false,
 targetField: null
 });
 };

 const handleApplyFunctionMapping = (newMapping: FieldMapping) => {
 if (functionMappingModal.existingMappingId) {
 // Update existing mapping
 if (onUpdateMapping) {
 onUpdateMapping(functionMappingModal.existingMappingId, {
 functionNode: newMapping.functionNode,
 requiresTransformation: true,
 sourceFields: newMapping.sourceFields,
 sourcePaths: newMapping.sourcePaths
 });
 }
 } else {
 // Create new mapping
 if (onCreateMapping) {
 onCreateMapping(newMapping);
 }
 }

 // Close the modal
 setFunctionMappingModal({
 open: false,
 selectedFunction: '',
 targetField: null,
 filteredSourceFields: []
 });
 };

 // Debug logging for render
 logger.info(LogCategory.UI, '🔍 MappingArea render - mappings count', { data: mappings.length });
 logger.info(LogCategory.UI, '🔍 MappingArea render - mappings', { data: mappings });
 return (
 <div className="w-1/3 relative bg-background animate-fade-in">
 <div className="p-4 border-b">
 <h3 className="font-semibold flex items-center gap-2">
 <ArrowRight className="h-4 w-4" />
 Field Mappings ({mappings.length})
 </h3>
 </div>

 <div className="p-4 h-[calc(100%-60px)] overflow-y-auto">
 {mappings.length === 0 ? (
 <Alert className="mt-8">
 <AlertDescription>
 Drag fields from source to target to create mappings
 </AlertDescription>
 </Alert>
 ) : (
 <div className="space-y-3">
 {mappings.map(mapping => (
 <div key={mapping.id} className="p-3 border rounded-lg bg-muted/30 animate-scale-in">
 <div className="flex items-center justify-between mb-2">
 <div className="flex items-center gap-2">
 <Badge variant="outline" className="text-xs">
 {mapping.sourceFields && mapping.sourceFields.length > 1 ? 'Multi-Source Mapping' : 'Mapping'}
 </Badge>
 {mapping.requiresTransformation === false && (
 <Badge variant="secondary" className="text-xs">
 Pass-through
 </Badge>
 )}
 {mapping.functionNode && (
 <Badge
 variant={mapping.functionNode.functionName === 'nodeMapping' ? "secondary" : "default"}
 className="text-xs"
 >
 {mapping.functionNode.functionName === 'nodeMapping'
 ? `Node: ${mapping.functionNode.parameters?.sourceType || 'object'} → ${mapping.functionNode.parameters?.targetType || 'object'}`
 : `Function: ${mapping.functionNode.functionName}`
 }
 </Badge>)}
 </div>
 <div className="flex gap-1">
 {mapping.requiresTransformation !== false && (
 <>
 <Button
 variant="ghost"
 size="sm"
 onClick={() => {
 logger.info(LogCategory.UI, '🔥 Lightning bolt clicked for mapping', { data: mapping.id });
 handleOpenVisualFlowEditor(mapping.id);
 }}
 className="h-6 w-6 p-0 hover-scale"
 title="Open Visual Flow Editor"
 >
 <Zap className="h-3 w-3" />
 </Button>
 </>
 )}
 <Button
 variant="ghost"
 size="sm"
 onClick={() => onRemoveMapping(mapping.id)}
 className="h-6 w-6 p-0 text-destructive hover:text-destructive hover-scale"
 title="Remove Mapping"
 >
 <X className="h-3 w-3" />
 </Button>
 </div>
 </div>

 <div className="text-xs space-y-1">
 <div className="flex items-center gap-2">
 <span className="font-medium text-primary">{mapping.name}</span>
 </div>
 <div className="flex items-center gap-2">
 <span className="font-medium">Source:</span>
 <span className="text-muted-foreground">
 {mapping.sourceFields ? mapping.sourceFields.join(', ') : ''}
 </span>
 </div>
 <div className="flex items-center gap-2">
 <ArrowRight className="h-3 w-3 text-primary" />
 <span className="font-medium">Target:</span>
 <span className="text-muted-foreground">{mapping.targetField}</span>
 </div>
 {mapping.requiresTransformation !== false && (
 <>
 {mapping.functionNode && (
 <div className="mt-2 p-2 bg-primary/10 rounded text-xs">
 <div className="flex items-center gap-2 mb-1">
 <Zap className="h-3 w-3 text-primary" />
 <span className="font-medium text-primary">Function Mapping:</span>
 </div>
 <div className="text-muted-foreground">
 {mapping.functionNode.functionName === 'visual_flow' ? (
 mapping.visualFlowData ? (
 `Visual Flow - ${mapping.sourceFields ? mapping.sourceFields.length : 0} source field(s): ${mapping.sourceFields ? mapping.sourceFields.join(', ') : ''}`
 ) : (
 `Visual Flow - ${mapping.sourceFields ? mapping.sourceFields.length : 0} source field(s) connected`
 )
 ) : (
 `${mapping.functionNode.functionName} with ${Object.keys(mapping.functionNode.sourceConnections).length} parameter(s) connected`
 )}
 </div>
 </div>
 )}
 {!mapping.functionNode && (
 <div className="mt-2 text-xs text-muted-foreground italic">
 Click the lightning icon to configure transformations using the visual flow editor
 </div>
 )}
 </>
 )}
 {mapping.requiresTransformation === false && (
 <div className="mt-2 text-xs text-muted-foreground italic">
 Direct field mapping - no transformation required
 </div>
 )}
 </div>
 </div>
 ))}
 </div>
 )}
 </div>

 {/* Function Mapping Modal */}
 {functionMappingModal.open && functionMappingModal.targetField && (
 <FunctionMappingModal
 open={functionMappingModal.open}
 onClose={() => setFunctionMappingModal({
 open: false,
 selectedFunction: '',
 targetField: null,
 filteredSourceFields: []
 })}
 selectedFunction={functionMappingModal.selectedFunction}
 sourceFields={functionMappingModal.filteredSourceFields || sourceFields}
 targetField={functionMappingModal.targetField}
 onApplyMapping={handleApplyFunctionMapping}
 />
 )}

 {/* Visual Flow Editor */}
 {visualFlowEditor.open && visualFlowEditor.targetField && (
 <VisualFlowEditor
 open={visualFlowEditor.open}
 onClose={() => setVisualFlowEditor({
 open: false,
 targetField: null
 })}
 sourceFields={sourceFields}
 targetField={visualFlowEditor.targetField}
 onApplyMapping={handleApplyVisualFlow}
 initialMapping={visualFlowEditor.existingMapping}
 />
 )}
 </div>
 );
}
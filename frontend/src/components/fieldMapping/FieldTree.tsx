import { useCallback } from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Link2, ChevronDown, ChevronRight } from 'lucide-react';
import { FieldNode, FieldMapping } from './types';

interface FieldTreeProps {
 fields: FieldNode[];
 mappings: FieldMapping[];
 side: 'source' | 'target';
 selectedField?: FieldNode | null;
 onToggleExpanded: (nodeId: string, isSource: boolean) => void;
 onDragStart?: (field: FieldNode) => void;
 onDragEnd?: () => void;
 onDragOver?: (e: React.DragEvent) => void;
 onDrop?: (field: FieldNode) => void;
 onSelectField?: (field: FieldNode) => void;
}

export function FieldTree({
 fields,
 mappings,
 side,
 selectedField,
 onToggleExpanded,
 onDragStart,
 onDragEnd,
 onDragOver,
 onDrop,
 onSelectField
}: FieldTreeProps) {
 const renderField = useCallback((field: FieldNode, level: number): JSX.Element => {
 const isLeaf = !field.children || field.children.length === 0;
 const isArray = field.type === 'array' || field.name.endsWith('[]');
 const isNodeMappable = !isLeaf && (isArray || field.type === 'object');
 const isDraggable = side === 'source' && (isLeaf || isNodeMappable);
 const isDroppable = side === 'target' && (isLeaf || isNodeMappable);
 const isMapped = mappings.some(m => {
 if (side === 'source') {
 // Check both sourcePaths and sourceFields for compatibility
 const inSourcePaths = m.sourcePaths && m.sourcePaths.includes(field.path);
 const inSourceFields = m.sourceFields && m.sourceFields.includes(field.name);
 return inSourcePaths || inSourceFields;
 } else {
 // For target, check both targetPath and targetField
 return m.targetPath === field.path || m.targetField === field.name;
 }
 });

 const isSelected = selectedField?.id === field.id;
;
 return (
 <div key={field.id} className="w-full">
 <div
 className={`flex items-center p-2 border rounded-md transition-colors ${
 isSelected ? 'bg-primary/20 border-primary ring-1 ring-primary' :
 isMapped ? 'bg-primary/10 border-primary border-dotted border-2' : 'bg-background hover:bg-muted/50'
 } ${isDraggable ? 'cursor-grab active:cursor-grabbing' : ''} ${
 isDroppable && !isMapped ? 'border-dashed border-2 hover:border-primary cursor-pointer' : ''
 }`}
 style={{ marginLeft: `${level * 16}px` }}
 draggable={isDraggable}
 onClick={() => {
 if (onSelectField) {
 onSelectField(field);
 }
 }}
 onDragStart={(e) => {
 if (isDraggable && onDragStart) {
 e.dataTransfer.effectAllowed = 'copy';
 e.dataTransfer.setData('application/json', JSON.stringify(field));
 onDragStart(field);
 }
 }}
 onDragEnd={(e) => {
 if (isDraggable && onDragEnd) {
 onDragEnd();
 }
 }}
 onDragOver={isDroppable ? onDragOver : undefined}
 onDrop={(e) => {
 if (isDroppable) {
 e.preventDefault();
 e.stopPropagation();
 onDrop?.(field);
 }
 }}
 >
 {!isLeaf && (
 <Button
 variant="ghost"
 size="sm"
 onClick={() => onToggleExpanded(field.id, side === 'source')}
 className="mr-2 h-auto w-auto p-1 hover:bg-muted rounded"
 >
 {field.expanded ? <ChevronDown className="h-3 w-3" /> : <ChevronRight className="h-3 w-3" />}
 </Button>
 )}

 <div className="flex-1 flex items-center gap-2">
 <span className="font-medium text-sm">{field.name}</span>
 <Badge variant="outline" className="text-xs">{field.type}</Badge>
 {isMapped && <Link2 className="h-3 w-3 text-primary" />}
 {isNodeMappable && (
 <Badge variant="secondary" className="text-xs">Node</Badge>
 )}
 </div>

 {side === 'source' && isDraggable && (
 <span className="text-xs text-muted-foreground">Drag →</span>
 )}
 {side === 'target' && isDroppable && (
 <span className="text-xs text-muted-foreground">Drop here</span>
 )}
 </div>

 {!isLeaf && field.expanded && field.children && (
 <div className="ml-4">
 {field.children.map(child => renderField(child, level + 1))}
 </div>
 )}
 </div>
 );
 }, [mappings, side, selectedField, onToggleExpanded, onDragStart, onDragEnd, onDragOver, onDrop, onSelectField]);

 return (
 <div className="space-y-2">
 {fields.map(field => renderField(field, 0))}
 </div>
 );
};
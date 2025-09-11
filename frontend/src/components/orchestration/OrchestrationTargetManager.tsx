import React, { useState, useCallback, useEffect } from 'react';
import { DndProvider, useDrag, useDrop, DropTargetMonitor } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Plus, 
  Trash2, 
  GripVertical, 
  Settings, 
  Play,
  Pause,
  AlertCircle,
  CheckCircle2,
  Clock,
  RefreshCw,
  Zap,
  GitBranch,
  Copy,
  Eye,
  EyeOff
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { OrchestrationTargetService } from '@/services/orchestrationTargetService';
import { RoutingRuleBuilder } from './RoutingRuleBuilder';
import { useToast } from '@/hooks/use-toast';

interface OrchestrationTargetDto {
  id: string;
  flowId: string;
  targetAdapterId: string;
  executionOrder: number;
  parallel: boolean;
  conditionType: 'ALWAYS' | 'EXPRESSION' | 'HEADER_MATCH' | 'CONTENT_TYPE' | 'XPATH' | 'JSONPATH' | 'REGEX' | 'CUSTOM';
  routingCondition?: string;
  structureId?: string;
  responseStructureId?: string;
  retryAttempts?: number;
  retryDelayMs?: number;
  timeoutMs?: number;
  errorStrategy?: 'FAIL' | 'CONTINUE' | 'COMPENSATE' | 'RETRY';
  compensationAdapterId?: string;
  active: boolean;
  metadata?: Record<string, any>;
  adapterName?: string;
  adapterType?: string;
}

interface DragItem {
  id: string;
  index: number;
  type: 'target';
}

interface OrchestrationTargetItemProps {
  target: OrchestrationTargetDto;
  index: number;
  onMove: (dragIndex: number, hoverIndex: number) => void;
  onEdit: (target: OrchestrationTargetDto) => void;
  onDelete: (id: string) => void;
  onToggleActive: (id: string, active: boolean) => void;
  onDuplicate: (target: OrchestrationTargetDto) => void;
}

const OrchestrationTargetItem: React.FC<OrchestrationTargetItemProps> = ({
  target,
  index,
  onMove,
  onEdit,
  onDelete,
  onToggleActive,
  onDuplicate
}) => {
  const ref = React.useRef<HTMLDivElement>(null);
  
  const [{ handlerId }, drop] = useDrop({
    accept: 'target',
    collect(monitor) {
      return {
        handlerId: monitor.getHandlerId(),
      };
    },
    hover(item: DragItem, monitor: DropTargetMonitor) {
      if (!ref.current) {
        return;
      }
      const dragIndex = item.index;
      const hoverIndex = index;
      
      if (dragIndex === hoverIndex) {
        return;
      }
      
      const hoverBoundingRect = ref.current?.getBoundingClientRect();
      const hoverMiddleY = (hoverBoundingRect.bottom - hoverBoundingRect.top) / 2;
      const clientOffset = monitor.getClientOffset();
      const hoverClientY = (clientOffset as any).y - hoverBoundingRect.top;
      
      if (dragIndex < hoverIndex && hoverClientY < hoverMiddleY) {
        return;
      }
      if (dragIndex > hoverIndex && hoverClientY > hoverMiddleY) {
        return;
      }
      
      onMove(dragIndex, hoverIndex);
      item.index = hoverIndex;
    },
  });

  const [{ isDragging }, drag] = useDrag({
    type: 'target',
    item: () => {
      return { id: target.id, index };
    },
    collect: (monitor: any) => ({
      isDragging: monitor.isDragging(),
    }),
  });

  drag(drop(ref));
  
  const getConditionIcon = () => {
    switch (target.conditionType) {
      case 'ALWAYS': return <CheckCircle2 className="h-4 w-4" />;
      case 'EXPRESSION': return <GitBranch className="h-4 w-4" />;
      case 'XPATH': return <></>;
      case 'JSONPATH': return <></>;
      case 'REGEX': return <></>;
      default: return <AlertCircle className="h-4 w-4" />;
    }
  };

  return (
    <div
      ref={ref}
      className={cn(
        "transition-opacity",
        isDragging && "opacity-50"
      )}
      data-handler-id={handlerId}
    >
      <Card className={cn(
        "relative",
        !target.active && "opacity-60"
      )}>
        <CardContent className="p-4">
          <div className="flex items-center gap-4">
            <div className="cursor-move">
              <GripVertical className="h-5 w-5 text-muted-foreground" />
            </div>
            
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <span className="font-medium text-lg">#{target.executionOrder + 1}</span>
                <h4 className="font-semibold">{target.adapterName || 'Unknown Adapter'}</h4>
                {target.parallel && (
                  <Badge variant="secondary" className="gap-1">
                    <Zap className="h-3 w-3" />
                    Parallel
                  </Badge>
                )}
              </div>
              
              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                <div className="flex items-center gap-1">
                  {getConditionIcon()}
                  <span>{target.conditionType}</span>
                </div>
                {target.retryAttempts && target.retryAttempts > 0 && (
                  <div className="flex items-center gap-1">
                    <RefreshCw className="h-3 w-3" />
                    <span>{target.retryAttempts} retries</span>
                  </div>
                )}
                {target.timeoutMs && (
                  <div className="flex items-center gap-1">
                    <Clock className="h-3 w-3" />
                    <span>{target.timeoutMs / 1000}s timeout</span>
                  </div>
                )}
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <Switch
                checked={target.active}
                onCheckedChange={(checked) => onToggleActive(target.id, checked)}
              />
              <Button
                variant="ghost"
                size="icon"
                onClick={() => onDuplicate(target)}
                title="Duplicate target"
              >
                <Copy className="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => onEdit(target)}
              >
                <Settings className="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => onDelete(target.id)}
                className="text-destructive"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          </div>
          
          {target.routingCondition && target.conditionType !== 'ALWAYS' && (
            <div className="mt-3 p-2 bg-muted rounded-md font-mono text-xs">
              {target.routingCondition}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

interface OrchestrationTargetManagerProps {
  flowId: string;
  availableAdapters: Array<{ id: string; name: string; type: string }>;
  onTargetsChange?: (targets: OrchestrationTargetDto[]) => void;
  className?: string;
}

export function OrchestrationTargetManager({
  flowId,
  availableAdapters,
  onTargetsChange,
  className
}: OrchestrationTargetManagerProps) {
  const { toast } = useToast();
  const [targets, setTargets] = useState<OrchestrationTargetDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingTarget, setEditingTarget] = useState<OrchestrationTargetDto | null>(null);
  const [showAddDialog, setShowAddDialog] = useState(false);

  const loadTargets = useCallback(async () => {
    try {
      setLoading(true);
      const response = await OrchestrationTargetService.getTargets(flowId);
      setTargets(response);
      onTargetsChange?.(response);
    } catch (error) {
      console.error('Failed to load targets:', error);
      toast({
        title: 'Error',
        description: 'Failed to load orchestration targets',
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  }, [flowId, onTargetsChange, toast]);

  // Load targets on mount
  useEffect(() => {
    loadTargets();
  }, [loadTargets]);

  const moveTarget = useCallback(async (dragIndex: number, hoverIndex: number) => {
    const dragTarget = targets[dragIndex];
    const updatedTargets = [...targets];
    
    // Remove the dragged target
    updatedTargets.splice(dragIndex, 1);
    // Insert it at the new position
    updatedTargets.splice(hoverIndex, 0, dragTarget);
    
    // Update execution orders
    const reorderedTargets = updatedTargets.map((target, index) => ({
      ...target,
      executionOrder: index
    }));
    
    setTargets(reorderedTargets);
    
    // Save the new order to backend
    try {
      const targetIds = reorderedTargets.map(t => t.id);
      await OrchestrationTargetService.reorderTargets(flowId, targetIds);
      onTargetsChange?.(reorderedTargets);
    } catch (error) {
      console.error('Failed to reorder targets:', error);
      toast({
        title: 'Error',
        description: 'Failed to save new order',
        variant: 'destructive'
      });
      // Reload to get correct order
      loadTargets();
    }
  }, [targets, flowId, toast, onTargetsChange, loadTargets]);

  const handleAddTarget = async (adapterId: string) => {
    try {
      const adapter = availableAdapters.find(a => a.id === adapterId);
      if (!adapter) return;

      const newTarget: Partial<OrchestrationTargetDto> = {
        targetAdapterId: adapterId,
        executionOrder: targets.length,
        parallel: false,
        conditionType: 'ALWAYS',
        active: true,
        retryAttempts: 0,
        timeoutMs: 30000,
        errorStrategy: 'FAIL'
      };

      const created = await OrchestrationTargetService.addTarget(flowId, newTarget);
      const targetsWithAdapter = {
        ...created,
        adapterName: adapter.name,
        adapterType: adapter.type
      };
      
      const updatedTargets = [...targets, targetsWithAdapter];
      setTargets(updatedTargets);
      onTargetsChange?.(updatedTargets);
      
      toast({
        title: 'Success',
        description: 'Target adapter added successfully'
      });
      
      setShowAddDialog(false);
    } catch (error) {
      console.error('Failed to add target:', error);
      toast({
        title: 'Error',
        description: 'Failed to add target adapter',
        variant: 'destructive'
      });
    }
  };

  const handleUpdateTarget = async (target: OrchestrationTargetDto) => {
    try {
      const updated = await OrchestrationTargetService.updateTarget(flowId, target.id, target);
      const updatedTargets = targets.map(t => t.id === target.id ? 
        { ...updated, adapterName: t.adapterName, adapterType: t.adapterType } : t
      );
      setTargets(updatedTargets);
      onTargetsChange?.(updatedTargets);
      
      toast({
        title: 'Success',
        description: 'Target updated successfully'
      });
      
      setEditingTarget(null);
    } catch (error) {
      console.error('Failed to update target:', error);
      toast({
        title: 'Error',
        description: 'Failed to update target',
        variant: 'destructive'
      });
    }
  };

  const handleDeleteTarget = async (targetId: string) => {
    if (!confirm('Are you sure you want to delete this target?')) return;

    try {
      await OrchestrationTargetService.deleteTarget(flowId, targetId);
      const updatedTargets = targets
        .filter(t => t.id !== targetId)
        .map((t, index) => ({ ...t, executionOrder: index }));
      
      setTargets(updatedTargets);
      onTargetsChange?.(updatedTargets);
      
      toast({
        title: 'Success',
        description: 'Target deleted successfully'
      });
    } catch (error) {
      console.error('Failed to delete target:', error);
      toast({
        title: 'Error',
        description: 'Failed to delete target',
        variant: 'destructive'
      });
    }
  };

  const handleToggleActive = async (targetId: string, active: boolean) => {
    try {
      if (active) {
        await OrchestrationTargetService.activateTarget(flowId, targetId);
      } else {
        await OrchestrationTargetService.deactivateTarget(flowId, targetId);
      }
      
      const updatedTargets = targets.map(t => 
        t.id === targetId ? { ...t, active } : t
      );
      setTargets(updatedTargets);
      onTargetsChange?.(updatedTargets);
      
      toast({
        title: 'Success',
        description: `Target ${active ? 'activated' : 'deactivated'} successfully`
      });
    } catch (error) {
      console.error('Failed to toggle target:', error);
      toast({
        title: 'Error',
        description: `Failed to ${active ? 'activate' : 'deactivate'} target`,
        variant: 'destructive'
      });
    }
  };

  const handleDuplicateTarget = async (target: OrchestrationTargetDto) => {
    try {
      const duplicate: Partial<OrchestrationTargetDto> = {
        ...target,
        id: undefined,
        executionOrder: targets.length,
        active: false // Start duplicated targets as inactive
      };
      
      const created = await OrchestrationTargetService.addTarget(flowId, duplicate);
      const targetWithAdapter = {
        ...created,
        adapterName: target.adapterName,
        adapterType: target.adapterType
      };
      
      const updatedTargets = [...targets, targetWithAdapter];
      setTargets(updatedTargets);
      onTargetsChange?.(updatedTargets);
      
      toast({
        title: 'Success',
        description: 'Target duplicated successfully'
      });
    } catch (error) {
      console.error('Failed to duplicate target:', error);
      toast({
        title: 'Error',
        description: 'Failed to duplicate target',
        variant: 'destructive'
      });
    }
  };

  if (loading) {
    return (
      <div className={cn("flex items-center justify-center py-8", className)}>
        <RefreshCw className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <DndProvider backend={HTML5Backend}>
      <div className={cn("space-y-6", className)}>
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold">Orchestration Targets</h3>
            <p className="text-sm text-muted-foreground">
              Drag to reorder execution sequence. Targets execute in order unless marked as parallel.
            </p>
          </div>
          <Button onClick={() => setShowAddDialog(true)} className="gap-2">
            <Plus className="h-4 w-4" />
            Add Target
          </Button>
        </div>

        {targets.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12 text-center">
              <AlertCircle className="h-12 w-12 text-muted-foreground mb-4" />
              <h4 className="text-lg font-semibold mb-2">No Targets Configured</h4>
              <p className="text-muted-foreground mb-4">
                Add target adapters to define where messages should be routed.
              </p>
              <Button onClick={() => setShowAddDialog(true)} className="gap-2">
                <Plus className="h-4 w-4" />
                Add First Target
              </Button>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-3">
            {targets.map((target, index) => (
              <OrchestrationTargetItem
                key={target.id}
                target={target}
                index={index}
                onMove={moveTarget}
                onEdit={setEditingTarget}
                onDelete={handleDeleteTarget}
                onToggleActive={handleToggleActive}
                onDuplicate={handleDuplicateTarget}
              />
            ))}
          </div>
        )}

        {/* Add Target Dialog */}
        {showAddDialog && (
          <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50">
            <div className="fixed left-[50%] top-[50%] z-50 w-full max-w-lg translate-x-[-50%] translate-y-[-50%]">
              <Card>
                <CardHeader>
                  <CardTitle>Add Target Adapter</CardTitle>
                  <CardDescription>
                    Select an adapter to add as a target for this orchestration flow.
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid gap-2">
                    {availableAdapters.map(adapter => (
                      <Button
                        key={adapter.id}
                        variant="outline"
                        className="justify-start"
                        onClick={() => handleAddTarget(adapter.id)}
                      >
                        <Badge variant="secondary" className="mr-2">
                          {adapter.type}
                        </Badge>
                        {adapter.name}
                      </Button>
                    ))}
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      onClick={() => setShowAddDialog(false)}
                    >
                      Cancel
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        )}

        {/* Edit Target Dialog */}
        {editingTarget && (
          <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50">
            <div className="fixed left-[50%] top-[50%] z-50 w-full max-w-3xl max-h-[90vh] overflow-y-auto translate-x-[-50%] translate-y-[-50%]">
              <Card>
                <CardHeader>
                  <CardTitle>Edit Target: {editingTarget.adapterName}</CardTitle>
                  <CardDescription>
                    Configure routing conditions, retry policies, and error handling.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <Tabs defaultValue="routing" className="w-full">
                    <TabsList className="grid w-full grid-cols-3">
                      <TabsTrigger value="routing">Routing</TabsTrigger>
                      <TabsTrigger value="resilience">Resilience</TabsTrigger>
                      <TabsTrigger value="advanced">Advanced</TabsTrigger>
                    </TabsList>
                    
                    <TabsContent value="routing" className="space-y-4 mt-4">
                      <div className="space-y-4">
                        <div className="flex items-center justify-between">
                          <Label htmlFor="parallel">Parallel Execution</Label>
                          <Switch
                            id="parallel"
                            checked={editingTarget.parallel}
                            onCheckedChange={(checked) => 
                              setEditingTarget({ ...editingTarget, parallel: checked })
                            }
                          />
                        </div>
                        
                        <Separator />
                        
                        <div className="space-y-2">
                          <Label>Routing Condition</Label>
                          <RoutingRuleBuilder
                            conditionType={editingTarget.conditionType}
                            routingCondition={editingTarget.routingCondition}
                            onChange={(condition, type) => 
                              setEditingTarget({ 
                                ...editingTarget, 
                                routingCondition: condition,
                                conditionType: type 
                              })
                            }
                          />
                        </div>
                      </div>
                    </TabsContent>
                    
                    <TabsContent value="resilience" className="space-y-4 mt-4">
                      <div className="grid gap-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div className="space-y-2">
                            <Label htmlFor="retryAttempts">Retry Attempts</Label>
                            <Input
                              id="retryAttempts"
                              type="number"
                              min="0"
                              max="10"
                              value={editingTarget.retryAttempts || 0}
                              onChange={(e) => 
                                setEditingTarget({ 
                                  ...editingTarget, 
                                  retryAttempts: parseInt(e.target.value) || 0 
                                })
                              }
                            />
                          </div>
                          
                          <div className="space-y-2">
                            <Label htmlFor="retryDelayMs">Retry Delay (ms)</Label>
                            <Input
                              id="retryDelayMs"
                              type="number"
                              min="0"
                              value={editingTarget.retryDelayMs || 1000}
                              onChange={(e) => 
                                setEditingTarget({ 
                                  ...editingTarget, 
                                  retryDelayMs: parseInt(e.target.value) || 1000 
                                })
                              }
                            />
                          </div>
                        </div>
                        
                        <div className="space-y-2">
                          <Label htmlFor="timeoutMs">Timeout (ms)</Label>
                          <Input
                            id="timeoutMs"
                            type="number"
                            min="1000"
                            value={editingTarget.timeoutMs || 30000}
                            onChange={(e) => 
                              setEditingTarget({ 
                                ...editingTarget, 
                                timeoutMs: parseInt(e.target.value) || 30000 
                              })
                            }
                          />
                        </div>
                        
                        <div className="space-y-2">
                          <Label htmlFor="errorStrategy">Error Strategy</Label>
                          <select
                            id="errorStrategy"
                            className="w-full p-2 border rounded-md"
                            value={editingTarget.errorStrategy || 'FAIL'}
                            onChange={(e) => 
                              setEditingTarget({ 
                                ...editingTarget, 
                                errorStrategy: e.target.value as any 
                              })
                            }
                          >
                            <option value="FAIL">Fail Flow</option>
                            <option value="CONTINUE">Continue to Next</option>
                            <option value="COMPENSATE">Run Compensation</option>
                            <option value="RETRY">Retry with Backoff</option>
                          </select>
                        </div>
                      </div>
                    </TabsContent>
                    
                    <TabsContent value="advanced" className="space-y-4 mt-4">
                      <Alert>
                        <AlertCircle className="h-4 w-4" />
                        <AlertDescription>
                          Advanced settings for structure mappings and metadata.
                        </AlertDescription>
                      </Alert>
                      
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <Label htmlFor="structureId">Structure ID</Label>
                          <Input
                            id="structureId"
                            value={editingTarget.structureId || ''}
                            onChange={(e) => 
                              setEditingTarget({ 
                                ...editingTarget, 
                                structureId: e.target.value 
                              })
                            }
                            placeholder="Optional: Override default structure"
                          />
                        </div>
                        
                        <div className="space-y-2">
                          <Label htmlFor="responseStructureId">Response Structure ID</Label>
                          <Input
                            id="responseStructureId"
                            value={editingTarget.responseStructureId || ''}
                            onChange={(e) => 
                              setEditingTarget({ 
                                ...editingTarget, 
                                responseStructureId: e.target.value 
                              })
                            }
                            placeholder="Optional: For synchronous targets"
                          />
                        </div>
                      </div>
                    </TabsContent>
                  </Tabs>
                  
                  <div className="flex justify-end gap-2 mt-6">
                    <Button
                      variant="outline"
                      onClick={() => setEditingTarget(null)}
                    >
                      Cancel
                    </Button>
                    <Button
                      onClick={() => handleUpdateTarget(editingTarget)}
                    >
                      Save Changes
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        )}
      </div>
    </DndProvider>
  );
}
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import {
  FileText,
  Plus,
  Save,
  Copy,
  Trash2,
  Download,
  Upload,
  Search,
  Filter,
  Star,
  StarOff,
  GitBranch,
  Layers,
  Share2,
  Lock,
  Unlock,
  Eye,
  Edit,
  CheckCircle2,
  AlertCircle,
  Info,
  Code,
  Package,
  Zap,
  RefreshCw,
  ArrowRight,
  ArrowLeft,
  Users,
  Calendar
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import ReactFlow, {
  Node,
  Edge,
  Background,
  Controls,
  MiniMap,
  ReactFlowProvider,
  Handle,
  Position,
  MarkerType,
  NodeProps,
  BackgroundVariant
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

interface OrchestrationPatternLibraryProps {
  onSelectPattern?: (pattern: OrchestrationPattern) => void;
  showActions?: boolean;
  className?: string;
}

interface OrchestrationPattern {
  id: string;
  name: string;
  description?: string;
  category: 'INTEGRATION' | 'TRANSFORMATION' | 'ROUTING' | 'ERROR_HANDLING' | 'SECURITY' | 'CUSTOM';
  type: 'BASIC' | 'ADVANCED' | 'ENTERPRISE';
  tags: string[];
  author: string;
  createdAt: string;
  updatedAt: string;
  version: string;
  isPublic: boolean;
  isFavorite: boolean;
  usageCount: number;
  rating: number;
  components: {
    nodes: PatternNode[];
    connections: PatternConnection[];
    configuration: PatternConfig;
  };
  metadata: {
    targetCount: number;
    complexity: 'LOW' | 'MEDIUM' | 'HIGH';
    estimatedDuration: number;
    requirements: string[];
    benefits: string[];
  };
}

interface PatternNode {
  id: string;
  type: 'SOURCE' | 'TRANSFORMER' | 'ROUTER' | 'TARGET' | 'ERROR_HANDLER' | 'AGGREGATOR' | 'SPLITTER';
  name: string;
  description?: string;
  configuration?: Record<string, any>;
  position: { x: number; y: number };
}

interface PatternConnection {
  id: string;
  source: string;
  target: string;
  condition?: string;
  label?: string;
}

interface PatternConfig {
  transactionManagement: boolean;
  errorHandling: string;
  retryPolicy?: {
    enabled: boolean;
    maxAttempts: number;
    backoffMultiplier: number;
  };
  monitoring: boolean;
  logging: string;
}

interface PatternCategory {
  value: string;
  label: string;
  icon: React.ElementType;
  description: string;
}

const PATTERN_CATEGORIES: PatternCategory[] = [
  {
    value: 'INTEGRATION',
    label: 'Integration',
    icon: GitBranch,
    description: 'Point-to-point and multi-system integration patterns'
  },
  {
    value: 'TRANSFORMATION',
    label: 'Transformation',
    icon: RefreshCw,
    description: 'Data transformation and mapping patterns'
  },
  {
    value: 'ROUTING',
    label: 'Routing',
    icon: Share2,
    description: 'Message routing and distribution patterns'
  },
  {
    value: 'ERROR_HANDLING',
    label: 'Error Handling',
    icon: AlertCircle,
    description: 'Error recovery and compensation patterns'
  },
  {
    value: 'SECURITY',
    label: 'Security',
    icon: Lock,
    description: 'Security and authentication patterns'
  },
  {
    value: 'CUSTOM',
    label: 'Custom',
    icon: Package,
    description: 'User-defined custom patterns'
  }
];

const DEFAULT_PATTERNS: OrchestrationPattern[] = [
  {
    id: 'scatter-gather',
    name: 'Scatter-Gather',
    description: 'Send a message to multiple recipients and aggregate responses',
    category: 'ROUTING',
    type: 'ADVANCED',
    tags: ['parallel', 'aggregation', 'performance'],
    author: 'System',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: '1.0.0',
    isPublic: true,
    isFavorite: false,
    usageCount: 156,
    rating: 4.5,
    components: {
      nodes: [
        { id: 'source', type: 'SOURCE', name: 'Request Source', position: { x: 100, y: 200 } },
        { id: 'splitter', type: 'SPLITTER', name: 'Message Splitter', position: { x: 300, y: 200 } },
        { id: 'target1', type: 'TARGET', name: 'Service A', position: { x: 500, y: 100 } },
        { id: 'target2', type: 'TARGET', name: 'Service B', position: { x: 500, y: 200 } },
        { id: 'target3', type: 'TARGET', name: 'Service C', position: { x: 500, y: 300 } },
        { id: 'aggregator', type: 'AGGREGATOR', name: 'Response Aggregator', position: { x: 700, y: 200 } }
      ],
      connections: [
        { id: 'c1', source: 'source', target: 'splitter' },
        { id: 'c2', source: 'splitter', target: 'target1' },
        { id: 'c3', source: 'splitter', target: 'target2' },
        { id: 'c4', source: 'splitter', target: 'target3' },
        { id: 'c5', source: 'target1', target: 'aggregator' },
        { id: 'c6', source: 'target2', target: 'aggregator' },
        { id: 'c7', source: 'target3', target: 'aggregator' }
      ],
      configuration: {
        transactionManagement: false,
        errorHandling: 'CONTINUE_ON_ERROR',
        monitoring: true,
        logging: 'INFO'
      }
    },
    metadata: {
      targetCount: 3,
      complexity: 'MEDIUM',
      estimatedDuration: 500,
      requirements: ['Multiple target endpoints', 'Aggregation strategy'],
      benefits: ['Parallel processing', 'Improved performance', 'Fault isolation']
    }
  },
  {
    id: 'circuit-breaker',
    name: 'Circuit Breaker',
    description: 'Prevent cascading failures with automatic service protection',
    category: 'ERROR_HANDLING',
    type: 'ADVANCED',
    tags: ['resilience', 'fault-tolerance', 'stability'],
    author: 'System',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: '1.0.0',
    isPublic: true,
    isFavorite: false,
    usageCount: 89,
    rating: 4.8,
    components: {
      nodes: [
        { id: 'source', type: 'SOURCE', name: 'Client Request', position: { x: 100, y: 200 } },
        { id: 'breaker', type: 'ERROR_HANDLER', name: 'Circuit Breaker', position: { x: 300, y: 200 } },
        { id: 'target', type: 'TARGET', name: 'Protected Service', position: { x: 500, y: 200 } },
        { id: 'fallback', type: 'TARGET', name: 'Fallback Response', position: { x: 500, y: 350 } }
      ],
      connections: [
        { id: 'c1', source: 'source', target: 'breaker' },
        { id: 'c2', source: 'breaker', target: 'target', label: 'Circuit Closed' },
        { id: 'c3', source: 'breaker', target: 'fallback', label: 'Circuit Open' }
      ],
      configuration: {
        transactionManagement: false,
        errorHandling: 'CIRCUIT_BREAKER',
        retryPolicy: {
          enabled: true,
          maxAttempts: 3,
          backoffMultiplier: 2
        },
        monitoring: true,
        logging: 'WARN'
      }
    },
    metadata: {
      targetCount: 1,
      complexity: 'MEDIUM',
      estimatedDuration: 100,
      requirements: ['Error threshold configuration', 'Fallback mechanism'],
      benefits: ['Prevents cascade failures', 'Automatic recovery', 'System stability']
    }
  }
];

const PatternNode = ({ data }: NodeProps) => {
  const getIcon = () => {
    switch (data.type) {
      case 'SOURCE': return <ArrowRight className="h-4 w-4" />;
      case 'TARGET': return <CheckCircle2 className="h-4 w-4" />;
      case 'TRANSFORMER': return <RefreshCw className="h-4 w-4" />;
      case 'ROUTER': return <Share2 className="h-4 w-4" />;
      case 'SPLITTER': return <GitBranch className="h-4 w-4" />;
      case 'AGGREGATOR': return <Layers className="h-4 w-4" />;
      case 'ERROR_HANDLER': return <AlertCircle className="h-4 w-4" />;
      default: return <Zap className="h-4 w-4" />;
    }
  };

  return (
    <div className="px-4 py-2 shadow-md rounded-md border bg-background min-w-[150px]">
      <Handle type="target" position={Position.Top} className="w-3 h-3" />
      <div className="flex items-center gap-2">
        {getIcon()}
        <div className="text-sm font-medium">{data.label}</div>
      </div>
      <Handle type="source" position={Position.Bottom} className="w-3 h-3" />
    </div>
  );
};

const nodeTypes = {
  custom: PatternNode
};

export function OrchestrationPatternLibrary({
  onSelectPattern,
  showActions = true,
  className
}: OrchestrationPatternLibraryProps) {
  const { toast } = useToast();
  const [patterns, setPatterns] = useState<OrchestrationPattern[]>(DEFAULT_PATTERNS);
  const [loading, setLoading] = useState(false);
  const [selectedPattern, setSelectedPattern] = useState<OrchestrationPattern | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [newPattern, setNewPattern] = useState<Partial<OrchestrationPattern>>({
    name: '',
    description: '',
    category: 'CUSTOM',
    type: 'BASIC',
    tags: [],
    isPublic: false
  });

  // Fetch patterns from API
  const fetchPatterns = useCallback(async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/api/patterns/orchestration');
      setPatterns([...DEFAULT_PATTERNS, ...response.data]);
    } catch (error) {
      console.error('Error fetching patterns:', error);
      // Use default patterns as fallback
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPatterns();
  }, [fetchPatterns]);

  const filteredPatterns = useMemo(() => {
    return patterns.filter(pattern => {
      const matchesSearch = !searchTerm || 
        pattern.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        pattern.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        pattern.tags.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase()));
      
      const matchesCategory = selectedCategory === 'all' || pattern.category === selectedCategory;
      const matchesFavorites = !showFavoritesOnly || pattern.isFavorite;
      
      return matchesSearch && matchesCategory && matchesFavorites;
    });
  }, [patterns, searchTerm, selectedCategory, showFavoritesOnly]);

  const handleToggleFavorite = async (patternId: string) => {
    try {
      const pattern = patterns.find(p => p.id === patternId);
      if (!pattern) return;

      // Optimistic update
      setPatterns(prev => prev.map(p => 
        p.id === patternId ? { ...p, isFavorite: !p.isFavorite } : p
      ));

      await apiClient.put(`/api/patterns/orchestration/${patternId}/favorite`, {
        isFavorite: !pattern.isFavorite
      });
    } catch (error) {
      // Revert on error
      const pattern = patterns.find(p => p.id === patternId);
      if (pattern) {
        setPatterns(prev => prev.map(p => 
          p.id === patternId ? { ...p, isFavorite: !pattern.isFavorite } : p
        ));
      }
      toast({
        title: 'Error',
        description: 'Failed to update favorite status',
        variant: 'destructive'
      });
    }
  };

  const handleDeletePattern = async (patternId: string) => {
    try {
      await apiClient.delete(`/api/patterns/orchestration/${patternId}`);
      setPatterns(prev => prev.filter(p => p.id !== patternId));
      toast({
        title: 'Success',
        description: 'Pattern deleted successfully'
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to delete pattern',
        variant: 'destructive'
      });
    }
  };

  const handleCreatePattern = async () => {
    if (!newPattern.name) {
      toast({
        title: 'Validation Error',
        description: 'Please provide a pattern name',
        variant: 'destructive'
      });
      return;
    }

    try {
      const response = await apiClient.post('/api/patterns/orchestration', {
        ...newPattern,
        author: 'Current User', // This would come from auth context
        version: '1.0.0',
        components: {
          nodes: [],
          connections: [],
          configuration: {
            transactionManagement: false,
            errorHandling: 'DEFAULT',
            monitoring: true,
            logging: 'INFO'
          }
        },
        metadata: {
          targetCount: 0,
          complexity: 'LOW',
          estimatedDuration: 0,
          requirements: [],
          benefits: []
        }
      });

      setPatterns(prev => [...prev, response.data]);
      setShowCreateDialog(false);
      setNewPattern({
        name: '',
        description: '',
        category: 'CUSTOM',
        type: 'BASIC',
        tags: [],
        isPublic: false
      });
      
      toast({
        title: 'Success',
        description: 'Pattern created successfully'
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to create pattern',
        variant: 'destructive'
      });
    }
  };

  const handleDuplicatePattern = async (pattern: OrchestrationPattern) => {
    try {
      const duplicated = {
        ...pattern,
        id: undefined,
        name: `${pattern.name} (Copy)`,
        author: 'Current User',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        usageCount: 0,
        rating: 0,
        isFavorite: false
      };

      const response = await apiClient.post('/api/patterns/orchestration', duplicated);
      setPatterns(prev => [...prev, response.data]);
      
      toast({
        title: 'Success',
        description: 'Pattern duplicated successfully'
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to duplicate pattern',
        variant: 'destructive'
      });
    }
  };

  const exportPattern = (pattern: OrchestrationPattern) => {
    const data = JSON.stringify(pattern, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `pattern-${pattern.name.toLowerCase().replace(/\s+/g, '-')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const importPattern = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const pattern = JSON.parse(e.target?.result as string);
        const response = await apiClient.post('/api/patterns/orchestration', {
          ...pattern,
          id: undefined,
          author: 'Current User',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        });
        
        setPatterns(prev => [...prev, response.data]);
        toast({
          title: 'Success',
          description: 'Pattern imported successfully'
        });
      } catch (error) {
        toast({
          title: 'Error',
          description: 'Failed to import pattern',
          variant: 'destructive'
        });
      }
    };
    reader.readAsText(file);
  };

  // Convert pattern to React Flow elements
  const getFlowElements = (pattern: OrchestrationPattern) => {
    const nodes: Node[] = pattern.components.nodes.map(node => ({
      id: node.id,
      type: 'custom',
      position: node.position,
      data: {
        label: node.name,
        type: node.type
      }
    }));

    const edges: Edge[] = pattern.components.connections.map(conn => ({
      id: conn.id,
      source: conn.source,
      target: conn.target,
      label: conn.label,
      type: 'smoothstep',
      animated: true,
      markerEnd: { type: MarkerType.ArrowClosed }
    }));

    return { nodes, edges };
  };

  return (
    <div className={cn("space-y-6", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                Orchestration Pattern Library
              </CardTitle>
              <CardDescription>
                Browse and manage reusable orchestration patterns
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Label htmlFor="import" className="cursor-pointer">
                <Button variant="outline" size="sm" asChild>
                  <span>
                    <Upload className="mr-2 h-4 w-4" />
                    Import
                  </span>
                </Button>
                <Input
                  id="import"
                  type="file"
                  accept=".json"
                  onChange={importPattern}
                  className="hidden"
                />
              </Label>
              {showActions && (
                <Button onClick={() => setShowCreateDialog(true)}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Pattern
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {/* Filters */}
          <div className="flex items-center gap-4 mb-6">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search patterns..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-8"
                />
              </div>
            </div>
            <Select value={selectedCategory} onValueChange={setSelectedCategory}>
              <SelectTrigger className="w-[200px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Categories</SelectItem>
                {PATTERN_CATEGORIES.map(category => (
                  <SelectItem key={category.value} value={category.value}>
                    <div className="flex items-center gap-2">
                      <category.icon className="h-4 w-4" />
                      {category.label}
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <div className="flex items-center space-x-2">
              <Switch
                id="favorites"
                checked={showFavoritesOnly}
                onCheckedChange={setShowFavoritesOnly}
              />
              <Label htmlFor="favorites" className="cursor-pointer">
                Favorites Only
              </Label>
            </div>
          </div>

          <Tabs defaultValue="grid">
            <TabsList>
              <TabsTrigger value="grid">Grid View</TabsTrigger>
              <TabsTrigger value="list">List View</TabsTrigger>
              <TabsTrigger value="preview">Preview</TabsTrigger>
            </TabsList>

            <TabsContent value="grid" className="mt-4">
              <div className="grid grid-cols-3 gap-4">
                {filteredPatterns.map(pattern => {
                  const category = PATTERN_CATEGORIES.find(c => c.value === pattern.category);
                  const Icon = category?.icon || Package;
                  
                  return (
                    <Card
                      key={pattern.id}
                      className={cn(
                        "cursor-pointer transition-colors hover:border-primary",
                        selectedPattern?.id === pattern.id && "border-primary"
                      )}
                      onClick={() => setSelectedPattern(pattern)}
                    >
                      <CardHeader>
                        <div className="flex items-center justify-between">
                          <Icon className="h-5 w-5 text-muted-foreground" />
                          <div className="flex items-center gap-1">
                            <Button
                              size="icon"
                              variant="ghost"
                              className="h-8 w-8"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleToggleFavorite(pattern.id);
                              }}
                            >
                              {pattern.isFavorite ? (
                                <Star className="h-4 w-4 fill-current text-yellow-500" />
                              ) : (
                                <StarOff className="h-4 w-4" />
                              )}
                            </Button>
                            {pattern.isPublic ? (
                              <Unlock className="h-4 w-4 text-muted-foreground" />
                            ) : (
                              <Lock className="h-4 w-4 text-muted-foreground" />
                            )}
                          </div>
                        </div>
                        <CardTitle className="text-base">{pattern.name}</CardTitle>
                        <CardDescription className="line-clamp-2">
                          {pattern.description}
                        </CardDescription>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-3">
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-muted-foreground">Type</span>
                            <Badge variant={pattern.type === 'ENTERPRISE' ? 'default' : 'secondary'}>
                              {pattern.type}
                            </Badge>
                          </div>
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-muted-foreground">Complexity</span>
                            <Badge
                              variant={
                                pattern.metadata.complexity === 'HIGH' ? 'destructive' :
                                pattern.metadata.complexity === 'MEDIUM' ? 'secondary' : 'outline'
                              }
                            >
                              {pattern.metadata.complexity}
                            </Badge>
                          </div>
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-muted-foreground">Usage</span>
                            <span>{pattern.usageCount} times</span>
                          </div>
                          {pattern.tags.length > 0 && (
                            <div className="flex flex-wrap gap-1">
                              {pattern.tags.slice(0, 3).map(tag => (
                                <Badge key={tag} variant="outline" className="text-xs">
                                  {tag}
                                </Badge>
                              ))}
                              {pattern.tags.length > 3 && (
                                <Badge variant="outline" className="text-xs">
                                  +{pattern.tags.length - 3}
                                </Badge>
                              )}
                            </div>
                          )}
                        </div>
                        {showActions && (
                          <div className="flex items-center gap-1 mt-4 pt-4 border-t">
                            <Button
                              size="sm"
                              variant="outline"
                              className="flex-1"
                              onClick={(e) => {
                                e.stopPropagation();
                                onSelectPattern?.(pattern);
                              }}
                            >
                              Use
                            </Button>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDuplicatePattern(pattern);
                              }}
                            >
                              <Copy className="h-4 w-4" />
                            </Button>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                exportPattern(pattern);
                              }}
                            >
                              <Download className="h-4 w-4" />
                            </Button>
                            {pattern.author === 'Current User' && (
                              <Button
                                size="icon"
                                variant="ghost"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleDeletePattern(pattern.id);
                                }}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            )}
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            </TabsContent>

            <TabsContent value="list" className="mt-4">
              <ScrollArea className="h-[600px]">
                <div className="space-y-2">
                  {filteredPatterns.map(pattern => {
                    const category = PATTERN_CATEGORIES.find(c => c.value === pattern.category);
                    const Icon = category?.icon || Package;
                    
                    return (
                      <Card
                        key={pattern.id}
                        className={cn(
                          "cursor-pointer transition-colors hover:border-primary",
                          selectedPattern?.id === pattern.id && "border-primary"
                        )}
                        onClick={() => setSelectedPattern(pattern)}
                      >
                        <CardContent className="p-4">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-4 flex-1">
                              <Icon className="h-8 w-8 text-muted-foreground" />
                              <div className="flex-1">
                                <div className="flex items-center gap-2">
                                  <p className="font-medium">{pattern.name}</p>
                                  <Badge variant="outline">{pattern.category}</Badge>
                                  <Badge variant={pattern.type === 'ENTERPRISE' ? 'default' : 'secondary'}>
                                    {pattern.type}
                                  </Badge>
                                  {pattern.isPublic ? (
                                    <Unlock className="h-4 w-4 text-muted-foreground" />
                                  ) : (
                                    <Lock className="h-4 w-4 text-muted-foreground" />
                                  )}
                                </div>
                                <p className="text-sm text-muted-foreground mt-1">
                                  {pattern.description}
                                </p>
                                <div className="flex items-center gap-4 mt-2 text-xs text-muted-foreground">
                                  <span className="flex items-center gap-1">
                                    <Users className="h-3 w-3" />
                                    {pattern.author}
                                  </span>
                                  <span className="flex items-center gap-1">
                                    <Calendar className="h-3 w-3" />
                                    {new Date(pattern.updatedAt).toLocaleDateString()}
                                  </span>
                                  <span className="flex items-center gap-1">
                                    <Activity className="h-3 w-3" />
                                    {pattern.usageCount} uses
                                  </span>
                                  <span>v{pattern.version}</span>
                                </div>
                              </div>
                            </div>
                            <div className="flex items-center gap-2">
                              <Button
                                size="icon"
                                variant="ghost"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleToggleFavorite(pattern.id);
                                }}
                              >
                                {pattern.isFavorite ? (
                                  <Star className="h-4 w-4 fill-current text-yellow-500" />
                                ) : (
                                  <StarOff className="h-4 w-4" />
                                )}
                              </Button>
                              {showActions && (
                                <>
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      onSelectPattern?.(pattern);
                                    }}
                                  >
                                    Use Pattern
                                  </Button>
                                  <Button
                                    size="icon"
                                    variant="ghost"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      exportPattern(pattern);
                                    }}
                                  >
                                    <Download className="h-4 w-4" />
                                  </Button>
                                </>
                              )}
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>
              </ScrollArea>
            </TabsContent>

            <TabsContent value="preview" className="mt-4">
              {selectedPattern ? (
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <Card>
                      <CardHeader>
                        <CardTitle>{selectedPattern.name}</CardTitle>
                        <CardDescription>{selectedPattern.description}</CardDescription>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <Label className="text-muted-foreground">Category</Label>
                            <p className="font-medium">{selectedPattern.category}</p>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Type</Label>
                            <p className="font-medium">{selectedPattern.type}</p>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Complexity</Label>
                            <p className="font-medium">{selectedPattern.metadata.complexity}</p>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Target Count</Label>
                            <p className="font-medium">{selectedPattern.metadata.targetCount}</p>
                          </div>
                        </div>

                        <Separator />

                        <div>
                          <Label className="text-muted-foreground">Requirements</Label>
                          <ul className="list-disc list-inside space-y-1 mt-2">
                            {selectedPattern.metadata.requirements.map((req, idx) => (
                              <li key={idx} className="text-sm">{req}</li>
                            ))}
                          </ul>
                        </div>

                        <div>
                          <Label className="text-muted-foreground">Benefits</Label>
                          <ul className="list-disc list-inside space-y-1 mt-2">
                            {selectedPattern.metadata.benefits.map((benefit, idx) => (
                              <li key={idx} className="text-sm">{benefit}</li>
                            ))}
                          </ul>
                        </div>

                        <div>
                          <Label className="text-muted-foreground">Configuration</Label>
                          <div className="space-y-2 mt-2">
                            <div className="flex items-center gap-2">
                              {selectedPattern.components.configuration.transactionManagement ? (
                                <CheckCircle2 className="h-4 w-4 text-green-500" />
                              ) : (
                                <XCircle className="h-4 w-4 text-gray-400" />
                              )}
                              <span className="text-sm">Transaction Management</span>
                            </div>
                            <div className="flex items-center gap-2">
                              {selectedPattern.components.configuration.monitoring ? (
                                <CheckCircle2 className="h-4 w-4 text-green-500" />
                              ) : (
                                <XCircle className="h-4 w-4 text-gray-400" />
                              )}
                              <span className="text-sm">Monitoring</span>
                            </div>
                            <div className="text-sm">
                              Error Handling: {selectedPattern.components.configuration.errorHandling}
                            </div>
                            <div className="text-sm">
                              Logging Level: {selectedPattern.components.configuration.logging}
                            </div>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  </div>

                  <Card>
                    <CardHeader>
                      <CardTitle className="text-base">Pattern Flow</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="h-[400px] border rounded-lg">
                        <ReactFlowProvider>
                          <ReactFlow
                            {...getFlowElements(selectedPattern)}
                            nodeTypes={nodeTypes}
                            fitView
                            attributionPosition="bottom-left"
                          >
                            <Background variant={BackgroundVariant.Dots} />
                            <Controls />
                            <MiniMap />
                          </ReactFlow>
                        </ReactFlowProvider>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              ) : (
                <Card>
                  <CardContent className="pt-6">
                    <div className="text-center py-12">
                      <FileText className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground">Select a pattern to preview</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Create Pattern Dialog */}
      {showCreateDialog && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50" onClick={() => setShowCreateDialog(false)}>
          <div className="fixed inset-4 md:inset-auto md:left-1/2 md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:w-[500px] bg-background border rounded-lg shadow-lg" onClick={(e) => e.stopPropagation()}>
            <div className="p-6 space-y-4">
              <h3 className="text-lg font-semibold">Create New Pattern</h3>
              
              <div className="space-y-4">
                <div>
                  <Label htmlFor="patternName">Pattern Name *</Label>
                  <Input
                    id="patternName"
                    value={newPattern.name}
                    onChange={(e) => setNewPattern({ ...newPattern, name: e.target.value })}
                    placeholder="Enter pattern name"
                    className="mt-1"
                  />
                </div>

                <div>
                  <Label htmlFor="patternDescription">Description</Label>
                  <Textarea
                    id="patternDescription"
                    value={newPattern.description}
                    onChange={(e) => setNewPattern({ ...newPattern, description: e.target.value })}
                    placeholder="Describe the pattern"
                    className="mt-1"
                    rows={3}
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="patternCategory">Category</Label>
                    <Select
                      value={newPattern.category}
                      onValueChange={(value) => setNewPattern({ ...newPattern, category: value as any })}
                    >
                      <SelectTrigger id="patternCategory" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {PATTERN_CATEGORIES.map(category => (
                          <SelectItem key={category.value} value={category.value}>
                            <div className="flex items-center gap-2">
                              <category.icon className="h-4 w-4" />
                              {category.label}
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="patternType">Type</Label>
                    <Select
                      value={newPattern.type}
                      onValueChange={(value) => setNewPattern({ ...newPattern, type: value as any })}
                    >
                      <SelectTrigger id="patternType" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="BASIC">Basic</SelectItem>
                        <SelectItem value="ADVANCED">Advanced</SelectItem>
                        <SelectItem value="ENTERPRISE">Enterprise</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div>
                  <Label htmlFor="patternTags">Tags (comma-separated)</Label>
                  <Input
                    id="patternTags"
                    value={newPattern.tags?.join(', ')}
                    onChange={(e) => setNewPattern({ 
                      ...newPattern, 
                      tags: e.target.value.split(',').map(t => t.trim()).filter(Boolean)
                    })}
                    placeholder="e.g., integration, performance, resilience"
                    className="mt-1"
                  />
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="isPublic"
                    checked={newPattern.isPublic}
                    onCheckedChange={(checked) => setNewPattern({ ...newPattern, isPublic: checked })}
                  />
                  <Label htmlFor="isPublic">Make pattern public</Label>
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-4">
                <Button variant="outline" onClick={() => setShowCreateDialog(false)}>
                  Cancel
                </Button>
                <Button onClick={handleCreatePattern}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Pattern
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
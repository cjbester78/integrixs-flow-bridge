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
  ArrowRightLeft,
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
  Code,
  FileJson,
  FileCode,
  Layers,
  ArrowRight,
  CheckCircle2,
  AlertCircle,
  Info,
  Edit,
  Eye,
  TestTube,
  Function,
  Hash,
  Type,
  Calendar,
  User,
  GitBranch,
  Zap
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import Editor from '@monaco-editor/react';
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

interface MappingTemplateLibraryProps {
  onSelectTemplate?: (template: MappingTemplate) => void;
  showActions?: boolean;
  className?: string;
}

interface MappingTemplate {
  id: string;
  name: string;
  description?: string;
  category: 'TRANSFORMATION' | 'ENRICHMENT' | 'FILTERING' | 'AGGREGATION' | 'FORMATTING' | 'CUSTOM';
  type: 'SIMPLE' | 'COMPLEX' | 'COMPOSITE';
  sourceFormat: 'JSON' | 'XML' | 'CSV' | 'FIXED' | 'ANY';
  targetFormat: 'JSON' | 'XML' | 'CSV' | 'FIXED' | 'ANY';
  mappings: FieldMapping[];
  functions: TransformationFunction[];
  metadata: {
    complexity: 'LOW' | 'MEDIUM' | 'HIGH';
    performance: 'FAST' | 'MODERATE' | 'SLOW';
    reusability: 'HIGH' | 'MEDIUM' | 'LOW';
    requirements: string[];
    limitations: string[];
    examples: MappingExample[];
  };
  configuration: {
    strictValidation: boolean;
    allowPartialMapping: boolean;
    errorHandling: 'FAIL' | 'SKIP' | 'DEFAULT';
    defaultValues: Record<string, any>;
    namespaces?: Record<string, string>;
  };
  tags: string[];
  author: string;
  createdAt: string;
  updatedAt: string;
  version: string;
  isPublic: boolean;
  isFavorite: boolean;
  usageCount: number;
  rating: number;
}

interface FieldMapping {
  id: string;
  sourcePath: string;
  targetPath: string;
  mappingType: 'DIRECT' | 'FUNCTION' | 'CONDITIONAL' | 'CONSTANT' | 'COMPOSITE';
  transformation?: string;
  condition?: string;
  defaultValue?: any;
  required: boolean;
  description?: string;
}

interface TransformationFunction {
  id: string;
  name: string;
  type: 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN' | 'ARRAY' | 'OBJECT' | 'CUSTOM';
  expression: string;
  language: 'JAVASCRIPT' | 'GROOVY' | 'JEXL' | 'SPEL';
  inputs: FunctionInput[];
  output: FunctionOutput;
  examples: FunctionExample[];
}

interface FunctionInput {
  name: string;
  type: string;
  required: boolean;
  description?: string;
}

interface FunctionOutput {
  type: string;
  description?: string;
}

interface FunctionExample {
  inputs: Record<string, any>;
  output: any;
  description?: string;
}

interface MappingExample {
  sourceData: any;
  targetData: any;
  description: string;
}

const TEMPLATE_CATEGORIES = [
  {
    value: 'TRANSFORMATION',
    label: 'Transformation',
    icon: ArrowRightLeft,
    description: 'Data transformation templates'
  },
  {
    value: 'ENRICHMENT',
    label: 'Enrichment',
    icon: Plus,
    description: 'Data enrichment and augmentation'
  },
  {
    value: 'FILTERING',
    label: 'Filtering',
    icon: Filter,
    description: 'Data filtering and selection'
  },
  {
    value: 'AGGREGATION',
    label: 'Aggregation',
    icon: Layers,
    description: 'Data aggregation and grouping'
  },
  {
    value: 'FORMATTING',
    label: 'Formatting',
    icon: Type,
    description: 'Data formatting and styling'
  },
  {
    value: 'CUSTOM',
    label: 'Custom',
    icon: Code,
    description: 'User-defined mapping logic'
  }
];

const DEFAULT_TEMPLATES: MappingTemplate[] = [
  {
    id: 'json-to-json-basic',
    name: 'JSON to JSON Basic Mapping',
    description: 'Simple field-to-field mapping between JSON structures',
    category: 'TRANSFORMATION',
    type: 'SIMPLE',
    sourceFormat: 'JSON',
    targetFormat: 'JSON',
    mappings: [
      {
        id: 'm1',
        sourcePath: '$.customer.firstName',
        targetPath: '$.client.name.first',
        mappingType: 'DIRECT',
        required: true,
        description: 'Map first name'
      },
      {
        id: 'm2',
        sourcePath: '$.customer.lastName',
        targetPath: '$.client.name.last',
        mappingType: 'DIRECT',
        required: true,
        description: 'Map last name'
      },
      {
        id: 'm3',
        sourcePath: '$.customer.email',
        targetPath: '$.client.contact.email',
        mappingType: 'FUNCTION',
        transformation: 'toLowerCase()',
        required: true,
        description: 'Map email with lowercase transformation'
      },
      {
        id: 'm4',
        sourcePath: '$.customer.birthDate',
        targetPath: '$.client.dateOfBirth',
        mappingType: 'FUNCTION',
        transformation: 'formatDate(sourceValue, "yyyy-MM-dd")',
        required: false,
        description: 'Format birth date'
      }
    ],
    functions: [],
    metadata: {
      complexity: 'LOW',
      performance: 'FAST',
      reusability: 'HIGH',
      requirements: ['Source must be valid JSON', 'Date fields should be in ISO format'],
      limitations: ['Does not handle nested arrays'],
      examples: [
        {
          sourceData: {
            customer: {
              firstName: 'John',
              lastName: 'Doe',
              email: 'JOHN.DOE@EXAMPLE.COM',
              birthDate: '1990-01-15T00:00:00Z'
            }
          },
          targetData: {
            client: {
              name: {
                first: 'John',
                last: 'Doe'
              },
              contact: {
                email: 'john.doe@example.com'
              },
              dateOfBirth: '1990-01-15'
            }
          },
          description: 'Basic customer to client transformation'
        }
      ]
    },
    configuration: {
      strictValidation: false,
      allowPartialMapping: true,
      errorHandling: 'SKIP',
      defaultValues: {}
    },
    tags: ['json', 'basic', 'customer', 'transformation'],
    author: 'System',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: '1.0.0',
    isPublic: true,
    isFavorite: false,
    usageCount: 342,
    rating: 4.5
  },
  {
    id: 'xml-enrichment',
    name: 'XML Data Enrichment',
    description: 'Enrich XML data with additional fields and calculations',
    category: 'ENRICHMENT',
    type: 'COMPLEX',
    sourceFormat: 'XML',
    targetFormat: 'XML',
    mappings: [
      {
        id: 'm1',
        sourcePath: '//Order/OrderItems/Item',
        targetPath: '//EnrichedOrder/Items/EnrichedItem',
        mappingType: 'COMPOSITE',
        required: true,
        description: 'Map and enrich order items'
      },
      {
        id: 'm2',
        sourcePath: '//Order/OrderItems/Item/Price',
        targetPath: '//EnrichedOrder/Items/EnrichedItem/TotalPrice',
        mappingType: 'FUNCTION',
        transformation: 'multiply(xpath("./Price"), xpath("./Quantity"))',
        required: true,
        description: 'Calculate total price per item'
      },
      {
        id: 'm3',
        sourcePath: '',
        targetPath: '//EnrichedOrder/Summary/TotalAmount',
        mappingType: 'FUNCTION',
        transformation: 'sum(xpath("//EnrichedItem/TotalPrice"))',
        required: true,
        description: 'Calculate order total'
      },
      {
        id: 'm4',
        sourcePath: '',
        targetPath: '//EnrichedOrder/Summary/ItemCount',
        mappingType: 'FUNCTION',
        transformation: 'count(xpath("//Item"))',
        required: true,
        description: 'Count total items'
      }
    ],
    functions: [
      {
        id: 'f1',
        name: 'multiply',
        type: 'NUMBER',
        expression: 'return parseFloat(a) * parseFloat(b);',
        language: 'JAVASCRIPT',
        inputs: [
          { name: 'a', type: 'number', required: true },
          { name: 'b', type: 'number', required: true }
        ],
        output: { type: 'number', description: 'Product of a and b' },
        examples: [
          {
            inputs: { a: 10, b: 5 },
            output: 50,
            description: 'Simple multiplication'
          }
        ]
      }
    ],
    metadata: {
      complexity: 'MEDIUM',
      performance: 'MODERATE',
      reusability: 'MEDIUM',
      requirements: ['Valid XML with Order structure', 'Numeric Price and Quantity fields'],
      limitations: ['XPath expressions must be compatible with XPath 2.0'],
      examples: []
    },
    configuration: {
      strictValidation: true,
      allowPartialMapping: false,
      errorHandling: 'FAIL',
      defaultValues: {},
      namespaces: {
        'ord': 'http://example.com/order'
      }
    },
    tags: ['xml', 'enrichment', 'order', 'calculation'],
    author: 'System',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: '1.0.0',
    isPublic: true,
    isFavorite: false,
    usageCount: 128,
    rating: 4.8
  },
  {
    id: 'csv-date-formatter',
    name: 'CSV Date Formatter',
    description: 'Format dates in CSV files to different formats',
    category: 'FORMATTING',
    type: 'SIMPLE',
    sourceFormat: 'CSV',
    targetFormat: 'CSV',
    mappings: [
      {
        id: 'm1',
        sourcePath: 'column[0]',
        targetPath: 'column[0]',
        mappingType: 'DIRECT',
        required: true,
        description: 'Copy ID column'
      },
      {
        id: 'm2',
        sourcePath: 'column[1]',
        targetPath: 'column[1]',
        mappingType: 'DIRECT',
        required: true,
        description: 'Copy name column'
      },
      {
        id: 'm3',
        sourcePath: 'column[2]',
        targetPath: 'column[2]',
        mappingType: 'FUNCTION',
        transformation: 'formatDate(parseDate(value, "MM/dd/yyyy"), "yyyy-MM-dd")',
        required: true,
        description: 'Format date from US to ISO format'
      }
    ],
    functions: [],
    metadata: {
      complexity: 'LOW',
      performance: 'FAST',
      reusability: 'HIGH',
      requirements: ['CSV with at least 3 columns', 'Date in column 3'],
      limitations: ['Fixed column positions'],
      examples: [
        {
          sourceData: 'ID,Name,Date\n001,John Doe,01/15/2024\n002,Jane Smith,02/20/2024',
          targetData: 'ID,Name,Date\n001,John Doe,2024-01-15\n002,Jane Smith,2024-02-20',
          description: 'US date format to ISO format'
        }
      ]
    },
    configuration: {
      strictValidation: false,
      allowPartialMapping: true,
      errorHandling: 'SKIP',
      defaultValues: {}
    },
    tags: ['csv', 'date', 'formatting', 'simple'],
    author: 'System',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: '1.0.0',
    isPublic: true,
    isFavorite: false,
    usageCount: 89,
    rating: 4.2
  }
];

const MappingNode = ({ data }: NodeProps) => {
  const getIcon = () => {
    switch (data.type) {
      case 'source': return <FileJson className="h-4 w-4" />;
      case 'target': return <FileCode className="h-4 w-4" />;
      case 'function': return <Function className="h-4 w-4" />;
      case 'constant': return <Hash className="h-4 w-4" />;
      default: return <ArrowRight className="h-4 w-4" />;
    }
  };

  return (
    <div className="px-4 py-2 shadow-md rounded-md border bg-background min-w-[150px]">
      <Handle type="target" position={Position.Left} className="w-3 h-3" />
      <div className="flex items-center gap-2">
        {getIcon()}
        <div className="text-sm font-medium">{data.label}</div>
      </div>
      <Handle type="source" position={Position.Right} className="w-3 h-3" />
    </div>
  );
};

const nodeTypes = {
  custom: MappingNode
};

export function MappingTemplateLibrary({
  onSelectTemplate,
  showActions = true,
  className
}: MappingTemplateLibraryProps) {
  const { toast } = useToast();
  const [templates, setTemplates] = useState<MappingTemplate[]>(DEFAULT_TEMPLATES);
  const [loading, setLoading] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState<MappingTemplate | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedFormat, setSelectedFormat] = useState<string>('all');
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showTestDialog, setShowTestDialog] = useState(false);
  const [testSourceData, setTestSourceData] = useState('{}');
  const [testResult, setTestResult] = useState<any>(null);

  // Fetch templates from API
  const fetchTemplates = useCallback(async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/api/templates/mapping');
      setTemplates([...DEFAULT_TEMPLATES, ...response.data]);
    } catch (error) {
      console.error('Error fetching mapping templates:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTemplates();
  }, [fetchTemplates]);

  const filteredTemplates = useMemo(() => {
    return templates.filter(template => {
      const matchesSearch = !searchTerm || 
        template.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        template.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        template.tags.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase()));
      
      const matchesCategory = selectedCategory === 'all' || template.category === selectedCategory;
      const matchesFormat = selectedFormat === 'all' || 
        template.sourceFormat === selectedFormat || 
        template.targetFormat === selectedFormat;
      const matchesFavorites = !showFavoritesOnly || template.isFavorite;
      
      return matchesSearch && matchesCategory && matchesFormat && matchesFavorites;
    });
  }, [templates, searchTerm, selectedCategory, selectedFormat, showFavoritesOnly]);

  const handleToggleFavorite = async (templateId: string) => {
    try {
      const template = templates.find(t => t.id === templateId);
      if (!template) return;

      setTemplates(prev => prev.map(t => 
        t.id === templateId ? { ...t, isFavorite: !t.isFavorite } : t
      ));

      await apiClient.put(`/api/templates/mapping/${templateId}/favorite`, {
        isFavorite: !template.isFavorite
      });
    } catch (error) {
      const template = templates.find(t => t.id === templateId);
      if (template) {
        setTemplates(prev => prev.map(t => 
          t.id === templateId ? { ...t, isFavorite: !template.isFavorite } : t
        ));
      }
      toast({
        title: 'Error',
        description: 'Failed to update favorite status',
        variant: 'destructive'
      });
    }
  };

  const handleDeleteTemplate = async (templateId: string) => {
    try {
      await apiClient.delete(`/api/templates/mapping/${templateId}`);
      setTemplates(prev => prev.filter(t => t.id !== templateId));
      toast({
        title: 'Success',
        description: 'Mapping template deleted successfully'
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to delete mapping template',
        variant: 'destructive'
      });
    }
  };

  const handleTestTemplate = async () => {
    if (!selectedTemplate || !testSourceData) return;

    try {
      const sourceData = JSON.parse(testSourceData);
      const response = await apiClient.post('/api/templates/mapping/test', {
        template: selectedTemplate,
        sourceData
      });
      
      setTestResult(response.data);
    } catch (error: any) {
      if (error instanceof SyntaxError) {
        toast({
          title: 'Error',
          description: 'Invalid JSON input',
          variant: 'destructive'
        });
      } else {
        toast({
          title: 'Error',
          description: error.message || 'Failed to test mapping template',
          variant: 'destructive'
        });
      }
    }
  };

  const exportTemplate = (template: MappingTemplate) => {
    const data = JSON.stringify(template, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `mapping-template-${template.name.toLowerCase().replace(/\s+/g, '-')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  // Convert mappings to flow nodes
  const getMappingFlowNodes = (template: MappingTemplate) => {
    const nodes: Node[] = [];
    const edges: Edge[] = [];
    let yPosition = 50;

    template.mappings.forEach((mapping, index) => {
      const mappingId = `mapping-${index}`;
      
      // Source node
      const sourceId = `source-${index}`;
      nodes.push({
        id: sourceId,
        type: 'custom',
        position: { x: 50, y: yPosition },
        data: {
          label: mapping.sourcePath || 'Source',
          type: 'source'
        }
      });

      // Function/transformation node if needed
      if (mapping.mappingType === 'FUNCTION' && mapping.transformation) {
        const functionId = `function-${index}`;
        nodes.push({
          id: functionId,
          type: 'custom',
          position: { x: 250, y: yPosition },
          data: {
            label: mapping.transformation.split('(')[0],
            type: 'function'
          }
        });
        edges.push({
          id: `${sourceId}-${functionId}`,
          source: sourceId,
          target: functionId,
          type: 'smoothstep'
        });
        edges.push({
          id: `${functionId}-target-${index}`,
          source: functionId,
          target: `target-${index}`,
          type: 'smoothstep'
        });
      } else {
        // Direct connection
        edges.push({
          id: `${sourceId}-target-${index}`,
          source: sourceId,
          target: `target-${index}`,
          type: 'smoothstep',
          animated: mapping.mappingType === 'DIRECT'
        });
      }

      // Target node
      nodes.push({
        id: `target-${index}`,
        type: 'custom',
        position: { x: 450, y: yPosition },
        data: {
          label: mapping.targetPath,
          type: 'target'
        }
      });

      yPosition += 100;
    });

    return { nodes, edges };
  };

  return (
    <div className={cn("space-y-6", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <ArrowRightLeft className="h-5 w-5" />
                Mapping Template Library
              </CardTitle>
              <CardDescription>
                Reusable field mapping configurations and transformations
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
                  onChange={(e) => console.log('Import:', e.target.files)}
                  className="hidden"
                />
              </Label>
              {showActions && (
                <Button onClick={() => setShowCreateDialog(true)}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Template
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
                  placeholder="Search mapping templates..."
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
                {TEMPLATE_CATEGORIES.map(category => (
                  <SelectItem key={category.value} value={category.value}>
                    <div className="flex items-center gap-2">
                      <category.icon className="h-4 w-4" />
                      {category.label}
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={selectedFormat} onValueChange={setSelectedFormat}>
              <SelectTrigger className="w-[150px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Formats</SelectItem>
                <SelectItem value="JSON">JSON</SelectItem>
                <SelectItem value="XML">XML</SelectItem>
                <SelectItem value="CSV">CSV</SelectItem>
                <SelectItem value="FIXED">Fixed Width</SelectItem>
              </SelectContent>
            </Select>
            <div className="flex items-center space-x-2">
              <Switch
                id="favorites"
                checked={showFavoritesOnly}
                onCheckedChange={setShowFavoritesOnly}
              />
              <Label htmlFor="favorites" className="cursor-pointer">
                Favorites
              </Label>
            </div>
          </div>

          <Tabs defaultValue="grid">
            <TabsList>
              <TabsTrigger value="grid">Grid View</TabsTrigger>
              <TabsTrigger value="detail">Detailed View</TabsTrigger>
              <TabsTrigger value="visual">Visual Editor</TabsTrigger>
            </TabsList>

            <TabsContent value="grid" className="mt-4">
              <div className="grid grid-cols-2 gap-4">
                {filteredTemplates.map(template => {
                  const category = TEMPLATE_CATEGORIES.find(c => c.value === template.category);
                  const Icon = category?.icon || Code;
                  
                  return (
                    <Card
                      key={template.id}
                      className={cn(
                        "cursor-pointer transition-colors hover:border-primary",
                        selectedTemplate?.id === template.id && "border-primary"
                      )}
                      onClick={() => setSelectedTemplate(template)}
                    >
                      <CardHeader>
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <Icon className="h-5 w-5 text-muted-foreground" />
                            <CardTitle className="text-base">{template.name}</CardTitle>
                          </div>
                          <div className="flex items-center gap-1">
                            <Button
                              size="icon"
                              variant="ghost"
                              className="h-8 w-8"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleToggleFavorite(template.id);
                              }}
                            >
                              {template.isFavorite ? (
                                <Star className="h-4 w-4 fill-current text-yellow-500" />
                              ) : (
                                <StarOff className="h-4 w-4" />
                              )}
                            </Button>
                          </div>
                        </div>
                        <CardDescription>{template.description}</CardDescription>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-3">
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-muted-foreground">Format</span>
                            <div className="flex items-center gap-1">
                              <Badge variant="outline">{template.sourceFormat}</Badge>
                              <ArrowRight className="h-3 w-3" />
                              <Badge variant="outline">{template.targetFormat}</Badge>
                            </div>
                          </div>
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-muted-foreground">Mappings</span>
                            <span className="font-medium">{template.mappings.length}</span>
                          </div>
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-muted-foreground">Complexity</span>
                            <Badge
                              variant={
                                template.metadata.complexity === 'HIGH' ? 'destructive' :
                                template.metadata.complexity === 'MEDIUM' ? 'secondary' : 'outline'
                              }
                            >
                              {template.metadata.complexity}
                            </Badge>
                          </div>
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-muted-foreground">Rating</span>
                            <div className="flex items-center gap-1">
                              <Star className="h-3 w-3 fill-current text-yellow-500" />
                              <span>{template.rating.toFixed(1)}</span>
                            </div>
                          </div>
                          {template.tags.length > 0 && (
                            <div className="flex flex-wrap gap-1">
                              {template.tags.slice(0, 3).map(tag => (
                                <Badge key={tag} variant="secondary" className="text-xs">
                                  {tag}
                                </Badge>
                              ))}
                              {template.tags.length > 3 && (
                                <Badge variant="secondary" className="text-xs">
                                  +{template.tags.length - 3}
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
                                onSelectTemplate?.(template);
                              }}
                            >
                              Use
                            </Button>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedTemplate(template);
                                setShowTestDialog(true);
                              }}
                            >
                              <TestTube className="h-4 w-4" />
                            </Button>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                exportTemplate(template);
                              }}
                            >
                              <Download className="h-4 w-4" />
                            </Button>
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            </TabsContent>

            <TabsContent value="detail" className="mt-4">
              {selectedTemplate ? (
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <Card>
                      <CardHeader>
                        <CardTitle>{selectedTemplate.name}</CardTitle>
                        <CardDescription>{selectedTemplate.description}</CardDescription>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <Label className="text-muted-foreground">Category</Label>
                            <p className="font-medium">{selectedTemplate.category}</p>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Type</Label>
                            <p className="font-medium">{selectedTemplate.type}</p>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Source Format</Label>
                            <Badge variant="outline">{selectedTemplate.sourceFormat}</Badge>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Target Format</Label>
                            <Badge variant="outline">{selectedTemplate.targetFormat}</Badge>
                          </div>
                        </div>

                        <Separator />

                        <div>
                          <h4 className="font-medium mb-2">Metadata</h4>
                          <div className="space-y-2 text-sm">
                            <div className="flex justify-between">
                              <span className="text-muted-foreground">Performance</span>
                              <Badge variant="outline">{selectedTemplate.metadata.performance}</Badge>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-muted-foreground">Reusability</span>
                              <Badge variant="outline">{selectedTemplate.metadata.reusability}</Badge>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-muted-foreground">Usage Count</span>
                              <span>{selectedTemplate.usageCount}</span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-muted-foreground">Version</span>
                              <span>{selectedTemplate.version}</span>
                            </div>
                          </div>
                        </div>

                        {selectedTemplate.metadata.requirements.length > 0 && (
                          <>
                            <Separator />
                            <div>
                              <h4 className="font-medium mb-2">Requirements</h4>
                              <ul className="list-disc list-inside space-y-1 text-sm text-muted-foreground">
                                {selectedTemplate.metadata.requirements.map((req, idx) => (
                                  <li key={idx}>{req}</li>
                                ))}
                              </ul>
                            </div>
                          </>
                        )}
                      </CardContent>
                    </Card>
                  </div>

                  <div className="space-y-4">
                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base">Field Mappings</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <ScrollArea className="h-[400px]">
                          <div className="space-y-3">
                            {selectedTemplate.mappings.map((mapping, idx) => (
                              <div key={mapping.id} className="p-3 border rounded space-y-2">
                                <div className="flex items-center justify-between">
                                  <div className="flex items-center gap-2">
                                    <Badge variant="outline">{mapping.mappingType}</Badge>
                                    {mapping.required && <Badge variant="destructive">Required</Badge>}
                                  </div>
                                </div>
                                <div className="grid grid-cols-2 gap-2 text-sm">
                                  <div>
                                    <Label className="text-xs">Source</Label>
                                    <code className="block p-1 bg-muted rounded text-xs">
                                      {mapping.sourcePath || 'N/A'}
                                    </code>
                                  </div>
                                  <div>
                                    <Label className="text-xs">Target</Label>
                                    <code className="block p-1 bg-muted rounded text-xs">
                                      {mapping.targetPath}
                                    </code>
                                  </div>
                                </div>
                                {mapping.transformation && (
                                  <div>
                                    <Label className="text-xs">Transformation</Label>
                                    <code className="block p-1 bg-muted rounded text-xs">
                                      {mapping.transformation}
                                    </code>
                                  </div>
                                )}
                                {mapping.description && (
                                  <p className="text-xs text-muted-foreground">{mapping.description}</p>
                                )}
                              </div>
                            ))}
                          </div>
                        </ScrollArea>
                      </CardContent>
                    </Card>

                    {selectedTemplate.metadata.examples.length > 0 && (
                      <Card>
                        <CardHeader>
                          <CardTitle className="text-base">Examples</CardTitle>
                        </CardHeader>
                        <CardContent>
                          <Tabs defaultValue="0">
                            <TabsList>
                              {selectedTemplate.metadata.examples.map((_, idx) => (
                                <TabsTrigger key={idx} value={idx.toString()}>
                                  Example {idx + 1}
                                </TabsTrigger>
                              ))}
                            </TabsList>
                            {selectedTemplate.metadata.examples.map((example, idx) => (
                              <TabsContent key={idx} value={idx.toString()} className="space-y-3">
                                <p className="text-sm">{example.description}</p>
                                <div className="grid grid-cols-2 gap-4">
                                  <div>
                                    <Label className="text-sm">Source Data</Label>
                                    <pre className="mt-1 p-2 bg-muted rounded text-xs overflow-x-auto">
                                      {typeof example.sourceData === 'string' 
                                        ? example.sourceData 
                                        : JSON.stringify(example.sourceData, null, 2)}
                                    </pre>
                                  </div>
                                  <div>
                                    <Label className="text-sm">Target Data</Label>
                                    <pre className="mt-1 p-2 bg-muted rounded text-xs overflow-x-auto">
                                      {typeof example.targetData === 'string'
                                        ? example.targetData
                                        : JSON.stringify(example.targetData, null, 2)}
                                    </pre>
                                  </div>
                                </div>
                              </TabsContent>
                            ))}
                          </Tabs>
                        </CardContent>
                      </Card>
                    )}
                  </div>
                </div>
              ) : (
                <Card>
                  <CardContent className="pt-6">
                    <div className="text-center py-12">
                      <ArrowRightLeft className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground">Select a mapping template to view details</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value="visual" className="mt-4">
              {selectedTemplate ? (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Visual Mapping Flow - {selectedTemplate.name}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="h-[500px] border rounded-lg">
                      <ReactFlowProvider>
                        <ReactFlow
                          {...getMappingFlowNodes(selectedTemplate)}
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
              ) : (
                <Card>
                  <CardContent className="pt-6">
                    <div className="text-center py-12">
                      <GitBranch className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground">Select a mapping template to view visual flow</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Test Dialog */}
      {showTestDialog && selectedTemplate && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50" onClick={() => setShowTestDialog(false)}>
          <div className="fixed inset-4 md:inset-auto md:left-1/2 md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:w-[800px] bg-background border rounded-lg shadow-lg" onClick={(e) => e.stopPropagation()}>
            <div className="p-6 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">Test Mapping Template</h3>
                <Badge variant="outline">{selectedTemplate.name}</Badge>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Source Data ({selectedTemplate.sourceFormat})</Label>
                  <Editor
                    height="350px"
                    language={selectedTemplate.sourceFormat.toLowerCase() === 'xml' ? 'xml' : 'json'}
                    theme="vs-dark"
                    value={testSourceData}
                    onChange={(value) => setTestSourceData(value || '{}')}
                    options={{
                      minimap: { enabled: false },
                      fontSize: 12,
                      wordWrap: 'on'
                    }}
                  />
                </div>
                
                <div className="space-y-2">
                  <Label>Target Data ({selectedTemplate.targetFormat})</Label>
                  {testResult ? (
                    <Editor
                      height="350px"
                      language={selectedTemplate.targetFormat.toLowerCase() === 'xml' ? 'xml' : 'json'}
                      theme="vs-dark"
                      value={typeof testResult.targetData === 'string' 
                        ? testResult.targetData 
                        : JSON.stringify(testResult.targetData, null, 2)}
                      options={{
                        readOnly: true,
                        minimap: { enabled: false },
                        fontSize: 12,
                        wordWrap: 'on'
                      }}
                    />
                  ) : (
                    <div className="h-[350px] flex items-center justify-center border rounded">
                      <p className="text-muted-foreground">Run test to see results</p>
                    </div>
                  )}
                </div>
              </div>

              {testResult?.errors && testResult.errors.length > 0 && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Mapping Errors</AlertTitle>
                  <AlertDescription>
                    <ul className="list-disc list-inside mt-2">
                      {testResult.errors.map((error: string, idx: number) => (
                        <li key={idx} className="text-sm">{error}</li>
                      ))}
                    </ul>
                  </AlertDescription>
                </Alert>
              )}

              <div className="flex justify-end gap-2 pt-4">
                <Button variant="outline" onClick={() => setShowTestDialog(false)}>
                  Close
                </Button>
                <Button onClick={handleTestTemplate}>
                  <TestTube className="mr-2 h-4 w-4" />
                  Run Test
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
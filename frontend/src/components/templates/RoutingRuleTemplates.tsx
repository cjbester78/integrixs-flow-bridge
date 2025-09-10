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
  GitBranch,
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
  Zap,
  Code,
  FileCode,
  Share2,
  ArrowRight,
  CheckCircle2,
  AlertCircle,
  Info,
  Edit,
  Eye,
  TestTube,
  Shuffle,
  GitMerge,
  Split,
  Calendar,
  User
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import { Monaco } from '@monaco-editor/react';
import Editor from '@monaco-editor/react';

interface RoutingRuleTemplatesProps {
  onSelectRule?: (rule: RoutingRuleTemplate) => void;
  showActions?: boolean;
  className?: string;
}

interface RoutingRuleTemplate {
  id: string;
  name: string;
  description?: string;
  category: 'CONTENT_BASED' | 'HEADER_BASED' | 'TIME_BASED' | 'LOAD_BALANCING' | 'CONDITIONAL' | 'CUSTOM';
  type: 'SIMPLE' | 'COMPLEX' | 'COMPOSITE';
  conditionType: 'ALWAYS' | 'EXPRESSION' | 'XPATH' | 'JSONPATH' | 'HEADER_MATCH' | 'TIME_WINDOW' | 'CUSTOM';
  expression: string;
  targets: RuleTarget[];
  metadata: {
    language: 'SPEL' | 'JAVASCRIPT' | 'GROOVY' | 'XPATH' | 'JSONPATH';
    variables: Variable[];
    examples: Example[];
    performance: 'HIGH' | 'MEDIUM' | 'LOW';
    complexity: 'SIMPLE' | 'MODERATE' | 'COMPLEX';
  };
  configuration: {
    cacheResults: boolean;
    timeout: number;
    fallbackTarget?: string;
    errorHandling: 'FAIL' | 'FALLBACK' | 'IGNORE';
  };
  tags: string[];
  author: string;
  createdAt: string;
  updatedAt: string;
  version: string;
  isPublic: boolean;
  isFavorite: boolean;
  usageCount: number;
  successRate: number;
}

interface RuleTarget {
  id: string;
  name: string;
  condition?: string;
  priority: number;
  weight?: number;
}

interface Variable {
  name: string;
  type: string;
  description: string;
  example: any;
}

interface Example {
  input: any;
  expectedTarget: string;
  description: string;
}

const RULE_CATEGORIES = [
  {
    value: 'CONTENT_BASED',
    label: 'Content Based',
    icon: FileCode,
    description: 'Route based on message content'
  },
  {
    value: 'HEADER_BASED',
    label: 'Header Based',
    icon: Code,
    description: 'Route based on message headers'
  },
  {
    value: 'TIME_BASED',
    label: 'Time Based',
    icon: Calendar,
    description: 'Route based on time conditions'
  },
  {
    value: 'LOAD_BALANCING',
    label: 'Load Balancing',
    icon: Shuffle,
    description: 'Distribute load across targets'
  },
  {
    value: 'CONDITIONAL',
    label: 'Conditional',
    icon: GitBranch,
    description: 'Complex conditional routing'
  },
  {
    value: 'CUSTOM',
    label: 'Custom',
    icon: Zap,
    description: 'User-defined routing logic'
  }
];

const DEFAULT_TEMPLATES: RoutingRuleTemplate[] = [
  {
    id: 'content-type-router',
    name: 'Content Type Router',
    description: 'Route messages based on content type header',
    category: 'HEADER_BASED',
    type: 'SIMPLE',
    conditionType: 'HEADER_MATCH',
    expression: "headers['Content-Type']",
    targets: [
      {
        id: 't1',
        name: 'JSON Handler',
        condition: "contains('application/json')",
        priority: 1
      },
      {
        id: 't2',
        name: 'XML Handler',
        condition: "contains('application/xml') || contains('text/xml')",
        priority: 2
      },
      {
        id: 't3',
        name: 'Default Handler',
        condition: "true",
        priority: 99
      }
    ],
    metadata: {
      language: 'SPEL',
      variables: [
        {
          name: 'headers',
          type: 'Map<String, String>',
          description: 'Message headers',
          example: { 'Content-Type': 'application/json' }
        }
      ],
      examples: [
        {
          input: { headers: { 'Content-Type': 'application/json' } },
          expectedTarget: 'JSON Handler',
          description: 'JSON content type'
        },
        {
          input: { headers: { 'Content-Type': 'text/xml; charset=utf-8' } },
          expectedTarget: 'XML Handler',
          description: 'XML content type with charset'
        }
      ],
      performance: 'HIGH',
      complexity: 'SIMPLE'
    },
    configuration: {
      cacheResults: true,
      timeout: 100,
      fallbackTarget: 'Default Handler',
      errorHandling: 'FALLBACK'
    },
    tags: ['headers', 'content-type', 'basic'],
    author: 'System',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: '1.0.0',
    isPublic: true,
    isFavorite: false,
    usageCount: 234,
    successRate: 99.5
  },
  {
    id: 'round-robin-balancer',
    name: 'Round Robin Load Balancer',
    description: 'Distribute requests evenly across multiple targets',
    category: 'LOAD_BALANCING',
    type: 'SIMPLE',
    conditionType: 'EXPRESSION',
    expression: "counter.incrementAndGet() % targets.size()",
    targets: [
      {
        id: 't1',
        name: 'Server 1',
        priority: 1,
        weight: 1
      },
      {
        id: 't2',
        name: 'Server 2',
        priority: 1,
        weight: 1
      },
      {
        id: 't3',
        name: 'Server 3',
        priority: 1,
        weight: 1
      }
    ],
    metadata: {
      language: 'SPEL',
      variables: [
        {
          name: 'counter',
          type: 'AtomicLong',
          description: 'Request counter',
          example: 0
        },
        {
          name: 'targets',
          type: 'List<Target>',
          description: 'Available targets',
          example: ['Server 1', 'Server 2', 'Server 3']
        }
      ],
      examples: [
        {
          input: { counter: 0 },
          expectedTarget: 'Server 1',
          description: 'First request'
        },
        {
          input: { counter: 1 },
          expectedTarget: 'Server 2',
          description: 'Second request'
        }
      ],
      performance: 'HIGH',
      complexity: 'SIMPLE'
    },
    configuration: {
      cacheResults: false,
      timeout: 50,
      errorHandling: 'FAIL'
    },
    tags: ['load-balancing', 'round-robin', 'performance'],
    author: 'System',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: '1.0.0',
    isPublic: true,
    isFavorite: false,
    usageCount: 156,
    successRate: 98.9
  },
  {
    id: 'business-hours-router',
    name: 'Business Hours Router',
    description: 'Route based on business hours and time zones',
    category: 'TIME_BASED',
    type: 'COMPLEX',
    conditionType: 'TIME_WINDOW',
    expression: `
def now = new Date()
def hour = now.hours
def dayOfWeek = now.day

if (dayOfWeek >= 1 && dayOfWeek <= 5) {
  if (hour >= 9 && hour < 17) {
    return 'business-hours'
  }
}
return 'after-hours'
`,
    targets: [
      {
        id: 't1',
        name: 'Business Hours Service',
        condition: "result == 'business-hours'",
        priority: 1
      },
      {
        id: 't2',
        name: 'After Hours Service',
        condition: "result == 'after-hours'",
        priority: 1
      }
    ],
    metadata: {
      language: 'GROOVY',
      variables: [
        {
          name: 'timezone',
          type: 'String',
          description: 'Target timezone',
          example: 'America/New_York'
        }
      ],
      examples: [
        {
          input: { timestamp: '2024-01-15T10:00:00' },
          expectedTarget: 'Business Hours Service',
          description: 'Monday 10 AM'
        },
        {
          input: { timestamp: '2024-01-15T19:00:00' },
          expectedTarget: 'After Hours Service',
          description: 'Monday 7 PM'
        }
      ],
      performance: 'MEDIUM',
      complexity: 'MODERATE'
    },
    configuration: {
      cacheResults: true,
      timeout: 200,
      errorHandling: 'FAIL'
    },
    tags: ['time-based', 'business-hours', 'scheduling'],
    author: 'System',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: '1.0.0',
    isPublic: true,
    isFavorite: false,
    usageCount: 89,
    successRate: 99.8
  }
];

export function RoutingRuleTemplates({
  onSelectRule,
  showActions = true,
  className
}: RoutingRuleTemplatesProps) {
  const { toast } = useToast();
  const [rules, setRules] = useState<RoutingRuleTemplate[]>(DEFAULT_TEMPLATES);
  const [loading, setLoading] = useState(false);
  const [selectedRule, setSelectedRule] = useState<RoutingRuleTemplate | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showTestDialog, setShowTestDialog] = useState(false);
  const [testInput, setTestInput] = useState('{}');
  const [testResult, setTestResult] = useState<any>(null);
  const [newRule, setNewRule] = useState<Partial<RoutingRuleTemplate>>({
    name: '',
    description: '',
    category: 'CUSTOM',
    type: 'SIMPLE',
    conditionType: 'EXPRESSION',
    expression: '',
    targets: []
  });

  // Fetch rules from API
  const fetchRules = useCallback(async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/api/templates/routing-rules');
      setRules([...DEFAULT_TEMPLATES, ...response.data]);
    } catch (error) {
      console.error('Error fetching routing rules:', error);
      // Use default templates as fallback
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchRules();
  }, [fetchRules]);

  const filteredRules = useMemo(() => {
    return rules.filter(rule => {
      const matchesSearch = !searchTerm || 
        rule.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        rule.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        rule.tags.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase()));
      
      const matchesCategory = selectedCategory === 'all' || rule.category === selectedCategory;
      const matchesFavorites = !showFavoritesOnly || rule.isFavorite;
      
      return matchesSearch && matchesCategory && matchesFavorites;
    });
  }, [rules, searchTerm, selectedCategory, showFavoritesOnly]);

  const handleToggleFavorite = async (ruleId: string) => {
    try {
      const rule = rules.find(r => r.id === ruleId);
      if (!rule) return;

      setRules(prev => prev.map(r => 
        r.id === ruleId ? { ...r, isFavorite: !r.isFavorite } : r
      ));

      await apiClient.put(`/api/templates/routing-rules/${ruleId}/favorite`, {
        isFavorite: !rule.isFavorite
      });
    } catch (error) {
      const rule = rules.find(r => r.id === ruleId);
      if (rule) {
        setRules(prev => prev.map(r => 
          r.id === ruleId ? { ...r, isFavorite: !rule.isFavorite } : r
        ));
      }
      toast({
        title: 'Error',
        description: 'Failed to update favorite status',
        variant: 'destructive'
      });
    }
  };

  const handleDeleteRule = async (ruleId: string) => {
    try {
      await apiClient.delete(`/api/templates/routing-rules/${ruleId}`);
      setRules(prev => prev.filter(r => r.id !== ruleId));
      toast({
        title: 'Success',
        description: 'Routing rule deleted successfully'
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to delete routing rule',
        variant: 'destructive'
      });
    }
  };

  const handleDuplicateRule = async (rule: RoutingRuleTemplate) => {
    try {
      const duplicated = {
        ...rule,
        id: undefined,
        name: `${rule.name} (Copy)`,
        author: 'Current User',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        usageCount: 0,
        successRate: 0,
        isFavorite: false
      };

      const response = await apiClient.post('/api/templates/routing-rules', duplicated);
      setRules(prev => [...prev, response.data]);
      
      toast({
        title: 'Success',
        description: 'Routing rule duplicated successfully'
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to duplicate routing rule',
        variant: 'destructive'
      });
    }
  };

  const handleTestRule = async () => {
    if (!selectedRule || !testInput) return;

    try {
      const input = JSON.parse(testInput);
      const response = await apiClient.post('/api/templates/routing-rules/test', {
        rule: selectedRule,
        input
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
          description: error.message || 'Failed to test routing rule',
          variant: 'destructive'
        });
      }
    }
  };

  const exportRule = (rule: RoutingRuleTemplate) => {
    const data = JSON.stringify(rule, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `routing-rule-${rule.name.toLowerCase().replace(/\s+/g, '-')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const importRule = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const rule = JSON.parse(e.target?.result as string);
        const response = await apiClient.post('/api/templates/routing-rules', {
          ...rule,
          id: undefined,
          author: 'Current User',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        });
        
        setRules(prev => [...prev, response.data]);
        toast({
          title: 'Success',
          description: 'Routing rule imported successfully'
        });
      } catch (error) {
        toast({
          title: 'Error',
          description: 'Failed to import routing rule',
          variant: 'destructive'
        });
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className={cn("space-y-6", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <GitBranch className="h-5 w-5" />
                Routing Rule Templates
              </CardTitle>
              <CardDescription>
                Reusable routing rules for message distribution
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
                  onChange={importRule}
                  className="hidden"
                />
              </Label>
              {showActions && (
                <Button onClick={() => setShowCreateDialog(true)}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Rule
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
                  placeholder="Search routing rules..."
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
                {RULE_CATEGORIES.map(category => (
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

          <Tabs defaultValue="cards">
            <TabsList>
              <TabsTrigger value="cards">Card View</TabsTrigger>
              <TabsTrigger value="table">Table View</TabsTrigger>
              <TabsTrigger value="editor">Rule Editor</TabsTrigger>
            </TabsList>

            <TabsContent value="cards" className="mt-4">
              <div className="grid grid-cols-2 gap-4">
                {filteredRules.map(rule => {
                  const category = RULE_CATEGORIES.find(c => c.value === rule.category);
                  const Icon = category?.icon || Zap;
                  
                  return (
                    <Card
                      key={rule.id}
                      className={cn(
                        "cursor-pointer transition-colors hover:border-primary",
                        selectedRule?.id === rule.id && "border-primary"
                      )}
                      onClick={() => setSelectedRule(rule)}
                    >
                      <CardHeader>
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <Icon className="h-5 w-5 text-muted-foreground" />
                            <CardTitle className="text-base">{rule.name}</CardTitle>
                          </div>
                          <div className="flex items-center gap-1">
                            <Button
                              size="icon"
                              variant="ghost"
                              className="h-8 w-8"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleToggleFavorite(rule.id);
                              }}
                            >
                              {rule.isFavorite ? (
                                <Star className="h-4 w-4 fill-current text-yellow-500" />
                              ) : (
                                <StarOff className="h-4 w-4" />
                              )}
                            </Button>
                          </div>
                        </div>
                        <CardDescription>{rule.description}</CardDescription>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-3">
                          <div className="grid grid-cols-2 gap-4 text-sm">
                            <div>
                              <p className="text-muted-foreground">Type</p>
                              <Badge variant="outline">{rule.conditionType}</Badge>
                            </div>
                            <div>
                              <p className="text-muted-foreground">Targets</p>
                              <p className="font-medium">{rule.targets.length}</p>
                            </div>
                            <div>
                              <p className="text-muted-foreground">Success Rate</p>
                              <p className="font-medium">{rule.successRate}%</p>
                            </div>
                            <div>
                              <p className="text-muted-foreground">Usage</p>
                              <p className="font-medium">{rule.usageCount}</p>
                            </div>
                          </div>
                          <Separator />
                          <div>
                            <p className="text-sm text-muted-foreground mb-2">Expression Preview</p>
                            <code className="text-xs bg-muted p-2 rounded block overflow-x-auto">
                              {rule.expression.split('\n')[0]}...
                            </code>
                          </div>
                          {rule.tags.length > 0 && (
                            <div className="flex flex-wrap gap-1">
                              {rule.tags.map(tag => (
                                <Badge key={tag} variant="secondary" className="text-xs">
                                  {tag}
                                </Badge>
                              ))}
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
                                onSelectRule?.(rule);
                              }}
                            >
                              Use
                            </Button>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedRule(rule);
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
                                handleDuplicateRule(rule);
                              }}
                            >
                              <Copy className="h-4 w-4" />
                            </Button>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                exportRule(rule);
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

            <TabsContent value="table" className="mt-4">
              <ScrollArea className="h-[600px]">
                <table className="w-full">
                  <thead className="border-b">
                    <tr>
                      <th className="text-left p-2">Name</th>
                      <th className="text-left p-2">Category</th>
                      <th className="text-left p-2">Type</th>
                      <th className="text-left p-2">Targets</th>
                      <th className="text-left p-2">Success Rate</th>
                      <th className="text-left p-2">Usage</th>
                      <th className="text-left p-2">Author</th>
                      <th className="text-left p-2">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredRules.map(rule => (
                      <tr
                        key={rule.id}
                        className={cn(
                          "border-b cursor-pointer hover:bg-muted/50",
                          selectedRule?.id === rule.id && "bg-muted/50"
                        )}
                        onClick={() => setSelectedRule(rule)}
                      >
                        <td className="p-2">
                          <div className="flex items-center gap-2">
                            {rule.isFavorite && <Star className="h-4 w-4 fill-current text-yellow-500" />}
                            <span className="font-medium">{rule.name}</span>
                          </div>
                        </td>
                        <td className="p-2">
                          <Badge variant="outline">{rule.category}</Badge>
                        </td>
                        <td className="p-2">{rule.conditionType}</td>
                        <td className="p-2">{rule.targets.length}</td>
                        <td className="p-2">{rule.successRate}%</td>
                        <td className="p-2">{rule.usageCount}</td>
                        <td className="p-2">{rule.author}</td>
                        <td className="p-2">
                          <div className="flex items-center gap-1">
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedRule(rule);
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
                                exportRule(rule);
                              }}
                            >
                              <Download className="h-4 w-4" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </ScrollArea>
            </TabsContent>

            <TabsContent value="editor" className="mt-4">
              {selectedRule ? (
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base">{selectedRule.name}</CardTitle>
                        <CardDescription>{selectedRule.description}</CardDescription>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <Label className="text-muted-foreground">Category</Label>
                            <p className="font-medium">{selectedRule.category}</p>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Condition Type</Label>
                            <p className="font-medium">{selectedRule.conditionType}</p>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Language</Label>
                            <p className="font-medium">{selectedRule.metadata.language}</p>
                          </div>
                          <div>
                            <Label className="text-muted-foreground">Performance</Label>
                            <Badge
                              variant={
                                selectedRule.metadata.performance === 'HIGH' ? 'default' :
                                selectedRule.metadata.performance === 'MEDIUM' ? 'secondary' : 'outline'
                              }
                            >
                              {selectedRule.metadata.performance}
                            </Badge>
                          </div>
                        </div>

                        <Separator />

                        <div>
                          <Label className="text-muted-foreground">Targets</Label>
                          <div className="space-y-2 mt-2">
                            {selectedRule.targets.map((target, idx) => (
                              <div key={target.id} className="flex items-center justify-between p-2 border rounded">
                                <div>
                                  <p className="font-medium">{target.name}</p>
                                  {target.condition && (
                                    <code className="text-xs text-muted-foreground">{target.condition}</code>
                                  )}
                                </div>
                                <Badge variant="outline">Priority {target.priority}</Badge>
                              </div>
                            ))}
                          </div>
                        </div>

                        <div>
                          <Label className="text-muted-foreground">Configuration</Label>
                          <div className="space-y-1 mt-2 text-sm">
                            <div className="flex justify-between">
                              <span>Cache Results</span>
                              <span>{selectedRule.configuration.cacheResults ? 'Yes' : 'No'}</span>
                            </div>
                            <div className="flex justify-between">
                              <span>Timeout</span>
                              <span>{selectedRule.configuration.timeout}ms</span>
                            </div>
                            <div className="flex justify-between">
                              <span>Error Handling</span>
                              <span>{selectedRule.configuration.errorHandling}</span>
                            </div>
                          </div>
                        </div>

                        <div>
                          <Label className="text-muted-foreground">Variables</Label>
                          <div className="space-y-2 mt-2">
                            {selectedRule.metadata.variables.map((variable, idx) => (
                              <div key={idx} className="p-2 border rounded">
                                <div className="flex items-center justify-between">
                                  <code className="font-medium">{variable.name}</code>
                                  <Badge variant="outline">{variable.type}</Badge>
                                </div>
                                <p className="text-xs text-muted-foreground mt-1">{variable.description}</p>
                              </div>
                            ))}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  </div>

                  <div className="space-y-4">
                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base">Expression</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <Editor
                          height="300px"
                          language={selectedRule.metadata.language.toLowerCase()}
                          theme="vs-dark"
                          value={selectedRule.expression}
                          options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            fontSize: 12,
                            wordWrap: 'on'
                          }}
                        />
                      </CardContent>
                    </Card>

                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base">Examples</CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        {selectedRule.metadata.examples.map((example, idx) => (
                          <div key={idx} className="space-y-2">
                            <p className="text-sm font-medium">{example.description}</p>
                            <div className="grid grid-cols-2 gap-2">
                              <div>
                                <Label className="text-xs">Input</Label>
                                <pre className="text-xs bg-muted p-2 rounded overflow-x-auto">
                                  {JSON.stringify(example.input, null, 2)}
                                </pre>
                              </div>
                              <div>
                                <Label className="text-xs">Expected Target</Label>
                                <p className="text-sm font-medium bg-muted p-2 rounded">
                                  {example.expectedTarget}
                                </p>
                              </div>
                            </div>
                            {idx < selectedRule.metadata.examples.length - 1 && <Separator />}
                          </div>
                        ))}
                      </CardContent>
                    </Card>
                  </div>
                </div>
              ) : (
                <Card>
                  <CardContent className="pt-6">
                    <div className="text-center py-12">
                      <Code className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground">Select a routing rule to view details</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Test Dialog */}
      {showTestDialog && selectedRule && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50" onClick={() => setShowTestDialog(false)}>
          <div className="fixed inset-4 md:inset-auto md:left-1/2 md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:w-[700px] bg-background border rounded-lg shadow-lg" onClick={(e) => e.stopPropagation()}>
            <div className="p-6 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">Test Routing Rule</h3>
                <Badge variant="outline">{selectedRule.name}</Badge>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Test Input (JSON)</Label>
                  <Editor
                    height="300px"
                    language="json"
                    theme="vs-dark"
                    value={testInput}
                    onChange={(value) => setTestInput(value || '{}')}
                    options={{
                      minimap: { enabled: false },
                      fontSize: 12,
                      wordWrap: 'on'
                    }}
                  />
                </div>
                
                <div className="space-y-2">
                  <Label>Test Result</Label>
                  {testResult ? (
                    <div className="h-[300px] overflow-y-auto border rounded p-4 space-y-2">
                      <div className="p-3 rounded bg-muted">
                        <p className="text-sm font-medium">Selected Target</p>
                        <p className="text-lg">{testResult.selectedTarget || 'No target matched'}</p>
                      </div>
                      {testResult.evaluationSteps && (
                        <div className="space-y-1">
                          <p className="text-sm font-medium">Evaluation Steps</p>
                          {testResult.evaluationSteps.map((step: any, idx: number) => (
                            <div key={idx} className="text-xs p-2 border rounded flex items-center gap-2">
                              {step.success ? (
                                <CheckCircle2 className="h-3 w-3 text-green-500" />
                              ) : (
                                <AlertCircle className="h-3 w-3 text-red-500" />
                              )}
                              <span>{step.description}</span>
                            </div>
                          ))}
                        </div>
                      )}
                      {testResult.executionTime && (
                        <p className="text-xs text-muted-foreground">
                          Execution time: {testResult.executionTime}ms
                        </p>
                      )}
                    </div>
                  ) : (
                    <div className="h-[300px] flex items-center justify-center border rounded">
                      <p className="text-muted-foreground">Run test to see results</p>
                    </div>
                  )}
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-4">
                <Button variant="outline" onClick={() => setShowTestDialog(false)}>
                  Close
                </Button>
                <Button onClick={handleTestRule}>
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
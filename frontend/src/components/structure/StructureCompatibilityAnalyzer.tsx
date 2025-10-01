import React, { useState, useCallback, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Progress } from '@/components/ui/progress';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import {
  AlertTriangle,
  CheckCircle2,
  XCircle,
  ArrowRight,
  ChevronDown,
  ChevronRight,
  FileCode,
  Zap,
  AlertCircle,
  Shield,
  GitCompare,
  RefreshCw,
  Loader2
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { StructureCompatibilityService } from '@/services/structureCompatibilityService';
import type {
  CompatibilityIssue as ServiceCompatibilityIssue,
  FieldMapping as ServiceFieldMapping,
  StructureCompatibilityResponse
} from '@/services/structureCompatibilityService';

interface StructureCompatibilityAnalyzerProps {
  sourceStructure?: {
    type: 'WSDL' | 'JSON_SCHEMA' | 'XSD';
    content: string;
  };
  targetStructure?: {
    type: 'WSDL' | 'JSON_SCHEMA' | 'XSD';
    content: string;
  };
  onCompatibilityChange?: (isCompatible: boolean, analysis: CompatibilityAnalysis) => void;
  autoAnalyze?: boolean;
  className?: string;
}

interface CompatibilityIssue {
  severity: 'ERROR' | 'WARNING' | 'INFO';
  category: 'TYPE_MISMATCH' | 'MISSING_FIELD' | 'FORMAT_DIFFERENCE' | 'CONSTRAINT_CONFLICT' | 'NAMESPACE_ISSUE' | 'OTHER';
  sourcePath: string;
  targetPath?: string;
  message: string;
  suggestion?: string;
}

interface FieldMapping {
  sourcePath: string;
  targetPath: string;
  sourceType?: string;
  targetType?: string;
  compatible: boolean;
  transformationRequired: boolean;
  transformationHint?: string;
}

interface CompatibilityAnalysis {
  overallCompatibility: number; // 0-100
  isCompatible: boolean;
  issues: CompatibilityIssue[];
  mappings: FieldMapping[];
  sourceMetadata?: {
    fields: Array<{ path: string; type: string; required: boolean }>;
    namespaces?: Record<string, string>;
  };
  targetMetadata?: {
    fields: Array<{ path: string; type: string; required: boolean }>;
    namespaces?: Record<string, string>;
  };
  recommendations: string[];
}

export function StructureCompatibilityAnalyzer({
  sourceStructure,
  targetStructure,
  onCompatibilityChange,
  autoAnalyze = true,
  className
}: StructureCompatibilityAnalyzerProps) {
  const { toast } = useToast();
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysis, setAnalysis] = useState<CompatibilityAnalysis | null>(null);
  const [expandedCategories, setExpandedCategories] = useState<Record<string, boolean>>({
    TYPE_MISMATCH: true,
    MISSING_FIELD: true,
    FORMAT_DIFFERENCE: false,
    CONSTRAINT_CONFLICT: false,
    NAMESPACE_ISSUE: false,
    OTHER: false
  });

  const analyzeCompatibility = useCallback(async () => {
    if (!sourceStructure || !targetStructure) {
      setAnalysis(null);
      return;
    }

    setIsAnalyzing(true);

    try {
      const response = await StructureCompatibilityService.analyzeCompatibility({
        sourceContent: sourceStructure.content,
        sourceType: sourceStructure.type,
        targetContent: targetStructure.content,
        targetType: targetStructure.type,
        includeDetailedAnalysis: true,
        generateMappingSuggestions: true
      });

      // Convert service response to component format
      const analysis: CompatibilityAnalysis = {
        overallCompatibility: response.overallCompatibility,
        isCompatible: response.isCompatible,
        issues: response.issues,
        mappings: response.mappings,
        sourceMetadata: response.sourceMetadata,
        targetMetadata: response.targetMetadata,
        recommendations: response.recommendations
      };

      setAnalysis(analysis);
      onCompatibilityChange?.(analysis.isCompatible, analysis);

    } catch (error) {
      console.error('Compatibility analysis error:', error);
      toast({
        title: 'Analysis Error',
        description: 'Failed to analyze structure compatibility',
        variant: 'destructive'
      });
      
      // Create error analysis result
      const errorAnalysis: CompatibilityAnalysis = {
        overallCompatibility: 0,
        isCompatible: false,
        issues: [{
          severity: 'ERROR',
          category: 'OTHER',
          sourcePath: '',
          message: error instanceof Error ? error.message : 'Unknown error occurred',
        }],
        mappings: [],
        recommendations: []
      };
      
      setAnalysis(errorAnalysis);
      onCompatibilityChange?.(false, errorAnalysis);
    } finally {
      setIsAnalyzing(false);
    }
  }, [sourceStructure, targetStructure, onCompatibilityChange, toast]);

  // Auto-analyze when structures change
  useEffect(() => {
    if (autoAnalyze && sourceStructure && targetStructure) {
      analyzeCompatibility();
    }
  }, [sourceStructure, targetStructure, autoAnalyze, analyzeCompatibility]);

  const toggleCategory = (category: string) => {
    setExpandedCategories(prev => ({
      ...prev,
      [category]: !prev[category]
    }));
  };

  const getIssueCategoryIcon = (category: CompatibilityIssue['category']) => {
    switch (category) {
      case 'TYPE_MISMATCH':
        return <AlertTriangle className="h-4 w-4" />;
      case 'MISSING_FIELD':
        return <XCircle className="h-4 w-4" />;
      case 'FORMAT_DIFFERENCE':
        return <GitCompare className="h-4 w-4" />;
      case 'CONSTRAINT_CONFLICT':
        return <Shield className="h-4 w-4" />;
      case 'NAMESPACE_ISSUE':
        return <FileCode className="h-4 w-4" />;
      case 'OTHER':
        return <AlertCircle className="h-4 w-4" />;
    }
  };

  const getIssueCategoryLabel = (category: CompatibilityIssue['category']) => {
    switch (category) {
      case 'TYPE_MISMATCH':
        return 'Type Mismatches';
      case 'MISSING_FIELD':
        return 'Missing Fields';
      case 'FORMAT_DIFFERENCE':
        return 'Format Differences';
      case 'CONSTRAINT_CONFLICT':
        return 'Constraint Conflicts';
      case 'NAMESPACE_ISSUE':
        return 'Namespace Issues';
      case 'OTHER':
        return 'Other Issues';
    }
  };

  const getCompatibilityColor = (compatibility: number) => {
    if (compatibility >= 90) return 'text-green-600';
    if (compatibility >= 70) return 'text-yellow-600';
    if (compatibility >= 50) return 'text-orange-600';
    return 'text-red-600';
  };

  const groupIssuesByCategory = (issues: CompatibilityIssue[]) => {
    return issues.reduce((acc, issue) => {
      if (!acc[issue.category]) {
        acc[issue.category] = [];
      }
      acc[issue.category].push(issue);
      return acc;
    }, {} as Record<string, CompatibilityIssue[]>);
  };

  if (!sourceStructure || !targetStructure) {
    return (
      <Card className={className}>
        <CardContent className="pt-6">
          <div className="text-center space-y-4">
            <GitCompare className="w-12 h-12 mx-auto text-muted-foreground" />
            <div>
              <h3 className="font-medium">Structure Compatibility Analysis</h3>
              <p className="text-sm text-muted-foreground mt-1">
                Provide both source and target structures to analyze compatibility
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className={cn("space-y-4", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Structure Compatibility Analysis</CardTitle>
              <CardDescription>
                Analyze compatibility between source and target data structures
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              {!autoAnalyze && (
                <Button
                  size="sm"
                  onClick={analyzeCompatibility}
                  disabled={isAnalyzing}
                >
                  {isAnalyzing ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Analyzing...
                    </>
                  ) : (
                    <>
                      <RefreshCw className="mr-2 h-4 w-4" />
                      Analyze
                    </>
                  )}
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isAnalyzing ? (
            <div className="space-y-4 py-8">
              <div className="text-center">
                <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
                <p className="text-sm text-muted-foreground">Analyzing structure compatibility...</p>
              </div>
            </div>
          ) : analysis ? (
            <Tabs defaultValue="overview" className="w-full">
              <TabsList>
                <TabsTrigger value="overview">Overview</TabsTrigger>
                <TabsTrigger value="issues">
                  Issues
                  {analysis.issues.length > 0 && (
                    <Badge variant="outline" className="ml-2">
                      {analysis.issues.length}
                    </Badge>
                  )}
                </TabsTrigger>
                <TabsTrigger value="mappings">Mappings</TabsTrigger>
                <TabsTrigger value="recommendations">Recommendations</TabsTrigger>
              </TabsList>

              <TabsContent value="overview" className="space-y-4">
                <div className="grid gap-4">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium">Overall Compatibility</span>
                      <span className={cn("text-2xl font-bold", getCompatibilityColor(analysis.overallCompatibility))}>
                        {analysis.overallCompatibility}%
                      </span>
                    </div>
                    <Progress value={analysis.overallCompatibility} className="h-3" />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <Card>
                      <CardHeader className="pb-3">
                        <CardTitle className="text-sm">Source Structure</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-2 text-sm">
                          <div>
                            <Badge variant="secondary">{sourceStructure.type}</Badge>
                          </div>
                          {analysis.sourceMetadata && (
                            <div className="text-muted-foreground">
                              {analysis.sourceMetadata.fields.length} fields detected
                            </div>
                          )}
                        </div>
                      </CardContent>
                    </Card>

                    <Card>
                      <CardHeader className="pb-3">
                        <CardTitle className="text-sm">Target Structure</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-2 text-sm">
                          <div>
                            <Badge variant="secondary">{targetStructure.type}</Badge>
                          </div>
                          {analysis.targetMetadata && (
                            <div className="text-muted-foreground">
                              {analysis.targetMetadata.fields.length} fields detected
                            </div>
                          )}
                        </div>
                      </CardContent>
                    </Card>
                  </div>

                  <div className="grid grid-cols-3 gap-4 text-center">
                    <Card>
                      <CardContent className="pt-6">
                        <div className="text-2xl font-bold text-red-600">
                          {analysis.issues.filter(i => i.severity === 'ERROR').length}
                        </div>
                        <p className="text-xs text-muted-foreground">Errors</p>
                      </CardContent>
                    </Card>
                    <Card>
                      <CardContent className="pt-6">
                        <div className="text-2xl font-bold text-yellow-600">
                          {analysis.issues.filter(i => i.severity === 'WARNING').length}
                        </div>
                        <p className="text-xs text-muted-foreground">Warnings</p>
                      </CardContent>
                    </Card>
                    <Card>
                      <CardContent className="pt-6">
                        <div className="text-2xl font-bold text-blue-600">
                          {analysis.issues.filter(i => i.severity === 'INFO').length}
                        </div>
                        <p className="text-xs text-muted-foreground">Info</p>
                      </CardContent>
                    </Card>
                  </div>

                  <Alert className={analysis.isCompatible ? "border-green-200" : "border-yellow-200"}>
                    {analysis.isCompatible ? (
                      <CheckCircle2 className="h-4 w-4 text-green-600" />
                    ) : (
                      <AlertCircle className="h-4 w-4 text-yellow-600" />
                    )}
                    <AlertTitle>
                      {analysis.isCompatible ? 'Structures are compatible' : 'Structures have compatibility issues'}
                    </AlertTitle>
                    <AlertDescription>
                      {analysis.isCompatible
                        ? 'The structures can be mapped with minimal transformations.'
                        : 'Some transformations will be required to map between these structures.'}
                    </AlertDescription>
                  </Alert>
                </div>
              </TabsContent>

              <TabsContent value="issues">
                <ScrollArea className="h-[400px]">
                  {analysis.issues.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      <CheckCircle2 className="h-12 w-12 mx-auto mb-2" />
                      <p>No compatibility issues found</p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {Object.entries(groupIssuesByCategory(analysis.issues)).map(([category, issues]) => (
                        <Collapsible
                          key={category}
                          open={expandedCategories[category]}
                          onOpenChange={() => toggleCategory(category)}
                        >
                          <CollapsibleTrigger className="flex items-center gap-2 w-full p-2 hover:bg-muted/50 rounded">
                            {expandedCategories[category] ? (
                              <ChevronDown className="h-4 w-4" />
                            ) : (
                              <ChevronRight className="h-4 w-4" />
                            )}
                            {getIssueCategoryIcon(category as CompatibilityIssue['category'])}
                            <span className="font-medium">
                              {getIssueCategoryLabel(category as CompatibilityIssue['category'])}
                            </span>
                            <Badge variant="outline" className="ml-auto">
                              {issues.length}
                            </Badge>
                          </CollapsibleTrigger>
                          <CollapsibleContent>
                            <div className="space-y-2 pl-8 pt-2">
                              {issues.map((issue, index) => (
                                <Alert
                                  key={index}
                                  className={cn(
                                    "py-2",
                                    issue.severity === 'ERROR' && "border-red-200",
                                    issue.severity === 'WARNING' && "border-yellow-200",
                                    issue.severity === 'INFO' && "border-blue-200"
                                  )}
                                >
                                  <div className="space-y-1">
                                    <p className="text-sm font-medium">{issue.message}</p>
                                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                                      <code>{issue.sourcePath}</code>
                                      {issue.targetPath && (
                                        <>
                                          <ArrowRight className="h-3 w-3" />
                                          <code>{issue.targetPath}</code>
                                        </>
                                      )}
                                    </div>
                                    {issue.suggestion && (
                                      <p className="text-xs mt-2">
                                        <span className="font-medium">Suggestion:</span> {issue.suggestion}
                                      </p>
                                    )}
                                  </div>
                                </Alert>
                              ))}
                            </div>
                          </CollapsibleContent>
                        </Collapsible>
                      ))}
                    </div>
                  )}
                </ScrollArea>
              </TabsContent>

              <TabsContent value="mappings">
                <ScrollArea className="h-[400px]">
                  <div className="space-y-2">
                    {analysis.mappings.map((mapping, index) => (
                      <Card key={index} className="p-3">
                        <div className="flex items-start justify-between">
                          <div className="space-y-1 flex-1">
                            <div className="flex items-center gap-2">
                              <code className="text-sm">{mapping.sourcePath}</code>
                              <ArrowRight className="h-3 w-3 text-muted-foreground" />
                              <code className="text-sm">{mapping.targetPath}</code>
                            </div>
                            <div className="flex items-center gap-2 text-xs">
                              {mapping.sourceType && (
                                <Badge variant="outline">{mapping.sourceType}</Badge>
                              )}
                              <span className="text-muted-foreground">to</span>
                              {mapping.targetType && (
                                <Badge variant="outline">{mapping.targetType}</Badge>
                              )}
                            </div>
                            {mapping.transformationHint && (
                              <p className="text-xs text-muted-foreground mt-1">
                                <Zap className="h-3 w-3 inline mr-1" />
                                {mapping.transformationHint}
                              </p>
                            )}
                          </div>
                          <div className="ml-4">
                            {mapping.compatible ? (
                              <Badge variant="success">Compatible</Badge>
                            ) : (
                              <Badge variant="destructive">Incompatible</Badge>
                            )}
                          </div>
                        </div>
                      </Card>
                    ))}
                  </div>
                </ScrollArea>
              </TabsContent>

              <TabsContent value="recommendations">
                <div className="space-y-4">
                  {analysis.recommendations.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      <Zap className="h-12 w-12 mx-auto mb-2" />
                      <p>No specific recommendations at this time</p>
                    </div>
                  ) : (
                    analysis.recommendations.map((recommendation, index) => (
                      <Alert key={index}>
                        <Zap className="h-4 w-4" />
                        <AlertDescription>{recommendation}</AlertDescription>
                      </Alert>
                    ))
                  )}
                </div>
              </TabsContent>
            </Tabs>
          ) : null}
        </CardContent>
      </Card>
    </div>
  );
}
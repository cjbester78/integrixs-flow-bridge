import React, { useState, useCallback, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import {
  AlertCircle,
  AlertTriangle,
  Info,
  CheckCircle2,
  ChevronDown,
  ChevronRight,
  FileCode,
  Server,
  MessageSquare,
  Copy,
  Download,
  RefreshCw,
  Loader2,
  XCircle
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import Editor from '@monaco-editor/react';
import { StructureValidationService } from '@/services/structureValidationService';
import { useDebounce } from 'use-debounce';

interface WsdlValidatorProps {
  initialContent?: string;
  onChange?: (content: string, isValid: boolean) => void;
  onValidationComplete?: (result: any) => void;
  autoValidate?: boolean;
  className?: string;
  height?: string;
}

interface ValidationIssue {
  type: 'ERROR' | 'WARNING' | 'INFO';
  message: string;
  line?: number;
  column?: number;
  path?: string;
}

interface WsdlMetadata {
  targetNamespace?: string;
  version?: string;
  namespaces?: Record<string, string>;
  services?: Array<{
    name: string;
    ports: Array<{
      name: string;
      binding: string;
      address?: string;
    }>;
  }>;
  portTypes?: Array<{
    name: string;
    operations: Array<{
      name: string;
      inputMessage?: string;
      outputMessage?: string;
      faultMessages?: string[];
    }>;
  }>;
  messages?: Array<{
    name: string;
    parts: Array<{
      name: string;
      type?: string;
      element?: string;
    }>;
  }>;
}

export function WsdlValidator({
  initialContent = '',
  onChange,
  onValidationComplete,
  autoValidate = true,
  className,
  height = '400px'
}: WsdlValidatorProps) {
  const { toast } = useToast();
  const [content, setContent] = useState(initialContent);
  const [debouncedContent] = useDebounce(content, 500);
  const [isValidating, setIsValidating] = useState(false);
  const [validationResult, setValidationResult] = useState<{
    valid: boolean;
    message?: string;
    issues: ValidationIssue[];
    metadata?: WsdlMetadata;
  } | null>(null);
  const [expandedSections, setExpandedSections] = useState<Record<string, boolean>>({
    services: true,
    portTypes: true,
    messages: false,
    namespaces: false
  });

  const validateWsdl = useCallback(async (wsdlContent: string) => {
    if (!wsdlContent.trim()) {
      setValidationResult(null);
      return;
    }

    setIsValidating(true);
    try {
      const result = await StructureValidationService.validateStructure({
        content: wsdlContent,
        structureType: 'WSDL',
        strictMode: false,
        extractMetadata: true
      });

      const validationResult = {
        valid: result.valid,
        message: result.message,
        issues: result.issues || [],
        metadata: result.wsdlMetadata
      };

      setValidationResult(validationResult);
      onChange?.(wsdlContent, result.valid);
      onValidationComplete?.(validationResult);

    } catch (error) {
      console.error('WSDL validation error:', error);
      const errorResult = {
        valid: false,
        message: 'Validation service error',
        issues: [{
          type: 'ERROR' as const,
          message: error instanceof Error ? error.message : 'Unknown error'
        }]
      };
      setValidationResult(errorResult);
      onChange?.(wsdlContent, false);
      
    } finally {
      setIsValidating(false);
    }
  }, [onChange, onValidationComplete]);

  // Validate WSDL when content changes (debounced)
  useEffect(() => {
    if (autoValidate && debouncedContent.trim()) {
      validateWsdl(debouncedContent);
    }
  }, [debouncedContent, autoValidate, validateWsdl]);

  const handleContentChange = useCallback((value: string | undefined) => {
    if (value !== undefined) {
      setContent(value);
    }
  }, []);

  const handleManualValidation = useCallback(() => {
    validateWsdl(content);
  }, [content, validateWsdl]);

  const toggleSection = useCallback((section: string) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  }, []);

  const getIssueIcon = (type: ValidationIssue['type']) => {
    switch (type) {
      case 'ERROR':
        return <XCircle className="h-4 w-4 text-destructive" />;
      case 'WARNING':
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      case 'INFO':
        return <Info className="h-4 w-4 text-blue-500" />;
    }
  };

  const getIssueCounts = () => {
    if (!validationResult) return { errors: 0, warnings: 0, infos: 0 };
    
    return {
      errors: validationResult.issues.filter(i => i.type === 'ERROR').length,
      warnings: validationResult.issues.filter(i => i.type === 'WARNING').length,
      infos: validationResult.issues.filter(i => i.type === 'INFO').length
    };
  };

  const exportValidationReport = useCallback(() => {
    if (!validationResult) return;

    const report = {
      timestamp: new Date().toISOString(),
      wsdl: {
        content: content,
        length: content.length
      },
      validation: validationResult,
      exportedBy: 'WSDL Validator'
    };

    const blob = new Blob([JSON.stringify(report, null, 2)], {
      type: 'application/json'
    });
    
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `wsdl-validation-report-${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    toast({
      title: 'Report Exported',
      description: 'Validation report has been downloaded'
    });
  }, [content, validationResult, toast]);

  const copyToClipboard = useCallback((text: string, label: string) => {
    navigator.clipboard.writeText(text).then(() => {
      toast({
        title: 'Copied',
        description: `${label} copied to clipboard`
      });
    });
  }, [toast]);

  return (
    <div className={cn("space-y-4", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>WSDL Validator</CardTitle>
              <CardDescription>
                Real-time WSDL validation with syntax checking and metadata extraction
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              {!autoValidate && (
                <Button
                  size="sm"
                  onClick={handleManualValidation}
                  disabled={isValidating || !content.trim()}
                >
                  {isValidating ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Validating...
                    </>
                  ) : (
                    <>
                      <RefreshCw className="mr-2 h-4 w-4" />
                      Validate
                    </>
                  )}
                </Button>
              )}
              {validationResult && (
                <Button
                  size="sm"
                  variant="outline"
                  onClick={exportValidationReport}
                >
                  <Download className="mr-2 h-4 w-4" />
                  Export Report
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <Tabs defaultValue="editor" className="w-full">
            <TabsList className="w-full justify-start rounded-none border-b px-4">
              <TabsTrigger value="editor">
                Editor
                {validationResult && (
                  <Badge
                    variant={validationResult.valid ? "success" : "destructive"}
                    className="ml-2"
                  >
                    {validationResult.valid ? 'Valid' : 'Invalid'}
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="issues">
                Issues
                {validationResult && validationResult.issues.length > 0 && (
                  <Badge variant="outline" className="ml-2">
                    {validationResult.issues.length}
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="metadata" disabled={!validationResult?.metadata}>
                Metadata
              </TabsTrigger>
            </TabsList>

            <TabsContent value="editor" className="m-0">
              <div className="relative">
                <Editor
                  height={height}
                  defaultLanguage="xml"
                  value={content}
                  onChange={handleContentChange}
                  theme="vs-dark"
                  options={{
                    minimap: { enabled: false },
                    fontSize: 12,
                    formatOnPaste: true,
                    formatOnType: true,
                    automaticLayout: true
                  }}
                />
                
                {validationResult && (
                  <div className="absolute bottom-4 right-4 flex gap-2">
                    {getIssueCounts().errors > 0 && (
                      <Badge variant="destructive">
                        {getIssueCounts().errors} error(s)
                      </Badge>
                    )}
                    {getIssueCounts().warnings > 0 && (
                      <Badge variant="secondary" className="bg-yellow-100 text-yellow-800">
                        {getIssueCounts().warnings} warning(s)
                      </Badge>
                    )}
                    {getIssueCounts().infos > 0 && (
                      <Badge variant="secondary">
                        {getIssueCounts().infos} info(s)
                      </Badge>
                    )}
                  </div>
                )}
              </div>
            </TabsContent>

            <TabsContent value="issues" className="m-0 p-4">
              {!validationResult ? (
                <div className="text-center py-8 text-muted-foreground">
                  <AlertCircle className="h-12 w-12 mx-auto mb-2 opacity-50" />
                  <p>No validation results yet</p>
                  <p className="text-sm">Enter WSDL content to see validation issues</p>
                </div>
              ) : validationResult.issues.length === 0 ? (
                <Alert className="border-green-200 bg-green-50">
                  <CheckCircle2 className="h-4 w-4 text-green-600" />
                  <AlertTitle className="text-green-800">No issues found</AlertTitle>
                  <AlertDescription className="text-green-700">
                    Your WSDL is valid and follows best practices.
                  </AlertDescription>
                </Alert>
              ) : (
                <ScrollArea className="h-[350px]">
                  <div className="space-y-2">
                    {validationResult.issues.map((issue, index) => (
                      <Alert
                        key={index}
                        className={cn(
                          "py-2",
                          issue.type === 'ERROR' && "border-red-200 bg-red-50",
                          issue.type === 'WARNING' && "border-yellow-200 bg-yellow-50",
                          issue.type === 'INFO' && "border-blue-200 bg-blue-50"
                        )}
                      >
                        <div className="flex items-start gap-2">
                          {getIssueIcon(issue.type)}
                          <div className="flex-1">
                            <p className="text-sm font-medium">{issue.message}</p>
                            {(issue.line || issue.column) && (
                              <p className="text-xs text-muted-foreground mt-1">
                                Line {issue.line || '?'}, Column {issue.column || '?'}
                              </p>
                            )}
                          </div>
                        </div>
                      </Alert>
                    ))}
                  </div>
                </ScrollArea>
              )}
            </TabsContent>

            <TabsContent value="metadata" className="m-0 p-4">
              {validationResult?.metadata && (
                <ScrollArea className="h-[350px]">
                  <div className="space-y-4">
                    {/* Target Namespace */}
                    {validationResult.metadata.targetNamespace && (
                      <div>
                        <h4 className="font-medium mb-2">Target Namespace</h4>
                        <div className="flex items-center gap-2">
                          <code className="text-sm bg-muted px-2 py-1 rounded flex-1">
                            {validationResult.metadata.targetNamespace}
                          </code>
                          <Button
                            size="icon"
                            variant="ghost"
                            onClick={() => copyToClipboard(
                              validationResult.metadata!.targetNamespace!,
                              'Target namespace'
                            )}
                          >
                            <Copy className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    )}

                    {/* Services */}
                    {validationResult.metadata.services && validationResult.metadata.services.length > 0 && (
                      <Collapsible
                        open={expandedSections.services}
                        onOpenChange={() => toggleSection('services')}
                      >
                        <CollapsibleTrigger className="flex items-center gap-2 font-medium cursor-pointer hover:text-primary">
                          {expandedSections.services ? (
                            <ChevronDown className="h-4 w-4" />
                          ) : (
                            <ChevronRight className="h-4 w-4" />
                          )}
                          <Server className="h-4 w-4" />
                          Services ({validationResult.metadata.services.length})
                        </CollapsibleTrigger>
                        <CollapsibleContent className="mt-2">
                          <div className="space-y-2 pl-6">
                            {validationResult.metadata.services.map((service, index) => (
                              <Card key={index} className="p-3">
                                <h5 className="font-medium text-sm mb-2">{service.name}</h5>
                                {service.ports.map((port, portIndex) => (
                                  <div key={portIndex} className="text-sm space-y-1 ml-4">
                                    <div className="flex items-center gap-2">
                                      <span className="text-muted-foreground">Port:</span>
                                      <code className="text-xs">{port.name}</code>
                                    </div>
                                    {port.address && (
                                      <div className="flex items-center gap-2">
                                        <span className="text-muted-foreground">Address:</span>
                                        <code className="text-xs">{port.address}</code>
                                      </div>
                                    )}
                                  </div>
                                ))}
                              </Card>
                            ))}
                          </div>
                        </CollapsibleContent>
                      </Collapsible>
                    )}

                    {/* Port Types & Operations */}
                    {validationResult.metadata.portTypes && validationResult.metadata.portTypes.length > 0 && (
                      <Collapsible
                        open={expandedSections.portTypes}
                        onOpenChange={() => toggleSection('portTypes')}
                      >
                        <CollapsibleTrigger className="flex items-center gap-2 font-medium cursor-pointer hover:text-primary">
                          {expandedSections.portTypes ? (
                            <ChevronDown className="h-4 w-4" />
                          ) : (
                            <ChevronRight className="h-4 w-4" />
                          )}
                          <FileCode className="h-4 w-4" />
                          Port Types ({validationResult.metadata.portTypes.length})
                        </CollapsibleTrigger>
                        <CollapsibleContent className="mt-2">
                          <div className="space-y-2 pl-6">
                            {validationResult.metadata.portTypes.map((portType, index) => (
                              <Card key={index} className="p-3">
                                <h5 className="font-medium text-sm mb-2">{portType.name}</h5>
                                <div className="space-y-1 ml-4">
                                  {portType.operations.map((op, opIndex) => (
                                    <div key={opIndex} className="text-sm">
                                      <Badge variant="outline" className="text-xs">
                                        {op.name}
                                      </Badge>
                                    </div>
                                  ))}
                                </div>
                              </Card>
                            ))}
                          </div>
                        </CollapsibleContent>
                      </Collapsible>
                    )}

                    {/* Messages */}
                    {validationResult.metadata.messages && validationResult.metadata.messages.length > 0 && (
                      <Collapsible
                        open={expandedSections.messages}
                        onOpenChange={() => toggleSection('messages')}
                      >
                        <CollapsibleTrigger className="flex items-center gap-2 font-medium cursor-pointer hover:text-primary">
                          {expandedSections.messages ? (
                            <ChevronDown className="h-4 w-4" />
                          ) : (
                            <ChevronRight className="h-4 w-4" />
                          )}
                          <MessageSquare className="h-4 w-4" />
                          Messages ({validationResult.metadata.messages.length})
                        </CollapsibleTrigger>
                        <CollapsibleContent className="mt-2">
                          <div className="space-y-2 pl-6">
                            {validationResult.metadata.messages.map((message, index) => (
                              <Card key={index} className="p-3">
                                <h5 className="font-medium text-sm">{message.name}</h5>
                                {message.parts.length > 0 && (
                                  <div className="text-xs text-muted-foreground mt-1">
                                    {message.parts.length} part(s)
                                  </div>
                                )}
                              </Card>
                            ))}
                          </div>
                        </CollapsibleContent>
                      </Collapsible>
                    )}
                  </div>
                </ScrollArea>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
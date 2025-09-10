import React, { useState, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Progress } from '@/components/ui/progress';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import {
  AlertCircle,
  CheckCircle2,
  XCircle,
  ChevronDown,
  ChevronRight,
  Wifi,
  WifiOff,
  Clock,
  Activity,
  Loader2,
  RefreshCw,
  Shield,
  Zap,
  AlertTriangle,
  Play
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import type { AdapterType } from '@/types/communicationAdapter';
import { ConnectionTestService } from '@/services/connectionTestService';

interface ConnectionTesterProps {
  adapterType: AdapterType;
  adapterName: string;
  configuration: Record<string, any>;
  onTestComplete?: (result: ConnectionTestResult) => void;
  className?: string;
  autoTest?: boolean;
}

interface ConnectionDiagnostic {
  step: string;
  status: 'SUCCESS' | 'FAILED' | 'WARNING' | 'SKIPPED';
  message: string;
  duration: number;
  details?: Record<string, any>;
  errorCode?: string;
}

interface ConnectionTestResult {
  success: boolean;
  message: string;
  diagnostics: ConnectionDiagnostic[];
  duration: number;
  timestamp: string;
  metadata?: Record<string, any>;
  healthScore?: number;
  recommendations?: string[];
}

export function ConnectionTester({
  adapterType,
  adapterName,
  configuration,
  onTestComplete,
  className,
  autoTest = false
}: ConnectionTesterProps) {
  const { toast } = useToast();
  const [isTesting, setIsTesting] = useState(false);
  const [testResult, setTestResult] = useState<ConnectionTestResult | null>(null);
  const [expandedSteps, setExpandedSteps] = useState<Record<number, boolean>>({});

  const testConnection = useCallback(async () => {
    setIsTesting(true);
    setTestResult(null);
    
    try {
      const response = await ConnectionTestService.testConnection({
        adapterType,
        adapterName,
        configuration,
        timeout: 30000,
        performExtendedTests: false,
        includeMetadata: true
      });
      
      setTestResult(response);
      onTestComplete?.(response);
      
      if (!response.success) {
        toast({
          title: 'Connection Failed',
          description: response.message || 'Unable to establish connection. Check diagnostics for details.',
          variant: 'destructive'
        });
      } else {
        toast({
          title: 'Connection Successful',
          description: `Connected to ${adapterName} successfully.`
        });
      }
    } catch (error) {
      const errorResult: ConnectionTestResult = {
        success: false,
        message: error instanceof Error ? error.message : 'Unknown error',
        diagnostics: [{
          step: 'Connection Test',
          status: 'FAILED',
          message: 'Failed to perform connection test',
          duration: 0
        }],
        duration: 0,
        timestamp: new Date().toISOString()
      };
      
      setTestResult(errorResult);
      onTestComplete?.(errorResult);
      
      toast({
        title: 'Test Error',
        description: 'Failed to perform connection test',
        variant: 'destructive'
      });
    } finally {
      setIsTesting(false);
    }
  }, [adapterType, adapterName, configuration, onTestComplete, toast]);

  const toggleStep = useCallback((index: number) => {
    setExpandedSteps(prev => ({
      ...prev,
      [index]: !prev[index]
    }));
  }, []);

  const getStatusIcon = (status: ConnectionDiagnostic['status']) => {
    switch (status) {
      case 'SUCCESS':
        return <CheckCircle2 className="h-4 w-4 text-green-600" />;
      case 'FAILED':
        return <XCircle className="h-4 w-4 text-red-600" />;
      case 'WARNING':
        return <AlertTriangle className="h-4 w-4 text-yellow-600" />;
      case 'SKIPPED':
        return <AlertCircle className="h-4 w-4 text-gray-400" />;
    }
  };

  const getStatusColor = (status: ConnectionDiagnostic['status']) => {
    switch (status) {
      case 'SUCCESS':
        return 'text-green-600';
      case 'FAILED':
        return 'text-red-600';
      case 'WARNING':
        return 'text-yellow-600';
      case 'SKIPPED':
        return 'text-gray-400';
    }
  };

  const getHealthScoreColor = (score: number) => {
    if (score >= 90) return 'text-green-600';
    if (score >= 70) return 'text-yellow-600';
    if (score >= 50) return 'text-orange-600';
    return 'text-red-600';
  };

  React.useEffect(() => {
    if (autoTest) {
      testConnection();
    }
  }, [autoTest, testConnection]);

  return (
    <div className={cn("space-y-4", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <Wifi className="h-5 w-5" />
                Connection Test
              </CardTitle>
              <CardDescription>
                Test connection to {adapterName} ({adapterType})
              </CardDescription>
            </div>
            <Button
              onClick={testConnection}
              disabled={isTesting}
              size="sm"
            >
              {isTesting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Testing...
                </>
              ) : (
                <>
                  <Play className="mr-2 h-4 w-4" />
                  Test Connection
                </>
              )}
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isTesting && !testResult && (
            <div className="space-y-4 py-8">
              <div className="text-center">
                <Activity className="h-8 w-8 mx-auto mb-4 text-primary animate-pulse" />
                <p className="text-sm text-muted-foreground">Testing connection...</p>
                <Progress className="mt-4" />
              </div>
            </div>
          )}

          {testResult && (
            <div className="space-y-4">
              {/* Summary */}
              <Alert className={testResult.success ? "border-green-200" : "border-red-200"}>
                <div className="flex items-start gap-2">
                  {testResult.success ? (
                    <CheckCircle2 className="h-5 w-5 text-green-600 mt-0.5" />
                  ) : (
                    <WifiOff className="h-5 w-5 text-red-600 mt-0.5" />
                  )}
                  <div className="flex-1">
                    <AlertTitle>
                      {testResult.success ? 'Connection Successful' : 'Connection Failed'}
                    </AlertTitle>
                    <AlertDescription className="mt-1">
                      {testResult.message}
                    </AlertDescription>
                    <div className="flex items-center gap-4 mt-2 text-sm">
                      <div className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        <span>{testResult.duration}ms</span>
                      </div>
                      {testResult.healthScore !== undefined && (
                        <div className="flex items-center gap-1">
                          <Shield className="h-3 w-3" />
                          <span className={getHealthScoreColor(testResult.healthScore)}>
                            Health: {testResult.healthScore}%
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </Alert>

              {/* Diagnostics */}
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-base flex items-center gap-2">
                    <Activity className="h-4 w-4" />
                    Diagnostics
                  </CardTitle>
                </CardHeader>
                <CardContent className="pt-0">
                  <ScrollArea className="h-[300px]">
                    <div className="space-y-2">
                      {testResult.diagnostics.map((diagnostic, index) => (
                        <Collapsible
                          key={index}
                          open={expandedSteps[index]}
                          onOpenChange={() => toggleStep(index)}
                        >
                          <div
                            className={cn(
                              "border rounded-lg p-3",
                              diagnostic.status === 'FAILED' && "border-red-200 bg-red-50",
                              diagnostic.status === 'WARNING' && "border-yellow-200 bg-yellow-50",
                              diagnostic.status === 'SUCCESS' && "border-green-200 bg-green-50",
                              diagnostic.status === 'SKIPPED' && "border-gray-200 bg-gray-50"
                            )}
                          >
                            <CollapsibleTrigger className="flex items-start gap-2 w-full text-left">
                              <div className="mt-0.5">
                                {expandedSteps[index] ? (
                                  <ChevronDown className="h-4 w-4" />
                                ) : (
                                  <ChevronRight className="h-4 w-4" />
                                )}
                              </div>
                              {getStatusIcon(diagnostic.status)}
                              <div className="flex-1">
                                <div className="flex items-center justify-between">
                                  <span className="font-medium">{diagnostic.step}</span>
                                  <span className="text-xs text-muted-foreground">
                                    {diagnostic.duration}ms
                                  </span>
                                </div>
                                <p className={cn("text-sm mt-1", getStatusColor(diagnostic.status))}>
                                  {diagnostic.message}
                                </p>
                              </div>
                            </CollapsibleTrigger>
                            {diagnostic.details && (
                              <CollapsibleContent className="mt-2 pl-10">
                                <div className="bg-white dark:bg-gray-900 p-2 rounded text-xs font-mono">
                                  {Object.entries(diagnostic.details).map(([key, value]) => (
                                    <div key={key}>
                                      <span className="text-muted-foreground">{key}:</span>{' '}
                                      <span>{JSON.stringify(value)}</span>
                                    </div>
                                  ))}
                                </div>
                              </CollapsibleContent>
                            )}
                          </div>
                        </Collapsible>
                      ))}
                    </div>
                  </ScrollArea>
                </CardContent>
              </Card>

              {/* Recommendations */}
              {testResult.recommendations && testResult.recommendations.length > 0 && (
                <Card>
                  <CardHeader className="pb-3">
                    <CardTitle className="text-base flex items-center gap-2">
                      <Zap className="h-4 w-4" />
                      Recommendations
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="space-y-2">
                      {testResult.recommendations.map((recommendation, index) => (
                        <Alert key={index} className="py-2">
                          <Zap className="h-4 w-4" />
                          <AlertDescription>{recommendation}</AlertDescription>
                        </Alert>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}


import React, { useState, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  Play,
  CheckCircle2,
  XCircle,
  AlertCircle,
  FileJson,
  Code,
  History,
  Copy,
  Download,
  Upload,
  Trash2,
  Save,
  Clock
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import Editor from '@monaco-editor/react';
import { ConditionTestService } from '@/services/conditionTestService';

interface TestResult {
  id: string;
  timestamp: Date;
  payload: any;
  condition: string;
  conditionType: string;
  result: boolean;
  executionTime: number;
  error?: string;
  details?: {
    evaluatedExpression?: string;
    variables?: Record<string, any>;
    steps?: Array<{
      description: string;
      result: any;
    }>;
  };
}

interface SavedTestCase {
  id: string;
  name: string;
  description?: string;
  payload: any;
  expectedResult: boolean;
  tags?: string[];
}

interface ConditionTesterProps {
  condition: string;
  conditionType: 'ALWAYS' | 'EXPRESSION' | 'HEADER_MATCH' | 'CONTENT_TYPE' | 'XPATH' | 'JSONPATH' | 'REGEX' | 'CUSTOM';
  onClose?: () => void;
  className?: string;
}

export function ConditionTester({
  condition,
  conditionType,
  onClose,
  className
}: ConditionTesterProps) {
  const { toast } = useToast();
  const [testPayload, setTestPayload] = useState('{\n  "type": "ORDER",\n  "amount": 1500,\n  "customer": {\n    "id": "12345",\n    "name": "John Doe"\n  },\n  "items": [\n    {\n      "sku": "ABC123",\n      "quantity": 2\n    }\n  ]\n}');
  const [testResults, setTestResults] = useState<TestResult[]>([]);
  const [savedTestCases, setSavedTestCases] = useState<SavedTestCase[]>([]);
  const [isRunning, setIsRunning] = useState(false);
  const [selectedResult, setSelectedResult] = useState<TestResult | null>(null);
  const [testCaseName, setTestCaseName] = useState('');
  const [editorError, setEditorError] = useState<string | null>(null);

  // Validate JSON as user types
  const handlePayloadChange = useCallback((value: string | undefined) => {
    if (!value) return;
    setTestPayload(value);
    
    try {
      JSON.parse(value);
      setEditorError(null);
    } catch (e) {
      setEditorError('Invalid JSON format');
    }
  }, []);

  // Run condition test
  const runTest = useCallback(async () => {
    if (!condition || conditionType === 'ALWAYS') {
      toast({
        title: 'Info',
        description: 'No condition to test (ALWAYS type)',
      });
      return;
    }

    try {
      const payload = JSON.parse(testPayload);
      setIsRunning(true);
      
      // Call the backend API to test the condition
      const response = await ConditionTestService.testCondition({
        condition,
        conditionType,
        payload,
        headers: payload.headers || {}
      });
      
      const testResult: TestResult = {
        id: response.id,
        timestamp: new Date(response.timestamp),
        payload,
        condition: response.condition,
        conditionType: response.conditionType,
        result: response.result,
        executionTime: response.executionTimeMs,
        error: response.error,
        details: response.details
      };
      
      setTestResults(prev => [testResult, ...prev]);
      setSelectedResult(testResult);
      
    } catch (error) {
      toast({
        title: 'Test Error',
        description: error instanceof Error ? error.message : 'Failed to run test',
        variant: 'destructive'
      });
    } finally {
      setIsRunning(false);
    }
  }, [condition, conditionType, testPayload, toast]);


  // Save test case
  const saveTestCase = useCallback(() => {
    if (!testCaseName.trim()) {
      toast({
        title: 'Error',
        description: 'Please enter a test case name',
        variant: 'destructive'
      });
      return;
    }

    try {
      const payload = JSON.parse(testPayload);
      const lastResult = testResults.find(r => 
        JSON.stringify(r.payload) === JSON.stringify(payload)
      );
      
      const testCase: SavedTestCase = {
        id: Date.now().toString(),
        name: testCaseName,
        payload,
        expectedResult: lastResult?.result || false,
        tags: [conditionType]
      };
      
      setSavedTestCases(prev => [...prev, testCase]);
      setTestCaseName('');
      
      toast({
        title: 'Success',
        description: 'Test case saved',
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Invalid payload',
        variant: 'destructive'
      });
    }
  }, [testCaseName, testPayload, testResults, conditionType, toast]);

  // Load test case
  const loadTestCase = useCallback((testCase: SavedTestCase) => {
    setTestPayload(JSON.stringify(testCase.payload, null, 2));
    toast({
      title: 'Test Case Loaded',
      description: `Loaded: ${testCase.name}`,
    });
  }, [toast]);

  // Export test results
  const exportResults = useCallback(() => {
    const data = {
      condition,
      conditionType,
      testResults,
      savedTestCases,
      exportedAt: new Date().toISOString()
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], {
      type: 'application/json'
    });
    
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `condition-tests-${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    toast({
      title: 'Exported',
      description: 'Test results exported successfully',
    });
  }, [condition, conditionType, testResults, savedTestCases, toast]);

  return (
    <div className={cn("space-y-4", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Condition Tester</CardTitle>
              <CardDescription>
                Test your routing conditions with sample payloads
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline">{conditionType}</Badge>
              {onClose && (
                <Button variant="ghost" size="icon" onClick={onClose}>
                  <XCircle className="h-4 w-4" />
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {/* Current Condition */}
            <div className="p-3 bg-muted rounded-md font-mono text-sm">
              {condition || 'No condition defined'}
            </div>

            <Separator />

            {/* Test Interface */}
            <Tabs defaultValue="payload" className="w-full">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="payload">Test Payload</TabsTrigger>
                <TabsTrigger value="results">Results</TabsTrigger>
                <TabsTrigger value="saved">Saved Tests</TabsTrigger>
              </TabsList>

              <TabsContent value="payload" className="space-y-4 mt-4">
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <label className="text-sm font-medium">
                      Test Payload (JSON)
                    </label>
                    {editorError && (
                      <span className="text-xs text-destructive">
                        {editorError}
                      </span>
                    )}
                  </div>
                  
                  <div className="border rounded-md overflow-hidden">
                    <Editor
                      height="300px"
                      defaultLanguage="json"
                      value={testPayload}
                      onChange={handlePayloadChange}
                      theme="vs-dark"
                      options={{
                        minimap: { enabled: false },
                        fontSize: 12,
                        formatOnPaste: true,
                        formatOnType: true
                      }}
                    />
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <Button
                    onClick={runTest}
                    disabled={isRunning || !!editorError || conditionType === 'ALWAYS'}
                    className="gap-2"
                  >
                    {isRunning ? (
                      <>
                        <Clock className="h-4 w-4 animate-spin" />
                        Running...
                      </>
                    ) : (
                      <>
                        <Play className="h-4 w-4" />
                        Run Test
                      </>
                    )}
                  </Button>
                  
                  <div className="flex-1" />
                  
                  <input
                    type="text"
                    placeholder="Test case name..."
                    value={testCaseName}
                    onChange={(e) => setTestCaseName(e.target.value)}
                    className="px-3 py-2 text-sm border rounded-md"
                  />
                  
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={saveTestCase}
                    disabled={!testCaseName.trim() || !!editorError}
                    className="gap-2"
                  >
                    <Save className="h-4 w-4" />
                    Save Test
                  </Button>
                </div>
              </TabsContent>

              <TabsContent value="results" className="space-y-4 mt-4">
                {testResults.length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">
                    <AlertCircle className="h-12 w-12 mx-auto mb-2 opacity-50" />
                    <p>No test results yet</p>
                    <p className="text-sm">Run a test to see results here</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <p className="text-sm text-muted-foreground">
                        {testResults.length} test(s) run
                      </p>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={exportResults}
                        className="gap-2"
                      >
                        <Download className="h-4 w-4" />
                        Export
                      </Button>
                    </div>
                    
                    <ScrollArea className="h-[300px] pr-4">
                      <div className="space-y-2">
                        {testResults.map((result) => (
                          <Card
                            key={result.id}
                            className={cn(
                              "cursor-pointer transition-colors",
                              selectedResult?.id === result.id && "border-primary"
                            )}
                            onClick={() => setSelectedResult(result)}
                          >
                            <CardContent className="p-3">
                              <div className="flex items-center justify-between">
                                <div className="flex items-center gap-2">
                                  {result.result ? (
                                    <CheckCircle2 className="h-4 w-4 text-green-500" />
                                  ) : result.error ? (
                                    <XCircle className="h-4 w-4 text-red-500" />
                                  ) : (
                                    <XCircle className="h-4 w-4 text-yellow-500" />
                                  )}
                                  <span className="text-sm font-medium">
                                    {result.result ? 'Matched' : 'No Match'}
                                  </span>
                                </div>
                                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                                  <Clock className="h-3 w-3" />
                                  {result.executionTime.toFixed(2)}ms
                                  <span>•</span>
                                  {result.timestamp.toLocaleTimeString()}
                                </div>
                              </div>
                              
                              {result.error && (
                                <p className="text-xs text-destructive mt-1">
                                  {result.error}
                                </p>
                              )}
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    </ScrollArea>
                    
                    {selectedResult && selectedResult.details && (
                      <Card>
                        <CardHeader>
                          <CardTitle className="text-sm">Test Details</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-3">
                          {selectedResult.details.steps && (
                            <div>
                              <p className="text-sm font-medium mb-2">Execution Steps:</p>
                              <div className="space-y-1">
                                {selectedResult.details.steps.map((step, i) => (
                                  <div key={i} className="flex items-start gap-2 text-sm">
                                    <span className="text-muted-foreground">{i + 1}.</span>
                                    <div className="flex-1">
                                      <p>{step.description}</p>
                                      <p className="text-xs text-muted-foreground font-mono">
                                        {JSON.stringify(step.result)}
                                      </p>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}
                          
                          {selectedResult.details.variables && (
                            <div>
                              <p className="text-sm font-medium mb-2">Variables:</p>
                              <pre className="text-xs bg-muted p-2 rounded-md overflow-x-auto">
                                {JSON.stringify(selectedResult.details.variables, null, 2)}
                              </pre>
                            </div>
                          )}
                        </CardContent>
                      </Card>
                    )}
                  </div>
                )}
              </TabsContent>

              <TabsContent value="saved" className="space-y-4 mt-4">
                {savedTestCases.length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">
                    <Save className="h-12 w-12 mx-auto mb-2 opacity-50" />
                    <p>No saved test cases</p>
                    <p className="text-sm">Save test cases for quick reuse</p>
                  </div>
                ) : (
                  <ScrollArea className="h-[400px]">
                    <div className="space-y-2 pr-4">
                      {savedTestCases.map((testCase) => (
                        <Card key={testCase.id}>
                          <CardContent className="p-3">
                            <div className="flex items-center justify-between">
                              <div className="flex-1">
                                <p className="font-medium text-sm">{testCase.name}</p>
                                {testCase.description && (
                                  <p className="text-xs text-muted-foreground">
                                    {testCase.description}
                                  </p>
                                )}
                                <div className="flex items-center gap-2 mt-1">
                                  {testCase.tags?.map(tag => (
                                    <Badge key={tag} variant="secondary" className="text-xs">
                                      {tag}
                                    </Badge>
                                  ))}
                                  <Badge
                                    variant={testCase.expectedResult ? "success" : "destructive"}
                                    className="text-xs"
                                  >
                                    Expects: {testCase.expectedResult ? 'Match' : 'No Match'}
                                  </Badge>
                                </div>
                              </div>
                              <div className="flex items-center gap-1">
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  onClick={() => loadTestCase(testCase)}
                                  title="Load test case"
                                >
                                  <Upload className="h-4 w-4" />
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  onClick={() => {
                                    setSavedTestCases(prev => 
                                      prev.filter(tc => tc.id !== testCase.id)
                                    );
                                  }}
                                  title="Delete test case"
                                  className="text-destructive"
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  </ScrollArea>
                )}
              </TabsContent>
            </Tabs>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
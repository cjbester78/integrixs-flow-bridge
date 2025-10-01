import React, { useState, useCallback, useEffect, useRef } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import {
  AlertCircle,
  CheckCircle2,
  Copy,
  Download,
  RefreshCw,
  Loader2,
  XCircle,
  Sparkles,
  FileJson
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import Editor, { Monaco } from '@monaco-editor/react';
import { editor } from 'monaco-editor';
import { useDebounce } from 'use-debounce';

interface JsonSchemaEditorProps {
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

interface JsonSchemaMetadata {
  schema?: string;
  title?: string;
  description?: string;
  type?: string;
  properties?: Record<string, any>;
  required?: string[];
}

// Common JSON Schema templates
const JSON_SCHEMA_TEMPLATES = {
  basic: {
    name: 'Basic Object',
    schema: {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "title": "Example Schema",
      "description": "A basic JSON schema example",
      "properties": {
        "name": {
          "type": "string",
          "description": "The name of the item"
        },
        "age": {
          "type": "integer",
          "minimum": 0,
          "description": "Age in years"
        }
      },
      "required": ["name"]
    }
  },
  api_response: {
    name: 'API Response',
    schema: {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "title": "API Response Schema",
      "properties": {
        "success": {
          "type": "boolean"
        },
        "data": {
          "type": "object",
          "properties": {
            "id": { "type": "string" },
            "timestamp": { "type": "string", "format": "date-time" }
          }
        },
        "error": {
          "type": ["object", "null"],
          "properties": {
            "code": { "type": "string" },
            "message": { "type": "string" }
          }
        }
      },
      "required": ["success"]
    }
  },
  array_items: {
    name: 'Array of Items',
    schema: {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "array",
      "title": "Items List",
      "items": {
        "type": "object",
        "properties": {
          "id": { "type": "string" },
          "name": { "type": "string" },
          "quantity": { "type": "integer", "minimum": 1 }
        },
        "required": ["id", "name"]
      }
    }
  }
};

export function JsonSchemaEditor({
  initialContent = '',
  onChange,
  onValidationComplete,
  autoValidate = true,
  className,
  height = '400px'
}: JsonSchemaEditorProps) {
  const { toast } = useToast();
  const [content, setContent] = useState(initialContent);
  const [debouncedContent] = useDebounce(content, 500);
  const [isValidating, setIsValidating] = useState(false);
  const [validationResult, setValidationResult] = useState<{
    valid: boolean;
    message?: string;
    issues: ValidationIssue[];
    metadata?: JsonSchemaMetadata;
  } | null>(null);
  const [selectedTemplate, setSelectedTemplate] = useState<string>('');
  const monacoRef = useRef<Monaco | null>(null);
  const editorRef = useRef<editor.IStandaloneCodeEditor | null>(null);

  // Configure Monaco editor for JSON Schema
  const handleEditorWillMount = (monaco: Monaco) => {
    monacoRef.current = monaco;
    
    // Configure JSON language features
    monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
      validate: true,
      schemas: [{
        uri: "http://json-schema.org/draft-07/schema#",
        fileMatch: ["*"],
        schema: {
          "$ref": "http://json-schema.org/draft-07/schema#"
        }
      }]
    });
  };

  const handleEditorDidMount = (editor: editor.IStandaloneCodeEditor, monaco: Monaco) => {
    editorRef.current = editor;

    // Configure autocomplete for JSON Schema keywords
    monaco.languages.registerCompletionItemProvider('json', {
      provideCompletionItems: (model, position) => {
        const textUntilPosition = model.getValueInRange({
          startLineNumber: 1,
          startColumn: 1,
          endLineNumber: position.lineNumber,
          endColumn: position.column
        });

        // Check if we're in a context where schema keywords would be appropriate
        const isInPropertyContext = /"[^"]*"\s*:\s*{\s*$/.test(textUntilPosition);
        
        if (!isInPropertyContext) {
          return { suggestions: [] };
        }

        const suggestions = [
          // Type keywords
          { label: 'type', insertText: '"type": "${1:string}"', detail: 'Defines the data type' },
          { label: 'enum', insertText: '"enum": [${1}]', detail: 'Restricts values to a fixed set' },
          { label: 'const', insertText: '"const": ${1}', detail: 'Requires exact value match' },
          
          // String validations
          { label: 'minLength', insertText: '"minLength": ${1:1}', detail: 'Minimum string length' },
          { label: 'maxLength', insertText: '"maxLength": ${1:100}', detail: 'Maximum string length' },
          { label: 'pattern', insertText: '"pattern": "${1:^[a-zA-Z]+$}"', detail: 'Regular expression pattern' },
          { label: 'format', insertText: '"format": "${1:email}"', detail: 'String format (email, date, etc.)' },
          
          // Number validations
          { label: 'minimum', insertText: '"minimum": ${1:0}', detail: 'Minimum numeric value' },
          { label: 'maximum', insertText: '"maximum": ${1:100}', detail: 'Maximum numeric value' },
          { label: 'exclusiveMinimum', insertText: '"exclusiveMinimum": ${1:0}', detail: 'Exclusive minimum value' },
          { label: 'exclusiveMaximum', insertText: '"exclusiveMaximum": ${1:100}', detail: 'Exclusive maximum value' },
          { label: 'multipleOf', insertText: '"multipleOf": ${1:1}', detail: 'Value must be multiple of' },
          
          // Object validations
          { label: 'properties', insertText: '"properties": {\n\t${1}\n}', detail: 'Object properties schema' },
          { label: 'required', insertText: '"required": [${1:"fieldName"}]', detail: 'Required properties' },
          { label: 'additionalProperties', insertText: '"additionalProperties": ${1:false}', detail: 'Allow additional properties' },
          { label: 'minProperties', insertText: '"minProperties": ${1:1}', detail: 'Minimum number of properties' },
          { label: 'maxProperties', insertText: '"maxProperties": ${1:10}', detail: 'Maximum number of properties' },
          
          // Array validations
          { label: 'items', insertText: '"items": {\n\t${1}\n}', detail: 'Array items schema' },
          { label: 'minItems', insertText: '"minItems": ${1:1}', detail: 'Minimum array length' },
          { label: 'maxItems', insertText: '"maxItems": ${1:10}', detail: 'Maximum array length' },
          { label: 'uniqueItems', insertText: '"uniqueItems": ${1:true}', detail: 'Require unique array items' },
          
          // General
          { label: 'description', insertText: '"description": "${1}"', detail: 'Schema description' },
          { label: 'default', insertText: '"default": ${1}', detail: 'Default value' },
          { label: 'examples', insertText: '"examples": [${1}]', detail: 'Example values' },
          { label: '$ref', insertText: '"$ref": "${1:#/definitions/}"', detail: 'Reference another schema' },
        ];

        return {
          suggestions: suggestions.map((s, index) => ({
            label: s.label,
            kind: monaco.languages.CompletionItemKind.Property,
            insertText: s.insertText,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            detail: s.detail,
            sortText: String(index).padStart(3, '0')
          }))
        };
      }
    });
  };

  // Validate JSON Schema
  const validateJsonSchema = useCallback((schemaContent: string) => {
    if (!schemaContent.trim()) {
      setValidationResult(null);
      return;
    }

    setIsValidating(true);
    const issues: ValidationIssue[] = [];
    let metadata: JsonSchemaMetadata = {};
    let isValid = false;

    try {
      const schema = JSON.parse(schemaContent);
      isValid = true;

      // Extract metadata
      metadata = {
        schema: schema.$schema,
        title: schema.title,
        description: schema.description,
        type: schema.type,
        properties: schema.properties,
        required: schema.required
      };

      // Basic validation checks
      if (!schema.$schema) {
        issues.push({
          type: 'WARNING',
          message: 'Missing $schema declaration. Consider adding "$schema": "http://json-schema.org/draft-07/schema#"'
        });
      }

      if (!schema.type && !schema.$ref && !schema.oneOf && !schema.anyOf && !schema.allOf) {
        issues.push({
          type: 'WARNING',
          message: 'Schema should define a type or use composition keywords (oneOf, anyOf, allOf)'
        });
      }

      if (schema.type === 'object' && !schema.properties) {
        issues.push({
          type: 'INFO',
          message: 'Object type without properties definition'
        });
      }

      if (schema.required && !Array.isArray(schema.required)) {
        issues.push({
          type: 'ERROR',
          message: '"required" must be an array of property names'
        });
        isValid = false;
      }

      // Check for common issues
      if (schema.properties) {
        Object.entries(schema.properties).forEach(([key, value]: [string, any]) => {
          if (!value.type && !value.$ref && !value.oneOf && !value.anyOf && !value.allOf) {
            issues.push({
              type: 'WARNING',
              message: `Property "${key}" is missing a type definition`,
              path: `properties.${key}`
            });
          }
        });
      }

    } catch (error) {
      isValid = false;
      if (error instanceof SyntaxError) {
        const match = error.message.match(/position (\d+)/);
        const position = match ? parseInt(match[1]) : undefined;
        issues.push({
          type: 'ERROR',
          message: `JSON Syntax Error: ${error.message}`,
          column: position
        });
      } else {
        issues.push({
          type: 'ERROR',
          message: error instanceof Error ? error.message : 'Unknown validation error'
        });
      }
    }

    const result = {
      valid: isValid,
      issues,
      metadata,
      message: isValid ? 'Valid JSON Schema' : 'Invalid JSON Schema'
    };

    setValidationResult(result);
    onChange?.(schemaContent, isValid);
    onValidationComplete?.(result);
    setIsValidating(false);
  }, [onChange, onValidationComplete]);

  // Auto-validate on content change
  useEffect(() => {
    if (autoValidate && debouncedContent) {
      validateJsonSchema(debouncedContent);
    }
  }, [debouncedContent, autoValidate, validateJsonSchema]);

  const handleContentChange = useCallback((value: string | undefined) => {
    if (value !== undefined) {
      setContent(value);
    }
  }, []);

  const handleTemplateSelect = useCallback((templateKey: string) => {
    if (templateKey && JSON_SCHEMA_TEMPLATES[templateKey as keyof typeof JSON_SCHEMA_TEMPLATES]) {
      const template = JSON_SCHEMA_TEMPLATES[templateKey as keyof typeof JSON_SCHEMA_TEMPLATES];
      const formattedSchema = JSON.stringify(template.schema, null, 2);
      setContent(formattedSchema);
      setSelectedTemplate(templateKey);
      
      toast({
        title: 'Template Applied',
        description: `Applied ${template.name} template`
      });
    }
  }, [toast]);

  const exportSchema = useCallback(() => {
    const blob = new Blob([content], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'schema.json';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    toast({
      title: 'Schema Exported',
      description: 'JSON Schema has been downloaded'
    });
  }, [content, toast]);

  const copyToClipboard = useCallback(() => {
    navigator.clipboard.writeText(content).then(() => {
      toast({
        title: 'Copied',
        description: 'Schema copied to clipboard'
      });
    });
  }, [content, toast]);

  return (
    <div className={cn("space-y-4", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <FileJson className="h-5 w-5" />
                JSON Schema Editor
              </CardTitle>
              <CardDescription>
                Create and validate JSON schemas with intelligent autocomplete
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Select value={selectedTemplate} onValueChange={handleTemplateSelect}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Select template" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">None</SelectItem>
                  {Object.entries(JSON_SCHEMA_TEMPLATES).map(([key, template]) => (
                    <SelectItem key={key} value={key}>
                      {template.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              
              <Button size="sm" variant="outline" onClick={copyToClipboard}>
                <Copy className="h-4 w-4 mr-2" />
                Copy
              </Button>
              
              <Button size="sm" variant="outline" onClick={exportSchema}>
                <Download className="h-4 w-4 mr-2" />
                Export
              </Button>
              
              {!autoValidate && (
                <Button
                  size="sm"
                  onClick={() => validateJsonSchema(content)}
                  disabled={isValidating}
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
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <Tabs defaultValue="editor" className="w-full">
            <TabsList className="w-full justify-start rounded-none border-b px-4">
              <TabsTrigger value="editor" className="gap-2">
                Editor
                {validationResult && (
                  <Badge
                    variant={validationResult.valid ? "success" : "destructive"}
                  >
                    {validationResult.valid ? 'Valid' : 'Invalid'}
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="preview">
                Preview
              </TabsTrigger>
              <TabsTrigger value="issues">
                Issues
                {validationResult && validationResult.issues.length > 0 && (
                  <Badge variant="outline">
                    {validationResult.issues.length}
                  </Badge>
                )}
              </TabsTrigger>
            </TabsList>

            <TabsContent value="editor" className="m-0">
              <Editor
                height={height}
                defaultLanguage="json"
                value={content}
                onChange={handleContentChange}
                theme="vs-dark"
                beforeMount={handleEditorWillMount}
                onMount={handleEditorDidMount}
                options={{
                  minimap: { enabled: false },
                  fontSize: 13,
                  formatOnPaste: true,
                  formatOnType: true,
                  automaticLayout: true,
                  suggestOnTriggerCharacters: true,
                  quickSuggestions: {
                    other: true,
                    comments: false,
                    strings: true
                  },
                  wordBasedSuggestions: false
                }}
              />
            </TabsContent>

            <TabsContent value="preview" className="m-0 p-4">
              {validationResult?.metadata ? (
                <div className="space-y-4">
                  <div>
                    <h4 className="font-medium mb-2">Schema Information</h4>
                    <div className="space-y-2 text-sm">
                      {validationResult.metadata.title && (
                        <div>
                          <span className="text-muted-foreground">Title:</span>{' '}
                          <span className="font-medium">{validationResult.metadata.title}</span>
                        </div>
                      )}
                      {validationResult.metadata.description && (
                        <div>
                          <span className="text-muted-foreground">Description:</span>{' '}
                          <span>{validationResult.metadata.description}</span>
                        </div>
                      )}
                      {validationResult.metadata.type && (
                        <div>
                          <span className="text-muted-foreground">Type:</span>{' '}
                          <Badge variant="secondary">{validationResult.metadata.type}</Badge>
                        </div>
                      )}
                      {validationResult.metadata.required && (
                        <div>
                          <span className="text-muted-foreground">Required fields:</span>{' '}
                          {validationResult.metadata.required.map((field) => (
                            <Badge key={field} variant="outline" className="ml-1">
                              {field}
                            </Badge>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>

                  {validationResult.metadata.properties && (
                    <div>
                      <h4 className="font-medium mb-2">Properties</h4>
                      <ScrollArea className="h-[300px]">
                        <div className="space-y-2">
                          {Object.entries(validationResult.metadata.properties).map(([key, value]: [string, any]) => (
                            <Card key={key} className="p-3">
                              <div className="flex items-start justify-between">
                                <div className="space-y-1">
                                  <div className="font-medium">{key}</div>
                                  {value.type && (
                                    <Badge variant="secondary" className="text-xs">
                                      {value.type}
                                    </Badge>
                                  )}
                                  {value.description && (
                                    <p className="text-sm text-muted-foreground">{value.description}</p>
                                  )}
                                </div>
                                {validationResult.metadata.required?.includes(key) && (
                                  <Badge variant="destructive" className="ml-2">Required</Badge>
                                )}
                              </div>
                            </Card>
                          ))}
                        </div>
                      </ScrollArea>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-8 text-muted-foreground">
                  <Sparkles className="h-12 w-12 mx-auto mb-2 opacity-50" />
                  <p>Enter a valid JSON Schema to see the preview</p>
                </div>
              )}
            </TabsContent>

            <TabsContent value="issues" className="m-0 p-4">
              {!validationResult ? (
                <div className="text-center py-8 text-muted-foreground">
                  <AlertCircle className="h-12 w-12 mx-auto mb-2 opacity-50" />
                  <p>No validation results yet</p>
                  <p className="text-sm">Enter JSON Schema content to see validation issues</p>
                </div>
              ) : validationResult.issues.length === 0 ? (
                <Alert className="border-green-200 bg-green-50">
                  <CheckCircle2 className="h-4 w-4 text-green-600" />
                  <AlertTitle className="text-green-800">No issues found</AlertTitle>
                  <AlertDescription className="text-green-700">
                    Your JSON Schema is valid and follows best practices.
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
                          {issue.type === 'ERROR' && <XCircle className="h-4 w-4 text-destructive" />}
                          {issue.type === 'WARNING' && <AlertCircle className="h-4 w-4 text-yellow-500" />}
                          {issue.type === 'INFO' && <AlertCircle className="h-4 w-4 text-blue-500" />}
                          <div className="flex-1">
                            <p className="text-sm font-medium">{issue.message}</p>
                            {issue.path && (
                              <p className="text-xs text-muted-foreground mt-1">
                                Path: {issue.path}
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
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
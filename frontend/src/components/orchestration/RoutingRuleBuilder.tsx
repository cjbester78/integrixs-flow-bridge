import React, { useState, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { 
  Plus, 
  Trash2, 
  AlertCircle, 
  CheckCircle2, 
  Code, 
  FileJson,
  Hash,
  Type,
  Calendar,
  ToggleLeft,
  TestTube2
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { ConditionTester } from './ConditionTester';

interface RoutingRule {
  id: string;
  field: string;
  operator: string;
  value: string;
  dataType: string;
  logicalOperator?: 'AND' | 'OR';
}

interface RoutingRuleBuilderProps {
  conditionType: 'ALWAYS' | 'EXPRESSION' | 'HEADER_MATCH' | 'CONTENT_TYPE' | 'XPATH' | 'JSONPATH' | 'REGEX' | 'CUSTOM';
  routingCondition?: string;
  onChange: (condition: string, type: 'ALWAYS' | 'EXPRESSION' | 'HEADER_MATCH' | 'CONTENT_TYPE' | 'XPATH' | 'JSONPATH' | 'REGEX' | 'CUSTOM') => void;
  availableFields?: string[];
  testPayload?: any;
  className?: string;
}

const OPERATORS = {
  string: ['equals', 'not equals', 'contains', 'starts with', 'ends with', 'matches regex'],
  number: ['equals', 'not equals', 'greater than', 'less than', 'greater or equal', 'less or equal'],
  boolean: ['is true', 'is false'],
  date: ['equals', 'before', 'after', 'between']
};

const DATA_TYPES = [
  { value: 'string', label: 'String', icon: Type },
  { value: 'number', label: 'Number', icon: Hash },
  { value: 'boolean', label: 'Boolean', icon: ToggleLeft },
  { value: 'date', label: 'Date', icon: Calendar }
];

export function RoutingRuleBuilder({
  conditionType,
  routingCondition,
  onChange,
  availableFields = [],
  testPayload,
  className
}: RoutingRuleBuilderProps) {
  const [rules, setRules] = useState<RoutingRule[]>([]);
  const [expression, setExpression] = useState(routingCondition || '');
  const [testResult, setTestResult] = useState<{ success: boolean; message: string } | null>(null);
  const [showTester, setShowTester] = useState(false);

  // Initialize rules from existing condition
  React.useEffect(() => {
    if (routingCondition && conditionType === 'EXPRESSION') {
      try {
        // Try to parse structured rules from expression
        const parsed = parseExpressionToRules(routingCondition);
        if (parsed.length > 0) {
          setRules(parsed);
        }
      } catch (e) {
        // If parsing fails, keep raw expression
        console.warn('Could not parse expression to rules:', e);
      }
    }
  }, [routingCondition, conditionType]);

  const parseExpressionToRules = (expr: string): RoutingRule[] => {
    // Simple parser for basic expressions
    // This would need to be enhanced for production use
    const rules: RoutingRule[] = [];
    // Example: payload.type == "ORDER" && payload.amount > 1000
    // For now, return empty array - real implementation would parse the expression
    return rules;
  };

  const addRule = useCallback(() => {
    const newRule: RoutingRule = {
      id: Date.now().toString(),
      field: availableFields[0] || '',
      operator: 'equals',
      value: '',
      dataType: 'string',
      logicalOperator: rules.length > 0 ? 'AND' : undefined
    };
    setRules([...rules, newRule]);
  }, [rules, availableFields]);

  const updateRule = useCallback((id: string, updates: Partial<RoutingRule>) => {
    setRules(rules.map(rule => 
      rule.id === id ? { ...rule, ...updates } : rule
    ));
  }, [rules]);

  const removeRule = useCallback((id: string) => {
    setRules(rules.filter(rule => rule.id !== id));
  }, [rules]);

  const buildExpression = useCallback(() => {
    if (rules.length === 0) return '';
    
    return rules.map((rule, index) => {
      const fieldPath = rule.field;
      const operator = rule.operator;
      const value = rule.dataType === 'string' ? `"${rule.value}"` : rule.value;
      
      let condition = '';
      switch (operator) {
        case 'equals':
          condition = `${fieldPath} == ${value}`;
          break;
        case 'not equals':
          condition = `${fieldPath} != ${value}`;
          break;
        case 'contains':
          condition = `${fieldPath}.contains(${value})`;
          break;
        case 'starts with':
          condition = `${fieldPath}.startsWith(${value})`;
          break;
        case 'ends with':
          condition = `${fieldPath}.endsWith(${value})`;
          break;
        case 'matches regex':
          condition = `${fieldPath}.matches(${value})`;
          break;
        case 'greater than':
          condition = `${fieldPath} > ${value}`;
          break;
        case 'less than':
          condition = `${fieldPath} < ${value}`;
          break;
        case 'greater or equal':
          condition = `${fieldPath} >= ${value}`;
          break;
        case 'less or equal':
          condition = `${fieldPath} <= ${value}`;
          break;
        case 'is true':
          condition = `${fieldPath} == true`;
          break;
        case 'is false':
          condition = `${fieldPath} == false`;
          break;
        default:
          condition = `${fieldPath} ${operator} ${value}`;
      }
      
      if (index > 0 && rule.logicalOperator) {
        return ` ${rule.logicalOperator} ${condition}`;
      }
      return condition;
    }).join('');
  }, [rules]);

  React.useEffect(() => {
    if (conditionType === 'EXPRESSION' && rules.length > 0) {
      const expr = buildExpression();
      setExpression(expr);
      onChange(expr, conditionType);
    }
  }, [rules, conditionType, buildExpression, onChange]);


  if (conditionType === 'ALWAYS') {
    return (
      <Alert className={className}>
        <CheckCircle2 className="h-4 w-4" />
        <AlertDescription>
          This target will always receive messages (no routing condition).
        </AlertDescription>
      </Alert>
    );
  }

  if (conditionType === 'CUSTOM') {
    return (
      <div className={cn("space-y-4", className)}>
        <Alert>
          <Code className="h-4 w-4" />
          <AlertDescription>
            Enter a custom routing expression. This will be evaluated by your custom routing engine.
          </AlertDescription>
        </Alert>
        <Textarea
          value={expression}
          onChange={(e) => {
            setExpression(e.target.value);
            onChange(e.target.value, conditionType);
          }}
          placeholder="Enter custom routing expression..."
          className="font-mono text-sm"
          rows={4}
        />
      </div>
    );
  }

  return (
    <div className={cn("space-y-4", className)}>
      {/* Visual Rule Builder */}
      {conditionType === 'EXPRESSION' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <Label>Visual Rule Builder</Label>
            <Button
              variant="outline"
              size="sm"
              onClick={addRule}
              className="gap-2"
            >
              <Plus className="h-4 w-4" />
              Add Rule
            </Button>
          </div>

          {rules.length === 0 ? (
            <Card>
              <CardContent className="pt-6 text-center text-muted-foreground">
                No rules defined. Click "Add Rule" to start building your condition.
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-2">
              {rules.map((rule, index) => (
                <Card key={rule.id}>
                  <CardContent className="pt-4">
                    <div className="grid gap-4">
                      {index > 0 && (
                        <Select
                          value={rule.logicalOperator}
                          onValueChange={(value) => updateRule(rule.id, { logicalOperator: value as 'AND' | 'OR' })}
                        >
                          <SelectTrigger className="w-24">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="AND">AND</SelectItem>
                            <SelectItem value="OR">OR</SelectItem>
                          </SelectContent>
                        </Select>
                      )}

                      <div className="flex gap-2 items-end">
                        <div className="flex-1">
                          <Label className="text-xs">Field</Label>
                          <Select
                            value={rule.field}
                            onValueChange={(value) => updateRule(rule.id, { field: value })}
                          >
                            <SelectTrigger>
                              <SelectValue placeholder="Select field" />
                            </SelectTrigger>
                            <SelectContent>
                              {availableFields.map(field => (
                                <SelectItem key={field} value={field}>{field}</SelectItem>
                              ))}
                              <SelectItem value="custom">Custom field...</SelectItem>
                            </SelectContent>
                          </Select>
                          {rule.field === 'custom' && (
                            <Input
                              className="mt-2"
                              placeholder="Enter field path (e.g., payload.order.type)"
                              onChange={(e) => updateRule(rule.id, { field: e.target.value })}
                            />
                          )}
                        </div>

                        <div className="w-32">
                          <Label className="text-xs">Type</Label>
                          <Select
                            value={rule.dataType}
                            onValueChange={(value) => updateRule(rule.id, { dataType: value })}
                          >
                            <SelectTrigger>
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              {DATA_TYPES.map(type => (
                                <SelectItem key={type.value} value={type.value}>
                                  <div className="flex items-center gap-2">
                                    <type.icon className="h-3 w-3" />
                                    {type.label}
                                  </div>
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </div>

                        <div className="w-40">
                          <Label className="text-xs">Operator</Label>
                          <Select
                            value={rule.operator}
                            onValueChange={(value) => updateRule(rule.id, { operator: value })}
                          >
                            <SelectTrigger>
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              {OPERATORS[rule.dataType as keyof typeof OPERATORS]?.map(op => (
                                <SelectItem key={op} value={op}>{op}</SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </div>

                        <div className="flex-1">
                          <Label className="text-xs">Value</Label>
                          {rule.dataType === 'boolean' ? (
                            <Select
                              value={rule.value}
                              onValueChange={(value) => updateRule(rule.id, { value })}
                            >
                              <SelectTrigger>
                                <SelectValue />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="true">True</SelectItem>
                                <SelectItem value="false">False</SelectItem>
                              </SelectContent>
                            </Select>
                          ) : (
                            <Input
                              value={rule.value}
                              onChange={(e) => updateRule(rule.id, { value: e.target.value })}
                              placeholder={rule.dataType === 'date' ? 'YYYY-MM-DD' : 'Enter value'}
                              type={rule.dataType === 'number' ? 'number' : 'text'}
                            />
                          )}
                        </div>

                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => removeRule(rule.id)}
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
          )}

          {rules.length > 0 && (
            <div className="space-y-2">
              <Label>Generated Expression</Label>
              <div className="p-3 bg-muted rounded-md font-mono text-sm">
                {buildExpression() || 'No expression generated'}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Raw Expression Editor */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label>
            {conditionType === 'EXPRESSION' ? 'Edit Expression Directly' : 
             conditionType === 'XPATH' ? 'XPath Expression' :
             conditionType === 'JSONPATH' ? 'JSONPath Expression' :
             conditionType === 'REGEX' ? 'Regular Expression' :
             conditionType === 'HEADER_MATCH' ? 'Header Match Expression' :
             conditionType === 'CONTENT_TYPE' ? 'Content Type Pattern' :
             'Expression'}
          </Label>
          {conditionType === 'EXPRESSION' && rules.length > 0 && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setRules([])}
            >
              Clear Visual Rules
            </Button>
          )}
        </div>
        <Textarea
          value={expression}
          onChange={(e) => {
            setExpression(e.target.value);
            onChange(e.target.value, conditionType);
          }}
          placeholder={
            conditionType === 'XPATH' ? '//order[@type="urgent"]' :
            conditionType === 'JSONPATH' ? '$.order[?(@.type=="urgent")]' :
            conditionType === 'REGEX' ? '^ORDER-\\d{6}$' :
            conditionType === 'HEADER_MATCH' ? 'Content-Type: application/json' :
            conditionType === 'CONTENT_TYPE' ? 'application/json' :
            'payload.type == "ORDER" && payload.amount > 1000'
          }
          className="font-mono text-sm"
          rows={3}
        />
      </div>

      {/* Test Button */}
      {conditionType !== 'ALWAYS' && expression && (
        <div className="flex justify-end">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setShowTester(true)}
            className="gap-2"
          >
            <TestTube2 className="h-4 w-4" />
            Test Condition
          </Button>
        </div>
      )}
      
      {/* Condition Tester Modal */}
      {showTester && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50">
          <div className="fixed left-[50%] top-[50%] z-50 w-full max-w-4xl max-h-[90vh] overflow-y-auto translate-x-[-50%] translate-y-[-50%]">
            <ConditionTester
              condition={expression}
              conditionType={conditionType}
              onClose={() => setShowTester(false)}
            />
          </div>
        </div>
      )}
    </div>
  );
}
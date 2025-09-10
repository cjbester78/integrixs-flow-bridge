import React from 'react';
import { ConfigurationFieldSchema } from '@/types/adapter';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Info } from 'lucide-react';
import { ConditionalFields } from './ConditionalFields';
import { FieldGroup } from './FieldGroup';
import { MultiSelect } from '@/components/ui/multi-select';
import { DynamicFieldLoader } from './DynamicFieldLoader';

interface FieldRendererProps {
  field: ConfigurationFieldSchema;
  value: any;
  onChange: (value: any) => void;
  error?: string;
  allValues: Record<string, any>;
}

export const FieldRenderer: React.FC<FieldRendererProps> = ({
  field,
  value,
  onChange,
  error,
  allValues
}) => {
  // Check if field should be shown based on condition
  if (field.condition) {
    const conditionValue = allValues[field.condition.field];
    const operator = field.condition.operator || 'equals';
    
    let shouldShow = false;
    switch (operator) {
      case 'equals':
        shouldShow = conditionValue === field.condition.value;
        break;
      case 'notEquals':
        shouldShow = conditionValue !== field.condition.value;
        break;
      case 'contains':
        shouldShow = String(conditionValue).includes(String(field.condition.value));
        break;
      case 'in':
        shouldShow = Array.isArray(field.condition.value) && field.condition.value.includes(conditionValue);
        break;
      case 'notIn':
        shouldShow = Array.isArray(field.condition.value) && !field.condition.value.includes(conditionValue);
        break;
    }
    
    if (!shouldShow) return null;
  }

  const renderField = () => {
    switch (field.type) {
      case 'text':
      case 'password':
      case 'number':
        return (
          <Input
            type={field.type}
            value={value || field.default || ''}
            onChange={(e) => onChange(field.type === 'number' ? Number(e.target.value) : e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
            min={field.validation?.min}
            max={field.validation?.max}
            minLength={field.validation?.minLength}
            maxLength={field.validation?.maxLength}
            pattern={field.validation?.pattern}
            className={error ? 'border-red-500' : ''}
          />
        );

      case 'select':
        return (
          <Select
            value={value || field.default || ''}
            onValueChange={onChange}
            required={field.required}
          >
            <SelectTrigger className={error ? 'border-red-500' : ''}>
              <SelectValue placeholder={field.placeholder || 'Select an option'} />
            </SelectTrigger>
            <SelectContent>
              {field.options?.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        );

      case 'multiselect':
        return (
          <MultiSelect
            options={field.options || []}
            selected={value || []}
            onChange={onChange}
            placeholder={field.placeholder || 'Select multiple options'}
            className={error ? 'border-red-500' : ''}
            disabled={field.disabled}
          />
        );

      case 'boolean':
        return (
          <div className="flex items-center space-x-2">
            <Switch
              checked={value || field.default || false}
              onCheckedChange={onChange}
              id={field.name}
            />
            <Label htmlFor={field.name}>{field.label}</Label>
          </div>
        );

      case 'textarea':
        return (
          <Textarea
            value={value || field.default || ''}
            onChange={(e) => onChange(e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
            rows={4}
            className={error ? 'border-red-500' : ''}
          />
        );

      case 'json':
        return (
          <Textarea
            value={typeof value === 'string' ? value : JSON.stringify(value || field.default || {}, null, 2)}
            onChange={(e) => {
              try {
                const parsed = JSON.parse(e.target.value);
                onChange(parsed);
              } catch {
                onChange(e.target.value);
              }
            }}
            placeholder={field.placeholder || '{}'}
            required={field.required}
            rows={6}
            className={`font-mono text-sm ${error ? 'border-red-500' : ''}`}
          />
        );

      case 'group':
        return (
          <FieldGroup
            field={field}
            values={value || {}}
            onChange={onChange}
            allValues={allValues}
          />
        );

      case 'conditional':
        return (
          <ConditionalFields
            field={field}
            value={value}
            onChange={onChange}
            allValues={allValues}
          />
        );

      case 'dynamic':
        return (
          <DynamicFieldLoader
            field={field}
            value={value}
            onChange={onChange}
            allValues={allValues}
            error={error}
          />
        );

      case 'file':
        return (
          <div>
            <Input
              type="file"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  onChange(file);
                }
              }}
              accept={field.validation?.pattern}
              required={field.required}
              className={error ? 'border-red-500' : ''}
            />
            {value && typeof value === 'string' && (
              <p className="text-sm text-muted-foreground mt-1">Current: {value}</p>
            )}
          </div>
        );

      default:
        return <div>Unknown field type: {field.type}</div>;
    }
  };

  if (field.type === 'boolean') {
    return renderField();
  }

  return (
    <div className="space-y-2">
      <Label htmlFor={field.name}>
        {field.label}
        {field.required && <span className="text-red-500 ml-1">*</span>}
      </Label>
      
      {renderField()}
      
      {field.help && (
        <Alert className="mt-2">
          <Info className="h-4 w-4" />
          <AlertDescription className="text-sm">{field.help}</AlertDescription>
        </Alert>
      )}
      
      {error && (
        <p className="text-sm text-red-500">{error}</p>
      )}
    </div>
  );
};
import React, { useState, useEffect, useCallback } from 'react';
import { ConfigurationFieldSchema } from '@/types/adapter';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { api } from '@/services/api';

interface DynamicFieldLoaderProps {
  field: ConfigurationFieldSchema;
  value: any;
  onChange: (value: any) => void;
  error?: string;
  allValues: Record<string, any>;
}

interface DynamicOption {
  label: string;
  value: string;
}

export const DynamicFieldLoader: React.FC<DynamicFieldLoaderProps> = ({
  field,
  value,
  onChange,
  error,
  allValues
}) => {
  const [options, setOptions] = useState<DynamicOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  const loadOptions = useCallback(async () => {
    if (!field.dynamicSource) {
      setLoadError('No dynamic source configured');
      return;
    }

    setLoading(true);
    setLoadError(null);

    try {
      // Extract dependencies from the dynamic source
      const dependencies = field.dynamicSource.dependencies || [];
      const params: Record<string, any> = {};

      // Add dependency values to params
      dependencies.forEach((dep: string) => {
        if (allValues[dep] !== undefined) {
          params[dep] = allValues[dep];
        }
      });

      // Make API call
      const response = await api.get(field.dynamicSource.endpoint, { params });
      
      if (response.success && response.data) {
        // Extract options based on the path configuration
        let optionsData = response.data;
        
        if (field.dynamicSource.dataPath) {
          // Navigate to the specified path in the response
          const pathParts = field.dynamicSource.dataPath.split('.');
          for (const part of pathParts) {
            optionsData = optionsData[part];
            if (!optionsData) break;
          }
        }

        // Transform data into options format
        if (Array.isArray(optionsData)) {
          const labelField = field.dynamicSource.labelField || 'label';
          const valueField = field.dynamicSource.valueField || 'value';

          const transformedOptions = optionsData.map((item) => ({
            label: item[labelField] || item.toString(),
            value: item[valueField] || item.toString()
          }));

          setOptions(transformedOptions);
        } else {
          setLoadError('Invalid response format');
        }
      } else {
        setLoadError(response.error || 'Failed to load options');
      }
    } catch (err) {
      setLoadError('Error loading options: ' + (err as Error).message);
    } finally {
      setLoading(false);
    }
  }, [field.dynamicSource, allValues]);

  // Extract complex dependency calculation
  const dependencyValues = field.dynamicSource?.dependencies
    ? field.dynamicSource.dependencies.map((dep: string) => allValues[dep]).join(',')
    : '';

  useEffect(() => {
    // Load options when component mounts or dependencies change
    if (field.dynamicSource?.dependencies) {
      const depsValues = field.dynamicSource.dependencies.map(
        (dep: string) => allValues[dep]
      );
      
      // Only reload if all dependencies have values
      const allDepsHaveValues = depsValues.every(val => val !== undefined && val !== '');
      if (allDepsHaveValues) {
        loadOptions();
      } else {
        setOptions([]);
        onChange(''); // Clear selection when dependencies change
      }
    } else {
      loadOptions();
    }
  }, [dependencyValues, loadOptions, onChange, setOptions, field.dynamicSource?.dependencies, allValues]);

  if (loading) {
    return (
      <div className="space-y-2">
        <Skeleton className="h-4 w-20" />
        <Skeleton className="h-10 w-full" />
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="space-y-2">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{loadError}</AlertDescription>
        </Alert>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={loadOptions}
          className="w-full"
        >
          <RefreshCw className="h-4 w-4 mr-2" />
          Retry
        </Button>
      </div>
    );
  }

  // Render as select field with dynamic options
  return (
    <Select
      value={value || ''}
      onValueChange={onChange}
      required={field.required}
    >
      <SelectTrigger className={error ? 'border-red-500' : ''}>
        <SelectValue placeholder={field.placeholder || 'Select an option'} />
      </SelectTrigger>
      <SelectContent>
        {options.length === 0 ? (
          <div className="px-2 py-1.5 text-sm text-muted-foreground">
            No options available
          </div>
        ) : (
          options.map((option) => (
            <SelectItem key={option.value} value={option.value}>
              {option.label}
            </SelectItem>
          ))
        )}
      </SelectContent>
    </Select>
  );
};
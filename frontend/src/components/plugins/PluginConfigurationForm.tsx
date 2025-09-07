import React from 'react';
import { HelpCircle } from 'lucide-react';
import type { ConfigurationSchema, ConfigurationField } from '../../types/plugin';

interface PluginConfigurationFormProps {
  schema: ConfigurationSchema;
  values: Record<string, any>;
  onChange: (values: Record<string, any>) => void;
}

export const PluginConfigurationForm: React.FC<PluginConfigurationFormProps> = ({
  schema,
  values,
  onChange,
}) => {
  const handleFieldChange = (fieldName: string, value: any) => {
    onChange({
      ...values,
      [fieldName]: value,
    });
  };

  const evaluateCondition = (condition?: {
    field: string;
    operator: 'equals' | 'notEquals' | 'in' | 'notIn';
    value: any;
  }) => {
    if (!condition) return true;
    
    const fieldValue = values[condition.field];
    
    switch (condition.operator) {
      case 'equals':
        return fieldValue === condition.value;
      case 'notEquals':
        return fieldValue !== condition.value;
      case 'in':
        return Array.isArray(condition.value) && condition.value.includes(fieldValue);
      case 'notIn':
        return Array.isArray(condition.value) && !condition.value.includes(fieldValue);
      default:
        return true;
    }
  };

  const renderField = (field: ConfigurationField) => {
    if (!evaluateCondition(field.condition)) {
      return null;
    }

    const value = values[field.name] ?? field.defaultValue ?? '';

    switch (field.type) {
      case 'text':
      case 'email':
      case 'url':
        return (
          <input
            type={field.type}
            value={value}
            onChange={(e) => handleFieldChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required={field.required}
          />
        );

      case 'password':
        return (
          <input
            type="password"
            value={value}
            onChange={(e) => handleFieldChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required={field.required}
          />
        );

      case 'number':
        return (
          <input
            type="number"
            value={value}
            onChange={(e) => handleFieldChange(field.name, e.target.value ? Number(e.target.value) : '')}
            placeholder={field.placeholder}
            min={field.validation?.min}
            max={field.validation?.max}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required={field.required}
          />
        );

      case 'textarea':
        return (
          <textarea
            value={value}
            onChange={(e) => handleFieldChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required={field.required}
          />
        );

      case 'select':
        return (
          <select
            value={value}
            onChange={(e) => handleFieldChange(field.name, e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required={field.required}
          >
            <option value="">Select an option</option>
            {field.options?.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        );

      case 'multiselect':
        return (
          <div className="space-y-2 border border-gray-300 rounded-lg p-3 max-h-48 overflow-y-auto">
            {field.options?.map((option) => (
              <label key={option.value} className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  value={option.value}
                  checked={Array.isArray(value) && value.includes(option.value)}
                  onChange={(e) => {
                    const currentValues = Array.isArray(value) ? value : [];
                    if (e.target.checked) {
                      handleFieldChange(field.name, [...currentValues, option.value]);
                    } else {
                      handleFieldChange(field.name, currentValues.filter(v => v !== option.value));
                    }
                  }}
                  className="rounded text-blue-600 focus:ring-2 focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">{option.label}</span>
              </label>
            ))}
          </div>
        );

      case 'checkbox':
      case 'boolean':
        return (
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={Boolean(value)}
              onChange={(e) => handleFieldChange(field.name, e.target.checked)}
              className="rounded text-blue-600 focus:ring-2 focus:ring-blue-500"
            />
            <span className="text-sm text-gray-700">Enable</span>
          </label>
        );

      case 'radio':
        return (
          <div className="space-y-2">
            {field.options?.map((option) => (
              <label key={option.value} className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name={field.name}
                  value={option.value}
                  checked={value === option.value}
                  onChange={(e) => handleFieldChange(field.name, e.target.value)}
                  className="text-blue-600 focus:ring-2 focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">{option.label}</span>
              </label>
            ))}
          </div>
        );

      case 'json':
        return (
          <textarea
            value={typeof value === 'string' ? value : JSON.stringify(value, null, 2)}
            onChange={(e) => {
              try {
                const parsed = JSON.parse(e.target.value);
                handleFieldChange(field.name, parsed);
              } catch {
                handleFieldChange(field.name, e.target.value);
              }
            }}
            placeholder={field.placeholder || '{"key": "value"}'}
            rows={6}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg font-mono text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required={field.required}
          />
        );

      default:
        return (
          <input
            type="text"
            value={value}
            onChange={(e) => handleFieldChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required={field.required}
          />
        );
    }
  };

  return (
    <div className="space-y-8">
      {schema.sections.map((section) => (
        <div key={section.id}>
          <h3 className="text-lg font-medium text-gray-900 mb-1">{section.title}</h3>
          {section.description && (
            <p className="text-sm text-gray-600 mb-4">{section.description}</p>
          )}
          
          <div className="space-y-4">
            {section.fields.map((field) => {
              const fieldElement = renderField(field);
              if (!fieldElement) return null;
              
              return (
                <div key={field.name}>
                  <label className="block mb-1">
                    <span className="text-sm font-medium text-gray-700">
                      {field.label}
                      {field.required && <span className="text-red-500 ml-1">*</span>}
                    </span>
                    {field.help && (
                      <span className="ml-2 text-gray-400 hover:text-gray-600 inline-flex items-center">
                        <HelpCircle className="w-4 h-4" title={field.help} />
                      </span>
                    )}
                  </label>
                  
                  {fieldElement}
                  
                  {field.help && (
                    <p className="mt-1 text-xs text-gray-500">{field.help}</p>
                  )}
                  
                  {field.validation?.message && (
                    <p className="mt-1 text-xs text-red-600">{field.validation.message}</p>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
};
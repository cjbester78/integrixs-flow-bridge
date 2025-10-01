import React from 'react';
import { ConfigurationFieldSchema } from '@/types/adapter';
import { FieldRenderer } from './FieldRenderer';

interface ConditionalFieldsProps {
  field: ConfigurationFieldSchema;
  value: any;
  onChange: (value: any) => void;
  allValues: Record<string, any>;
}

export const ConditionalFields: React.FC<ConditionalFieldsProps> = ({
  field,
  value,
  onChange,
  allValues
}) => {
  if (field.type !== 'conditional' || !field.options) {
    return null;
  }

  const selectedOption = value?.type || field.default;
  const selectedFields = field.options.find(opt => opt.value === selectedOption)?.fields || [];

  const handleFieldChange = (fieldName: string, fieldValue: any) => {
    onChange({
      ...value,
      type: selectedOption,
      [fieldName]: fieldValue
    });
  };

  return (
    <div className="space-y-4">
      <FieldRenderer
        field={{
          name: `${field.name}_type`,
          type: 'select',
          label: field.label,
          required: field.required,
          options: field.options.map(opt => ({ value: opt.value, label: opt.label || opt.value })),
          default: field.default
        }}
        value={selectedOption}
        onChange={(newType) => onChange({ type: newType })}
        allValues={allValues}
      />

      {selectedFields.length > 0 && (
        <div className="pl-6 space-y-4 border-l-2 border-gray-200">
          {selectedFields.map((subField) => (
            <FieldRenderer
              key={subField.name}
              field={subField}
              value={value?.[subField.name]}
              onChange={(fieldValue) => handleFieldChange(subField.name, fieldValue)}
              allValues={allValues}
            />
          ))}
        </div>
      )}
    </div>
  );
};
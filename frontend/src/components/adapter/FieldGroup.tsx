import React from 'react';
import { ConfigurationFieldSchema } from '@/types/adapter';
import { FieldRenderer } from './FieldRenderer';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

interface FieldGroupProps {
  field: ConfigurationFieldSchema;
  values: Record<string, any>;
  onChange: (values: Record<string, any>) => void;
  allValues: Record<string, any>;
}

export const FieldGroup: React.FC<FieldGroupProps> = ({
  field,
  values,
  onChange,
  allValues
}) => {
  if (field.type !== 'group' || !field.fields) {
    return null;
  }

  const handleFieldChange = (fieldName: string, value: any) => {
    onChange({
      ...values,
      [fieldName]: value
    });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">{field.label}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {field.fields.map((subField) => (
          <FieldRenderer
            key={subField.name}
            field={subField}
            value={values[subField.name]}
            onChange={(value) => handleFieldChange(subField.name, value)}
            allValues={allValues}
          />
        ))}
      </CardContent>
    </Card>
  );
};
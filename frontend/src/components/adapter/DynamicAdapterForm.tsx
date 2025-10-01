import React, { useMemo } from 'react';
import { ConfigurationFieldSchema } from '@/types/adapter';
import { FieldRenderer } from './FieldRenderer';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';

interface DynamicAdapterFormProps {
  schema: {
    common?: { fields: ConfigurationFieldSchema[] };
    inbound?: { fields: ConfigurationFieldSchema[] };
    outbound?: { fields: ConfigurationFieldSchema[] };
    sections?: Array<{
      id: string;
      title: string;
      fields: ConfigurationFieldSchema[];
    }>;
  };
  direction: 'inbound' | 'outbound' | 'bidirectional';
  values: Record<string, any>;
  onChange: (values: Record<string, any>) => void;
  errors?: Record<string, string>;
}

export const DynamicAdapterForm: React.FC<DynamicAdapterFormProps> = ({
  schema,
  direction,
  values,
  onChange,
  errors = {}
}) => {
  // Calculate fields to render early to avoid conditional hook call
  const fieldsToRender = useMemo(() => {
    const fields: ConfigurationFieldSchema[] = [];
    
    if (schema.common?.fields) {
      fields.push(...schema.common.fields);
    }
    
    if (direction === 'inbound' && schema.inbound?.fields) {
      fields.push(...schema.inbound.fields);
    }
    
    if (direction === 'outbound' && schema.outbound?.fields) {
      fields.push(...schema.outbound.fields);
    }
    
    return fields;
  }, [schema, direction]);

  const handleFieldChange = (fieldName: string, value: any) => {
    onChange({
      ...values,
      [fieldName]: value
    });
  };

  const renderFields = (fields: ConfigurationFieldSchema[]) => {
    return fields.map((field) => (
      <FieldRenderer
        key={field.name}
        field={field}
        value={values[field.name]}
        onChange={(value) => handleFieldChange(field.name, value)}
        error={errors[field.name]}
        allValues={values}
      />
    ));
  };

  const renderSection = (title: string, fields: ConfigurationFieldSchema[]) => {
    if (!fields || fields.length === 0) return null;

    return (
      <Card className="mb-4">
        <CardHeader>
          <CardTitle className="text-lg">{title}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {renderFields(fields)}
        </CardContent>
      </Card>
    );
  };

  // If schema has predefined sections, use those
  if (schema.sections) {
    return (
      <div className="space-y-4">
        {schema.sections.map((section) => (
          <Card key={section.id}>
            <CardHeader>
              <CardTitle className="text-lg">{section.title}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {renderFields(section.fields)}
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  // For bidirectional, show tabs
  if (direction === 'bidirectional' && (schema.inbound || schema.outbound)) {
    return (
      <Tabs defaultValue="common" className="w-full">
        <TabsList className="grid w-full grid-cols-3">
          {schema.common && <TabsTrigger value="common">Common</TabsTrigger>}
          {schema.inbound && <TabsTrigger value="inbound">Inbound</TabsTrigger>}
          {schema.outbound && <TabsTrigger value="outbound">Outbound</TabsTrigger>}
        </TabsList>
        
        {schema.common && (
          <TabsContent value="common">
            {renderSection('Common Configuration', schema.common.fields)}
          </TabsContent>
        )}
        
        {schema.inbound && (
          <TabsContent value="inbound">
            {renderSection('Inbound Configuration', schema.inbound.fields)}
          </TabsContent>
        )}
        
        {schema.outbound && (
          <TabsContent value="outbound">
            {renderSection('Outbound Configuration', schema.outbound.fields)}
          </TabsContent>
        )}
      </Tabs>
    );
  }

  // For single direction, just show the relevant fields
  return (
    <div className="space-y-4">
      {renderFields(fieldsToRender)}
    </div>
  );
};
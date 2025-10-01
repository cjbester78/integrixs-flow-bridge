import { useState, useEffect, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { ArrowRight } from 'lucide-react';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';
import { logger, LogCategory } from '@/lib/logger';

interface Adapter {
  id: string;
  name: string;
  icon: React.ComponentType<{ className?: string }>;
  category: string;
}

interface AdapterConfigurationCardProps {
  adapters: Adapter[];
  sourceBusinessComponent: string;
  targetBusinessComponent: string;
  inboundAdapter: string;
  outboundAdapter: string;
  inboundAdapterActive: boolean;
  outboundAdapterActive: boolean;
  onSourceBusinessComponentChange: (value: string) => void;
  onTargetBusinessComponentChange: (value: string) => void;
  onInboundAdapterChange: (value: string) => void;
  onOutboundAdapterChange: (value: string) => void;
  onInboundAdapterActiveChange: (active: boolean) => void;
  onOutboundAdapterActiveChange: (active: boolean) => void;
}

export const AdapterConfigurationCard = ({
  adapters,
  sourceBusinessComponent,
  targetBusinessComponent,
  inboundAdapter,
  outboundAdapter,
  inboundAdapterActive,
  outboundAdapterActive,
  onSourceBusinessComponentChange,
  onTargetBusinessComponentChange,
  onInboundAdapterChange,
  onOutboundAdapterChange,
  onInboundAdapterActiveChange,
  onOutboundAdapterActiveChange,
}: AdapterConfigurationCardProps) => {
  logger.info(LogCategory.UI, 'Debug info', { 
    message: 'AdapterConfigurationCard - Component rendered with props', 
    adapters: adapters.length,
    sourceBusinessComponent,
    targetBusinessComponent,
    inboundAdapter,
    outboundAdapter,
    inboundAdapterActive,
    outboundAdapterActive
  });


  const { businessComponents, loading, getAdaptersForBusinessComponent } = useBusinessComponentAdapters();

  logger.info(LogCategory.UI, 'Debug info', { 
    message: 'AdapterConfigurationCard - Hook data', 
    businessComponents: businessComponents.length,
    loading: loading
  });


  const getAdapterById = (id: string) => adapters.find(adapter => adapter.id === id);
  const [businessComponentAdapters, setBusinessComponentAdapters] = useState<string[]>([]);

  const loadBusinessComponentAdapters = useCallback(async (businessComponentId: string) => {
    const allowedAdapterIds = await getAdaptersForBusinessComponent(businessComponentId);
    setBusinessComponentAdapters(allowedAdapterIds);
  }, [getAdaptersForBusinessComponent]);

  useEffect(() => {
    if (sourceBusinessComponent) {
      loadBusinessComponentAdapters(sourceBusinessComponent);
    }
  }, [sourceBusinessComponent, loadBusinessComponentAdapters]);

  const getFilteredAdapters = (businessComponentId: string) => {
    if (!businessComponentId) return adapters;
    return adapters.filter(adapter => businessComponentAdapters.includes(adapter.id));
  };

  const handleSourceBusinessComponentChange = async (businessComponentId: string) => {
    logger.info(LogCategory.UI, '[AdapterConfigurationCard] Source business component changing', { data: businessComponentId });
    try {
      onSourceBusinessComponentChange(businessComponentId);
      // Reset source adapter if it's not available for the new business component
      const adaptersForBusinessComponent = await getAdaptersForBusinessComponent(businessComponentId);
      logger.info(LogCategory.UI, '[AdapterConfigurationCard] Available adapters for business component', { data: adaptersForBusinessComponent });
      if (inboundAdapter && !adaptersForBusinessComponent.includes(inboundAdapter)) {
        logger.info(LogCategory.UI, '[AdapterConfigurationCard] Resetting source adapter - not available for new business component');
        onInboundAdapterChange('');
      }
    } catch (error) {
      logger.error(LogCategory.UI, '[AdapterConfigurationCard] Error handling source business component change', { error });
    }
  };

  const handleTargetBusinessComponentChange = async (businessComponentId: string) => {
    onTargetBusinessComponentChange(businessComponentId);
    // Reset target adapter if it's not available for the new business component
    const adaptersForBusinessComponent = await getAdaptersForBusinessComponent(businessComponentId);
    if (outboundAdapter && !adaptersForBusinessComponent.includes(outboundAdapter)) {
      onOutboundAdapterChange('');
    }
  };

  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.1s' }}>
      <CardHeader>
        <CardTitle>Source & Target Configuration</CardTitle>
        <CardDescription>Select the source and target systems for your integration</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Source Business Component & Adapter */}
          <div className="space-y-4">
            <div className="space-y-3">
              <Label>Source Business Component *</Label>
              <Select value={sourceBusinessComponent} onValueChange={handleSourceBusinessComponentChange} disabled={loading}>
                <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
                  <SelectValue placeholder="Select source business component" />
                </SelectTrigger>
                <SelectContent>
                  {businessComponents.map((businessComponent) => (
                    <SelectItem key={businessComponent.id} value={businessComponent.id}>
                      <div className="flex items-center gap-2">
                        <span>{businessComponent.name}</span>
                        {businessComponent.description && (
                          <Badge variant="outline" className="text-xs">{businessComponent.description}</Badge>
                        )}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-3">
              <Label>Source Adapter *</Label>
              <Select
                value={inboundAdapter}
                onValueChange={onInboundAdapterChange}
                disabled={!sourceBusinessComponent}
              >
                <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
                  <SelectValue placeholder="Select source system" />
                </SelectTrigger>
                <SelectContent>
                  {getFilteredAdapters(sourceBusinessComponent).map((adapter) => (
                    <SelectItem key={adapter.id} value={adapter.id}>
                      <div className="flex items-center gap-2">
                        <adapter.icon className="h-4 w-4" />
                        <span>{adapter.name}</span>
                        <Badge variant="outline" className="text-xs">{adapter.category}</Badge>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {inboundAdapter && (
                <div className="p-3 bg-muted/50 rounded-lg">
                  <div className="flex items-center gap-2">
                    {getAdapterById(inboundAdapter) && (
                      <>
                        {(() => {
                          const adapter = getAdapterById(inboundAdapter)!;
                          const IconComponent = adapter.icon;
                          return (
                            <>
                              <IconComponent className="h-4 w-4 text-primary" />
                              <span className="font-medium">{adapter.name}</span>
                            </>
                          );
                        })()}
                      </>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* Source Adapter Active Status */}
            {inboundAdapter && (
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="inboundAdapterActive"
                  checked={inboundAdapterActive}
                  onCheckedChange={(checked) => onInboundAdapterActiveChange(checked === true)}
                />
                <Label htmlFor="inboundAdapterActive" className="text-sm font-normal flex items-center gap-1">
                  Active source adapter
                  <Badge variant={inboundAdapterActive ? "default" : "secondary"} className="text-xs">
                    {inboundAdapterActive ? "Active" : "Inactive"}
                  </Badge>
                </Label>
              </div>
            )}
          </div>

          {/* Target Business Component & Adapter */}
          <div className="space-y-4">
            <div className="space-y-3">
              <Label>Target Business Component *</Label>
              <Select value={targetBusinessComponent} onValueChange={handleTargetBusinessComponentChange} disabled={loading}>
                <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
                  <SelectValue placeholder="Select target business component" />
                </SelectTrigger>
                <SelectContent>
                  {businessComponents.map((businessComponent) => (
                    <SelectItem key={businessComponent.id} value={businessComponent.id}>
                      <div className="flex items-center gap-2">
                        <span>{businessComponent.name}</span>
                        {businessComponent.description && (
                          <Badge variant="outline" className="text-xs">{businessComponent.description}</Badge>
                        )}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-3">
              <Label>Target Adapter *</Label>
              <Select
                value={outboundAdapter}
                onValueChange={onOutboundAdapterChange}
                disabled={!targetBusinessComponent}
              >
                <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
                  <SelectValue placeholder="Select target system" />
                </SelectTrigger>
                <SelectContent>
                  {getFilteredAdapters(targetBusinessComponent).map((adapter) => (
                    <SelectItem key={adapter.id} value={adapter.id}>
                      <div className="flex items-center gap-2">
                        <adapter.icon className="h-4 w-4" />
                        <span>{adapter.name}</span>
                        <Badge variant="outline" className="text-xs">{adapter.category}</Badge>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {outboundAdapter && (
                <div className="p-3 bg-muted/50 rounded-lg">
                  <div className="flex items-center gap-2">
                    {getAdapterById(outboundAdapter) && (
                      <>
                        {(() => {
                          const adapter = getAdapterById(outboundAdapter)!;
                          const IconComponent = adapter.icon;
                          return (
                            <>
                              <IconComponent className="h-4 w-4 text-primary" />
                              <span className="font-medium">{adapter.name}</span>
                            </>
                          );
                        })()}
                      </>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* Target Adapter Active Status */}
            {outboundAdapter && (
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="outboundAdapterActive"
                  checked={outboundAdapterActive}
                  onCheckedChange={(checked) => onOutboundAdapterActiveChange(checked === true)}
                />
                <Label htmlFor="outboundAdapterActive" className="text-sm font-normal flex items-center gap-1">
                  Active target adapter
                  <Badge variant={outboundAdapterActive ? "default" : "secondary"} className="text-xs">
                    {outboundAdapterActive ? "Active" : "Inactive"}
                  </Badge>
                </Label>
              </div>
            )}
          </div>
        </div>

        {/* Flow Visualization */}
        {inboundAdapter && outboundAdapter && (
          <div className="mt-6 p-4 bg-gradient-secondary rounded-lg">
            <div className="flex items-center justify-center gap-4">
              <div className="flex items-center gap-2 bg-card p-3 rounded-lg shadow-soft">
                {getAdapterById(inboundAdapter) && (
                  <>
                    {(() => {
                      const adapter = getAdapterById(inboundAdapter)!;
                      const IconComponent = adapter.icon;
                      return (
                        <>
                          <IconComponent className="h-5 w-5 text-primary" />
                          <span className="font-medium">{adapter.name}</span>
                        </>
                      );
                    })()}
                  </>
                )}
              </div>
              <ArrowRight className="h-6 w-6 text-primary animate-pulse" />
              <div className="flex items-center gap-2 bg-card p-3 rounded-lg shadow-soft">
                {getAdapterById(outboundAdapter) && (
                  <>
                    {(() => {
                      const adapter = getAdapterById(outboundAdapter)!;
                      const IconComponent = adapter.icon;
                      return (
                        <>
                          <IconComponent className="h-5 w-5 text-primary" />
                          <span className="font-medium">{adapter.name}</span>
                        </>
                      );
                    })()}
                  </>
                )}
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
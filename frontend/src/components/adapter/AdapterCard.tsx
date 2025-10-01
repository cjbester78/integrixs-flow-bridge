import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { AdapterType } from '@/services/adapterTypeService';
import { AdapterTypeIcon } from './AdapterTypeIcon';
import { 
  ArrowRight, 
  CheckCircle, 
  ArrowUpDown, 
  ArrowDown, 
  ArrowUp,
  Star
} from 'lucide-react';

interface AdapterCardProps {
  adapter: AdapterType;
}

export const AdapterCard: React.FC<AdapterCardProps> = ({ adapter }) => {
  const navigate = useNavigate();

  const handleConfigure = () => {
    navigate(`/create-communication-adapter?type=${adapter.code}`);
  };

  const getDirectionBadge = () => {
    if (adapter.supportsBidirectional) {
      return (
        <Badge variant="secondary" className="gap-1">
          <ArrowUpDown className="h-3 w-3" />
          Bidirectional
        </Badge>
      );
    }
    
    const badges = [];
    if (adapter.supportsInbound) {
      badges.push(
        <Badge key="inbound" variant="secondary" className="gap-1">
          <ArrowDown className="h-3 w-3" />
          Inbound
        </Badge>
      );
    }
    if (adapter.supportsOutbound) {
      badges.push(
        <Badge key="outbound" variant="secondary" className="gap-1">
          <ArrowUp className="h-3 w-3" />
          Outbound
        </Badge>
      );
    }
    
    return badges;
  };

  const getPricingBadge = () => {
    const pricingColors: Record<string, string> = {
      free: 'bg-green-100 text-green-800',
      standard: 'bg-blue-100 text-blue-800',
      premium: 'bg-purple-100 text-purple-800',
      enterprise: 'bg-amber-100 text-amber-800'
    };

    if (adapter.pricingTier) {
      return (
        <Badge className={pricingColors[adapter.pricingTier] || 'bg-gray-100 text-gray-800'}>
          {adapter.pricingTier.charAt(0).toUpperCase() + adapter.pricingTier.slice(1)}
        </Badge>
      );
    }
    return null;
  };

  return (
    <Card className="h-full flex flex-col hover:shadow-lg transition-shadow">
      <CardHeader>
        <div className="flex items-start justify-between">
          <div className="flex items-center space-x-3">
            <AdapterTypeIcon icon={adapter.icon} name={adapter.name} />
            <div>
              <CardTitle className="text-lg flex items-center gap-2">
                {adapter.name}
                {adapter.isCertified && (
                  <CheckCircle className="h-4 w-4 text-green-600" title="Certified" />
                )}
              </CardTitle>
              {adapter.vendor && (
                <p className="text-sm text-muted-foreground">{adapter.vendor}</p>
              )}
            </div>
          </div>
          <div className="flex flex-col gap-1 items-end">
            {adapter.version && (
              <Badge variant="outline" className="text-xs">
                v{adapter.version}
              </Badge>
            )}
            {getPricingBadge()}
          </div>
        </div>
      </CardHeader>
      
      <CardContent className="flex-1 flex flex-col">
        <p className="text-sm text-muted-foreground mb-4 flex-1">
          {adapter.description || 'No description available'}
        </p>
        
        <div className="space-y-3">
          <div className="flex flex-wrap gap-1">
            {getDirectionBadge()}
          </div>
          
          {adapter.supportedProtocols && adapter.supportedProtocols.length > 0 && (
            <div className="flex flex-wrap gap-1">
              {adapter.supportedProtocols.slice(0, 3).map((protocol) => (
                <Badge key={protocol} variant="outline" className="text-xs">
                  {protocol}
                </Badge>
              ))}
              {adapter.supportedProtocols.length > 3 && (
                <Badge variant="outline" className="text-xs">
                  +{adapter.supportedProtocols.length - 3} more
                </Badge>
              )}
            </div>
          )}
        </div>
        
        <div className="mt-4 pt-4 border-t flex items-center justify-between">
          <Button 
            variant="default" 
            size="sm"
            onClick={handleConfigure}
            className="gap-1"
          >
            Configure
            <ArrowRight className="h-4 w-4" />
          </Button>
          
          {adapter.documentationUrl && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => window.open(adapter.documentationUrl, '_blank')}
            >
              Docs
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
};
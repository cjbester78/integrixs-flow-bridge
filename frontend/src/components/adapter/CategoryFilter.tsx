import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { AdapterCategory } from '@/services/adapterTypeService';
import { 
  Package, 
  Users, 
  Building, 
  MessageCircle, 
  ShoppingCart, 
  Database, 
  HardDrive, 
  BarChart, 
  Megaphone, 
  Heart, 
  Truck, 
  Archive,
  CreditCard,
  CheckSquare,
  Cpu,
  Share2
} from 'lucide-react';

interface CategoryFilterProps {
  categories: AdapterCategory[];
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
  adapterCounts?: Record<string, number>;
}

const getCategoryIcon = (iconName?: string) => {
  const iconMap: Record<string, React.FC<{ className?: string }>> = {
    users: Users,
    building: Building,
    'message-circle': MessageCircle,
    'shopping-cart': ShoppingCart,
    database: Database,
    'hard-drive': HardDrive,
    'bar-chart': BarChart,
    megaphone: Megaphone,
    heart: Heart,
    truck: Truck,
    archive: Archive,
    'credit-card': CreditCard,
    'check-square': CheckSquare,
    cpu: Cpu,
    'share-2': Share2
  };

  const Icon = iconName ? iconMap[iconName] || Package : Package;
  return Icon;
};

export const CategoryFilter: React.FC<CategoryFilterProps> = ({
  categories,
  selectedCategory,
  onCategoryChange,
  adapterCounts = {}
}) => {
  const topLevelCategories = categories.filter(cat => !cat.parentCategoryId);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Categories</CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <div className="space-y-1 p-2">
          <Button
            variant={selectedCategory === 'all' ? 'secondary' : 'ghost'}
            className="w-full justify-start"
            onClick={() => onCategoryChange('all')}
          >
            <Package className="mr-2 h-4 w-4" />
            All Adapters
            {adapterCounts.total && (
              <Badge variant="secondary" className="ml-auto">
                {adapterCounts.total}
              </Badge>
            )}
          </Button>

          {topLevelCategories
            .sort((a, b) => a.displayOrder - b.displayOrder)
            .map((category) => {
              const Icon = getCategoryIcon(category.icon);
              const count = adapterCounts[category.code];
              
              return (
                <Button
                  key={category.id}
                  variant={selectedCategory === category.code ? 'secondary' : 'ghost'}
                  className="w-full justify-start"
                  onClick={() => onCategoryChange(category.code)}
                >
                  <Icon className="mr-2 h-4 w-4" />
                  {category.name}
                  {count && (
                    <Badge variant="outline" className="ml-auto">
                      {count}
                    </Badge>
                  )}
                </Button>
              );
            })}
        </div>
      </CardContent>
    </Card>
  );
};
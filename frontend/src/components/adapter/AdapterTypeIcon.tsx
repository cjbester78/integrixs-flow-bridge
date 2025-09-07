import React from 'react';
import { 
  Package,
  Cloud,
  Database,
  Mail,
  MessageSquare,
  FileText,
  HardDrive,
  Globe,
  Server,
  Webhook,
  ShoppingCart,
  CreditCard,
  BarChart,
  Users,
  Building,
  Heart,
  Truck,
  Archive,
  Cpu,
  Share2,
  Smartphone,
  Lock,
  Network,
  Shield,
  Cable,
  Activity,
  Layers,
  FileSpreadsheet,
  FileCode,
  Zap,
  Send,
  Key
} from 'lucide-react';

interface AdapterTypeIconProps {
  icon?: string;
  name: string;
  className?: string;
}

const iconMap: Record<string, React.FC<{ className?: string }>> = {
  // Categories
  users: Users,
  building: Building,
  'message-circle': MessageSquare,
  'shopping-cart': ShoppingCart,
  database: Database,
  'hard-drive': HardDrive,
  'bar-chart': BarChart,
  megaphone: Send,
  heart: Heart,
  truck: Truck,
  archive: Archive,
  'credit-card': CreditCard,
  'check-square': FileText,
  cpu: Cpu,
  'share-2': Share2,
  
  // Adapter types
  salesforce: Cloud,
  sap: Building,
  oracle: Database,
  microsoft: Server,
  aws: Cloud,
  google: Globe,
  
  // Protocols
  rest: Globe,
  soap: Webhook,
  ftp: HardDrive,
  sftp: Lock,
  file: FileText,
  email: Mail,
  sms: Smartphone,
  jdbc: Database,
  jms: MessageSquare,
  kafka: Layers,
  http: Network,
  https: Shield,
  odata: Cable,
  rfc: Activity,
  idoc: FileCode,
  
  // Default
  adapter: Zap,
  integration: Network,
  default: Package
};

export const AdapterTypeIcon: React.FC<AdapterTypeIconProps> = ({ 
  icon, 
  name,
  className = "h-10 w-10"
}) => {
  // Try to find icon by explicit icon name
  if (icon) {
    const IconComponent = iconMap[icon.toLowerCase()];
    if (IconComponent) {
      return <IconComponent className={className} />;
    }
  }
  
  // Try to find icon by adapter name
  const nameLower = name.toLowerCase();
  for (const [key, IconComponent] of Object.entries(iconMap)) {
    if (nameLower.includes(key)) {
      return <IconComponent className={className} />;
    }
  }
  
  // Default icon
  return <Package className={className} />;
};
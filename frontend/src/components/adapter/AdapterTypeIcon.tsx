import React from 'react';
import { 
  Package,
  Cloud,
  Database,
  Mail,
  MessageSquare,
  MessageCircle,
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
  Key,
  Camera,
  Twitter,
  Linkedin,
  Youtube
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
  ibmmq: MessageSquare,
  kafka: Layers,
  http: Network,
  https: Shield,
  odata: Cable,
  rfc: Activity,
  idoc: FileCode,
  facebook: Zap,  // Using Zap icon as placeholder since lucide-react doesn't have Facebook icon
  facebookads: CreditCard,  // Using CreditCard icon for Facebook Ads
  instagram: Camera,  // Using Camera icon for Instagram
  whatsapp: MessageSquare,  // MessageSquare icon for WhatsApp
  twitter: Twitter,  // Twitter/X icon
  twitterads: CreditCard,  // Using CreditCard icon for Twitter Ads
  linkedin: Linkedin,  // LinkedIn icon
  linkedinads: CreditCard,  // Using CreditCard icon for LinkedIn Ads
  youtube: Youtube,  // YouTube icon
  youtubeanalytics: BarChart,  // Using BarChart icon for YouTube Analytics
  tiktokbusiness: Share2,  // Using Share2 icon for TikTok Business
  tiktokcontent: Share2,  // Using Share2 icon for TikTok Content
  tiktok: Share2,  // Using Share2 icon for TikTok
  facebookmessenger: MessageSquare,  // Using MessageSquare icon for Facebook Messenger
  pinterest: Share2,  // Using Share2 icon for Pinterest
  reddit: MessageCircle,  // Using MessageCircle icon for Reddit
  snapchatads: Camera,  // Using Camera icon for Snapchat Ads
  snapchat: Camera,  // Using Camera icon for Snapchat
  discord: MessageSquare,  // Using MessageSquare icon for Discord
  
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
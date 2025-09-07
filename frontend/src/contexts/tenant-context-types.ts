import { createContext } from 'react';

export interface Tenant {
  id: string;
  name: string;
  displayName: string;
  subdomain?: string;
  status: string;
  planId: string;
  userRole?: string;
  primary?: boolean;
}

export interface TenantSubscription {
  id: number;
  planId: string;
  planName: string;
  status: string;
  features: string[];
  quotas: Record<string, number>;
  daysRemaining: number;
}

export interface TenantUsage {
  executions: number;
  messages: number;
  apiCalls: number;
  storageGb: number;
  users: number;
  flows: number;
  quotas: Record<string, number>;
}

export interface TenantContextType {
  currentTenant: Tenant | null;
  userTenants: Tenant[];
  subscription: TenantSubscription | null;
  usage: TenantUsage | null;
  loading: boolean;
  switchTenant: (tenantId: string) => Promise<void>;
  refreshTenants: () => Promise<void>;
  refreshSubscription: () => Promise<void>;
  refreshUsage: () => Promise<void>;
  hasFeature: (feature: string) => boolean;
  isOverQuota: (metric: string) => boolean;
  getQuotaPercentage: (metric: string) => number;
}

export const TenantContext = createContext<TenantContextType | undefined>(undefined);
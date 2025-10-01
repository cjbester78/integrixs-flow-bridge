import { useState, useEffect, ReactNode, useCallback, useContext } from 'react';
import { apiClient } from '@/lib/api-client';
import { logger, LogCategory } from '@/lib/logger';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/use-toast';
import { TenantContext, type Tenant, type TenantSubscription, type TenantUsage, type TenantContextType } from './tenant-context-types';

export function TenantProvider({ children }: { children: ReactNode }) {
 const { toast } = useToast();
 const { isAuthenticated } = useAuth();
 const [currentTenant, setCurrentTenant] = useState<Tenant | null>(null);
 const [userTenants, setUserTenants] = useState<Tenant[]>([]);
 const [subscription, setSubscription] = useState<TenantSubscription | null>(null);
 const [usage, setUsage] = useState<TenantUsage | null>(null);
 const [loading, setLoading] = useState(true);

 // Load current tenant
 const loadCurrentTenant = async () => {
    try {
      const response = await apiClient.get<Tenant>('/tenants/current');
      setCurrentTenant(response);

      // Store in localStorage for persistence
      if (response && 'id' in response) {
        localStorage.setItem('currentTenantId', response.id);
      }
      // Tenant header is now automatically included in all API requests via api-client.ts
    } catch (error) {
      logger.error(LogCategory.ERROR, 'Failed to load current tenant', { error: error });
    }
  };

 // Load user's tenants
 const loadUserTenants = async () => {
    try {
      const response = await apiClient.get<Tenant[]>('/tenants/my-tenants');
      setUserTenants(response);
    } catch (error) {
      logger.error(LogCategory.ERROR, 'Failed to load user tenants', { error: error });
    }
  };

 // Load subscription info
 const loadSubscription = useCallback(async () => {
    if (!currentTenant) return;

    try {
      const response = await apiClient.get<TenantSubscription>(`/tenants/${currentTenant.id}/subscription`);
      setSubscription(response);
    } catch (error) {
      logger.error(LogCategory.ERROR, 'Failed to load subscription', { error: error });
    }
  }, [currentTenant]);

 // Load usage info
 const loadUsage = useCallback(async () => {
    if (!currentTenant) return;

    try {
      const response = await apiClient.get<TenantUsage>(`/tenants/${currentTenant.id}/usage`);
      setUsage(response);
    } catch (error) {
      logger.error(LogCategory.ERROR, 'Failed to load usage', { error: error });
    }
  }, [currentTenant]);

 // Switch tenant
 const switchTenant = async (tenantId: string) => {
    try {
      await apiClient.post(`/tenants/switch/${tenantId}`);

 // Reload tenant data
 await loadCurrentTenant();
 await loadSubscription();
 await loadUsage();

 toast({ title: "Success", description: 'Switched tenant successfully' });

 // Reload the page to ensure all data is refreshed
      window.location.reload();
    } catch (error) {
      toast({ title: "Error", description: 'Failed to switch tenant', variant: "destructive" });
      throw error;
    }
  };

 // Refresh functions
 const refreshTenants = async () => {
    await loadUserTenants();
  };

 const refreshSubscription = async () => {
    await loadSubscription();
  };

 const refreshUsage = async () => {
    await loadUsage();
  };

 // Feature checking
 const hasFeature = (feature: string): boolean => {
 return subscription?.features?.includes(feature) ?? false;
 };

 // Quota checking
 const isOverQuota = (metric: string): boolean => {
 if (!usage || !usage.quotas) return false;

 const quota = usage.quotas[metric];
 if (!quota || quota === -1) return false; // Unlimited

 const usageValue = getUsageValue(metric);
 return usageValue > quota;
 };

 const getQuotaPercentage = (metric: string): number => {
 if (!usage || !usage.quotas) return 0;

 const quota = usage.quotas[metric];
 if (!quota || quota === -1) return 0; // Unlimited

 const usageValue = getUsageValue(metric);
 return Math.min((usageValue / quota) * 100, 100);
 };

 const getUsageValue = (metric: string): number => {
 if (!usage) return 0;

 switch (metric) {
 case 'executions_per_month':
 return usage.executions;
 case 'messages_per_month':
 return usage.messages;
 case 'api_calls_per_month':
 return usage.apiCalls;
 case 'storage_gb':
 return usage.storageGb;
 case 'users':
 return usage.users;
 case 'flows':
 return usage.flows;
 default:
 return 0;
 }
 };

 // Initialize on mount
 useEffect(() => {
 if (isAuthenticated) {
 const initialize = async () => {
 setLoading(true);

 // Check for saved tenant ID
 const savedTenantId = localStorage.getItem('currentTenantId');
 if (savedTenantId) {
 // Tenant header is automatically included in all API requests via api-client.ts
 // The tenant ID from localStorage is added as X-Tenant-ID header
 }

 await Promise.all([
 loadCurrentTenant(),
 loadUserTenants()
 ]);

 setLoading(false);
 };

 initialize();
 }
 }, [isAuthenticated]);

 // Load subscription and usage when tenant changes
 useEffect(() => {
 if (currentTenant) {
 loadSubscription();
 loadUsage();
 }
 }, [currentTenant, loadSubscription, loadUsage]);

 const value: TenantContextType = {
 currentTenant,
 userTenants,
 subscription,
 usage,
 loading,
 switchTenant,
 refreshTenants,
 refreshSubscription,
 refreshUsage,
 hasFeature,
 isOverQuota,
 getQuotaPercentage
 };

 return (
 <TenantContext.Provider value={value}>
 {children}
 </TenantContext.Provider>
 );
}

export function useTenant() {
  const context = useContext(TenantContext);
  if (context === undefined) {
    throw new Error('useTenant must be used within a TenantProvider');
  }
  return context;
}
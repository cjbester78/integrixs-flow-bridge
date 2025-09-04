import { useTenant } from '@/contexts/TenantContext';

/**
 * Hook to check feature availability
 */
export function useFeature(feature: string): boolean {
 const { hasFeature } = useTenant();
 return hasFeature(feature);
}
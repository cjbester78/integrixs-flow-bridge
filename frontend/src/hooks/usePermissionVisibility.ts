import React from 'react';
import { useEnvironmentPermissions } from '@/hooks/useEnvironmentPermissions-no-query';

/**
 * Hook for checking if a UI element should be visible
 */
export const usePermissionVisibility = (element: string): boolean => {
  const { permissions, isDevelopment } = useEnvironmentPermissions();

  return React.useMemo(() => {
    switch (element) {
      case 'createFlowButton':
      case 'createAdapterButton':
      case 'createBusinessComponentButton':
      case 'createDataStructureButton':
        return isDevelopment ?? false;
      case 'importFlowButton':
      case 'deployButton':
      case 'exportButton':
      case 'adapterConfigButton':
        return true;
      case 'adminPanel':
        return permissions?.isAdmin ?? false;
      default:
        return true;
    }
  }, [element, permissions, isDevelopment]);
};
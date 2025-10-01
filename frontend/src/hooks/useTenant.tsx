import { useContext } from 'react';
import { TenantContext } from '../contexts/tenant-context-types';

export function useTenant() {
  const context = useContext(TenantContext);
  if (context === undefined) {
    throw new Error('useTenant must be used within a TenantProvider');
  }
  return context;
}
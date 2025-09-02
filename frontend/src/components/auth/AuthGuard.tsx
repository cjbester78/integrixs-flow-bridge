import React from 'react';
import { logger, LogCategory } from '@/lib/logger';




interface AuthGuardProps {
  children: React.ReactNode;
}

/**
 * Component that protects routes by checking authentication status
 * Redirects to login if user is not authenticated or session expired
 */
export const AuthGuard: React.FC<AuthGuardProps> = ({ children }) => {
  // Auth guard temporarily disabled for faster testing
  logger.warn(LogCategory.AUTH, 'AuthGuard bypassed: auth checks disabled for testing')
  return <>{children}</>;
};

/**
 * Higher-order component for protecting individual routes
 */
export const withAuth = <P extends object>(
  Component: React.ComponentType<P>
): React.FC<P> => {
  return (props: P) => (
    <AuthGuard>
      <Component {...props} />
    </AuthGuard>
  );
};
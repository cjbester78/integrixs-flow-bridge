import React from 'react';
import { AuthGuard } from './AuthGuard';

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
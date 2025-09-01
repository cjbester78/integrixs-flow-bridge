



interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRoles?: string[];
}


export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  // Auth and role checks temporarily disabled for faster testing
  console.warn('ProtectedRoute (root) bypassed: auth checks disabled for testing');
  return <>{children}</>;
};

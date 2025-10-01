import React from 'react';
import { Outlet, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { authService } from '@/services/authService';

/**
 * Component that wraps routes requiring authentication
 * Uses React Router's Outlet to render child routes
 */
export const ProtectedRoute: React.FC = () => {
 const location = useLocation();
 const auth: any = useAuth?.() as any;

 const isLoading: boolean = auth?.isLoading ?? false;
 const isAuthenticated: boolean =
 ((auth?.isAuthenticated as boolean | undefined) ?? !!auth?.user) || authService.isAuthenticated();

 if (isLoading) {
 return <div className="p-6">Checking session...</div>
 }

 if (!isAuthenticated) {
 return <Navigate to="/login" replace state={{ from: location }} />;
 }

 return <Outlet />;
};
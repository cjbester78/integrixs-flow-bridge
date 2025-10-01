import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService, type User } from '../services/authService';
import { useToast } from '@/hooks/use-toast';
import { logger, LogCategory } from '@/lib/logger';
import { AuthContext } from './auth-context-types';


export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
 const [user, setUser] = useState<User | null>(null);
 const [isLoading, setIsLoading] = useState(true);
 const [users, setUsers] = useState<User[]>([]);
 const [tokenExpiry, setTokenExpiry] = useState<number | null>(null);
 const navigate = useNavigate();
 const { toast } = useToast();

 // Initialize authentication state
 useEffect(() => {
 const initAuth = async () => {
    try {
      // Skip auth check if we're on the login page
      if (window.location.pathname === '/login') {
        logger.info(LogCategory.AUTH, 'AuthContext: On login page, skipping auth check');
        setIsLoading(false);
        return;
      }
 // Check if we have a token stored
 const token = authService.getToken();
 if (!token) {
 logger.info(LogCategory.AUTH, 'AuthContext: No token found, skipping initialization');
 setIsLoading(false);
 return;
 }

 // Check if token is expired
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expiry = payload.exp * 1000; // Convert to milliseconds
 if (expiry < Date.now()) {
          logger.info(LogCategory.AUTH, 'AuthContext: Token expired, clearing auth data');
          localStorage.removeItem('integration_platform_token');
          localStorage.removeItem('integration_platform_refresh_token');
          localStorage.removeItem('integration_platform_user');
          setIsLoading(false);
          return;
        }
        
        setTokenExpiry(expiry);
      } catch (error) {
        logger.warn(LogCategory.SYSTEM, 'Failed to parse token', { data: error });
        setIsLoading(false);
        return;
      }

 // Try to get user profile
      try {
        const response = await authService.getProfile();
        if (response.success && response.data) {
          setUser(response.data);
        } else {
          logger.warn(LogCategory.AUTH, 'AuthContext: Profile fetch failed');
          // Don't clear auth data - let user retry
        }
      } catch (profileError) {
        logger.warn(LogCategory.AUTH, 'AuthContext: Profile fetch error', { data: profileError });
        // Don't clear auth data on network errors
      }
    } catch (error) {
      logger.error(LogCategory.AUTH, 'Auth initialization error', { error: error });
    } finally {
      setIsLoading(false);
    }
 };

 initAuth();
 }, []);

 const logout = useCallback(async () => {
    try {
      // Clear state first to prevent any auth checks
      setUser(null);
      setTokenExpiry(null);

      // Try to call logout API but don't wait for it
      authService.logout().catch(error => {
        logger.warn(LogCategory.SYSTEM, 'Logout API call failed', { data: error });
      });

      // Navigate to login immediately
      navigate('/login');
      toast({ title: "Success", description: 'Logged out successfully' });
    } catch (error) {
      logger.error(LogCategory.ERROR, 'Logout error', { error: error });
      // Still navigate to login
      navigate('/login');
    }
  }, [navigate, toast]);

 // Auto-logout when token expires
 useEffect(() => {
 if (!tokenExpiry) return;

 const timeUntilExpiry = tokenExpiry - Date.now();
 if (timeUntilExpiry <= 0) {
 logout();
 return;
 }

 const timer = setTimeout(() => {
 logout();
 }, timeUntilExpiry);

 return () => clearTimeout(timer);
 }, [tokenExpiry, logout]);

 const login = async (username: string, password: string, redirectTo?: string): Promise<boolean> => {
 try {
 setIsLoading(true);
 const response = await authService.login({ username, password });
 if (response.success && response.data) {
 setUser(response.data.user);

 // Set token expiry
 const expiryTime = Date.now() + (response.data.expiresIn * 1000);
 setTokenExpiry(expiryTime);

 toast({ title: "Success", description: 'Login successful' });

 // Use a slight delay to ensure state updates are complete before navigation
 setTimeout(() => {
 const destination = redirectTo || '/dashboard';
 navigate(destination);
 }, 100);

 return true;
 } else {
 toast({ title: "Error", description: response.error || 'Login failed', variant: "destructive" });
 return false;}
} catch (error) {
 logger.error(LogCategory.AUTH, 'Login error', { error: error });
 toast({ title: "Error", description: 'Login failed. Please try again.', variant: "destructive" });
 return false;
 } finally {
 setIsLoading(false);
 }
 };

 const isSessionValid = (): boolean => {
 // Check if we have a token
 if (!authService.isAuthenticated()) {
 return false;
 }

 // Check if token has expired
 if (tokenExpiry && Date.now() >= tokenExpiry) {
 return false;
 }

 // Check if we have user data
 if (!user) {
 return false;
 }

 return true;
 };

 const checkSession = async (): Promise<boolean> => {
    try {
      // First check local session validity
      if (!isSessionValid()) {
        await logout();
        return false;
      }
 // Verify session with backend
      const response = await authService.getProfile();
      if (response.success && response.data) {
        setUser(response.data);

        // Update token expiry if we get a refreshed token
        const token = authService.getToken();
        if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            setTokenExpiry(payload.exp * 1000);
          } catch (error) {
            logger.warn(LogCategory.SYSTEM, 'Failed to parse token expiry', { data: error });
          }
        }

 return true;
 } else {
        // Session is invalid, logout
        await logout();
        return false;
      }
    } catch (error) {
 logger.error(LogCategory.ERROR, 'Session check error', { error: error });
 // On error, assume session is invalid
 await logout();
 return false;
 }
 };

 const getAllUsers = () => {
 return users;
 };

 const createUser = async (userData: Omit<User, 'id' | 'createdAt' | 'updatedAt'>) => {
 try {
 const response = await authService.register({
 ...userData,
 password: 'temp123', // Temporary password, should be changed on first login
 });

 if (response.success && response.data) {
 setUsers(prev => [...prev, response.data!]);
 toast({ title: "Success", description: 'User created successfully' });
 } else {
        toast({ title: "Error", description: response.error || 'Failed to create user', variant: "destructive" });
      }
    } catch (error) {
 logger.error(LogCategory.ERROR, 'Create user error', { error: error });
 toast({ title: "Error", description: 'Failed to create user', variant: "destructive" });
 }
 };

 const updateUser = async (id: string, userData: Partial<User>) => {
 try {
 // Note: This would need a specific API endpoint for admin user updates
 // For now, we'll update the local state
 setUsers(prev => prev.map(user =>
 user.id === id ? { ...user, ...userData } : user
 ))
 toast({ title: "Success", description: 'User updated successfully' });
    } catch (error) {
 logger.error(LogCategory.ERROR, 'Update user error', { error: error });
 toast({ title: "Error", description: 'Failed to update user', variant: "destructive" });
 }
 };

 const deleteUser = async (id: string) => {
 try {
 // Note: This would need a specific API endpoint for admin user deletion
 // For now, we'll update the local state
 setUsers(prev => prev.filter(user => user.id !== id));
      toast({ title: "Success", description: 'User deleted successfully' });
    } catch (error) {
 logger.error(LogCategory.ERROR, 'Delete user error', { error: error });
 toast({ title: "Error", description: 'Failed to delete user', variant: "destructive" });
 }
 };

 const value = {
 user,
 login,
 logout,
 isAuthenticated: !!user && authService.isAuthenticated(),
 isLoading,
 tokenExpiry,
 checkSession,
 isSessionValid,
 getAllUsers,
 createUser,
 updateUser,
 deleteUser
 };

 return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
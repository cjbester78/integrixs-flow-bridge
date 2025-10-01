import { createContext } from 'react';
import type { User } from '../services/authService';

export interface AuthContextType {
  user: User | null;
  login: (username: string, password: string, redirectTo?: string) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
  tokenExpiry: number | null;
  checkSession: () => Promise<boolean>;
  isSessionValid: () => boolean;
  getAllUsers: () => User[];
  createUser: (userData: Omit<User, 'id' | 'createdAt' | 'updatedAt'>) => Promise<void>;
  updateUser: (id: string, userData: Partial<User>) => Promise<void>;
  deleteUser: (id: string) => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
import {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
  useCallback,
  useRef,
} from 'react';
import { authApi } from '../api/authApi';
import { setAuthErrorHandler } from '../api/client';
import type { User, LoginRequest, RegisterRequest } from '../api/types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (request: LoginRequest) => Promise<void>;
  register: (request: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

function safeParseUser(stored: string | null): User | null {
  if (!stored) return null;
  try {
    return JSON.parse(stored) as User;
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() =>
    safeParseUser(localStorage.getItem(USER_KEY))
  );
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [isLoading, setIsLoading] = useState(true);

  // Verify token on mount
  useEffect(() => {
    const verifyToken = async () => {
      if (!token) {
        setIsLoading(false);
        return;
      }

      try {
        const currentUser = await authApi.getCurrentUser(token);
        setUser(currentUser);
        localStorage.setItem(USER_KEY, JSON.stringify(currentUser));
      } catch {
        // Token invalid, clear auth state
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        setToken(null);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    verifyToken();
  }, [token]);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authApi.login(request);
    const newUser: User = {
      id: response.userId,
      email: response.email,
      role: response.role,
      emailVerified: response.emailVerified,
    };

    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(newUser));
    setToken(response.token);
    setUser(newUser);
  }, []);

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await authApi.register(request);
    const newUser: User = {
      id: response.userId,
      email: response.email,
      role: response.role,
      emailVerified: response.emailVerified,
    };

    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(newUser));
    setToken(response.token);
    setUser(newUser);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setToken(null);
    setUser(null);
  }, []);

  // Register 401 handler for auto-logout on token expiration
  const logoutRef = useRef(logout);
  logoutRef.current = logout;

  useEffect(() => {
    setAuthErrorHandler(() => {
      logoutRef.current();
    });
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isLoading,
        isAuthenticated: !!user && !!token,
        isAdmin: user?.role === 'ADMIN',
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

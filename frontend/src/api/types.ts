export interface Example {
  id: number;
  userId: number | null;
  name: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface HealthResponse {
  status: 'UP' | 'DOWN';
  components?: Record<string, { status: string }>;
}

// Auth types
export interface User {
  id: number;
  email: string;
  role: 'USER' | 'ADMIN';
  emailVerified?: boolean;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  role: 'USER' | 'ADMIN';
  emailVerified: boolean;
}

export interface MessageResponse {
  message: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface ApiError {
  error: string;
  message: string;
  details?: Record<string, string>;
}

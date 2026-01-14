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

export interface LocationDto {
  latitude: number;
  longitude: number;
}

export interface LoginRequest {
  email: string;
  password: string;
  location?: LocationDto;
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

// Admin types
export interface AdminUser {
  id: number;
  email: string;
  role: 'USER' | 'ADMIN';
  emailVerified: boolean;
  lastLoginAt: string | null;
  createdAt: string;
}

export interface ChangeRoleRequest {
  role: 'USER' | 'ADMIN';
}

// Login History types
export interface LoginHistoryEntry {
  id: number;
  loggedInAt: string;
  success: boolean;
  failureReason: string | null;
  latitude: number | null;
  longitude: number | null;
  locationSource: 'GPS' | 'IP' | null;
  country: string | null;
  city: string | null;
  ipAddress: string | null;
  deviceType: string | null;
  browser: string | null;
  os: string | null;
}

export interface LoginHistoryPage {
  content: LoginHistoryEntry[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

import { API_BASE_URL } from '../../../shared/api/config';
import type {
  AuthResponse,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  User,
} from '../../../shared/api/types';

const AUTH_URL = `${API_BASE_URL}/api/auth`;

class AuthApi {
  async login(request: LoginRequest): Promise<AuthResponse> {
    const response = await fetch(`${AUTH_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Login failed' }));
      throw new Error(error.message || 'Invalid credentials');
    }

    return response.json();
  }

  async register(request: RegisterRequest): Promise<AuthResponse> {
    const response = await fetch(`${AUTH_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Registration failed' }));
      if (error.error === 'EMAIL_ALREADY_EXISTS') {
        throw new Error('Email already registered');
      }
      if (error.details) {
        const firstError = Object.values(error.details)[0];
        throw new Error(firstError as string);
      }
      throw new Error(error.message || 'Registration failed');
    }

    return response.json();
  }

  async getCurrentUser(token: string): Promise<User> {
    const response = await fetch(`${AUTH_URL}/me`, {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      throw new Error('Failed to get current user');
    }

    return response.json();
  }

  async verifyEmail(token: string): Promise<MessageResponse> {
    const response = await fetch(`${AUTH_URL}/verify-email`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Verification failed' }));
      throw new Error(error.message || 'Invalid or expired token');
    }

    return response.json();
  }

  async resendVerification(email: string): Promise<MessageResponse> {
    const response = await fetch(`${AUTH_URL}/resend-verification`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to resend' }));
      throw new Error(error.message || 'Failed to resend verification email');
    }

    return response.json();
  }

  async forgotPassword(email: string): Promise<MessageResponse> {
    const response = await fetch(`${AUTH_URL}/forgot-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Request failed' }));
      throw new Error(error.message || 'Failed to send reset email');
    }

    return response.json();
  }

  async resetPassword(token: string, password: string): Promise<MessageResponse> {
    const response = await fetch(`${AUTH_URL}/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, password }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Reset failed' }));
      throw new Error(error.message || 'Invalid or expired token');
    }

    return response.json();
  }

  async changePassword(
    token: string,
    currentPassword: string,
    newPassword: string
  ): Promise<MessageResponse> {
    const response = await fetch(`${AUTH_URL}/change-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ currentPassword, newPassword }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Change failed' }));
      throw new Error(error.message || 'Failed to change password');
    }

    return response.json();
  }
}

export const authApi = new AuthApi();

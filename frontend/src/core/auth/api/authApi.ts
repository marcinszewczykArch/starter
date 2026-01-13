import { apiClient, ApiClientError } from '../../../shared/api/client';
import type {
  AuthResponse,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  User,
} from '../../../shared/api/types';

const AUTH_PATH = '/api/auth';

/** Options for public (unauthenticated) endpoints */
const publicOptions = { skipAuth: true };

/**
 * Extracts user-friendly error message from API errors.
 * Handles validation errors by returning the first field error.
 */
function getErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiClientError) {
    // Handle validation errors - return first field error
    if (error.details) {
      const firstError = Object.values(error.details)[0];
      if (firstError) return firstError;
    }
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return fallback;
}

export const authApi = {
  async login(request: LoginRequest): Promise<AuthResponse> {
    try {
      return await apiClient.post<AuthResponse>(`${AUTH_PATH}/login`, request, publicOptions);
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Invalid credentials'));
    }
  },

  async register(request: RegisterRequest): Promise<AuthResponse> {
    try {
      return await apiClient.post<AuthResponse>(`${AUTH_PATH}/register`, request, publicOptions);
    } catch (error) {
      if (error instanceof ApiClientError && error.errorCode === 'EMAIL_ALREADY_EXISTS') {
        throw new Error('Email already registered');
      }
      throw new Error(getErrorMessage(error, 'Registration failed'));
    }
  },

  async getCurrentUser(token: string): Promise<User> {
    // Pass token explicitly for this call (used during token verification)
    try {
      return await apiClient.get<User>(`${AUTH_PATH}/me`, {
        headers: { Authorization: `Bearer ${token}` },
        skipAuth: true, // We're passing token manually
      });
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Failed to get current user'));
    }
  },

  async verifyEmail(token: string): Promise<MessageResponse> {
    try {
      return await apiClient.post<MessageResponse>(
        `${AUTH_PATH}/verify-email`,
        { token },
        publicOptions
      );
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Invalid or expired token'));
    }
  },

  async resendVerification(email: string): Promise<MessageResponse> {
    try {
      return await apiClient.post<MessageResponse>(
        `${AUTH_PATH}/resend-verification`,
        { email },
        publicOptions
      );
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Failed to resend verification email'));
    }
  },

  async forgotPassword(email: string): Promise<MessageResponse> {
    try {
      return await apiClient.post<MessageResponse>(
        `${AUTH_PATH}/forgot-password`,
        { email },
        publicOptions
      );
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Failed to send reset email'));
    }
  },

  async resetPassword(token: string, password: string): Promise<MessageResponse> {
    try {
      return await apiClient.post<MessageResponse>(
        `${AUTH_PATH}/reset-password`,
        { token, password },
        publicOptions
      );
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Invalid or expired token'));
    }
  },

  async changePassword(
    token: string,
    currentPassword: string,
    newPassword: string
  ): Promise<MessageResponse> {
    try {
      return await apiClient.post<MessageResponse>(
        `${AUTH_PATH}/change-password`,
        { currentPassword, newPassword },
        {
          headers: { Authorization: `Bearer ${token}` },
          skipAuth: true, // We're passing token manually
        }
      );
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Failed to change password'));
    }
  },
};

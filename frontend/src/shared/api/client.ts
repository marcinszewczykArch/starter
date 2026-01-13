import { API_BASE_URL } from './config';
import type { ApiError } from './types';

interface RequestOptions extends RequestInit {
  params?: Record<string, string>;
  /** Skip adding Authorization header (for public endpoints) */
  skipAuth?: boolean;
}

const TOKEN_KEY = 'auth_token';

// Handler for 401 errors (token expired)
let authErrorHandler: (() => void) | null = null;

export function setAuthErrorHandler(handler: () => void) {
  authErrorHandler = handler;
}

/** Custom error class that includes API error details */
export class ApiClientError extends Error {
  constructor(
    public readonly status: number,
    public readonly errorCode: string,
    message: string,
    public readonly details?: Record<string, string>
  ) {
    super(message);
    this.name = 'ApiClientError';
  }
}

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private getAuthHeaders(skipAuth?: boolean): Record<string, string> {
    if (skipAuth) return {};
    const token = localStorage.getItem(TOKEN_KEY);
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  private buildUrl(endpoint: string, params?: Record<string, string>): string {
    const url = new URL(endpoint, this.baseUrl || window.location.origin);

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        url.searchParams.append(key, value);
      });
    }

    return url.toString();
  }

  private async handleResponse<T>(response: Response, skipAuth?: boolean): Promise<T> {
    if (!response.ok) {
      // Auto-logout on 401 (token expired/invalid) - but only for authenticated requests
      if (response.status === 401 && !skipAuth && authErrorHandler) {
        authErrorHandler();
      }

      // Try to parse error as JSON
      let errorData: ApiError | null = null;
      try {
        errorData = await response.json();
      } catch {
        // Not JSON, use generic message
      }

      const message = errorData?.message || `Request failed with status ${response.status}`;
      const errorCode = errorData?.error || 'UNKNOWN_ERROR';

      throw new ApiClientError(response.status, errorCode, message, errorData?.details);
    }

    // Handle empty responses
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      return {} as T;
    }

    return response.json();
  }

  async get<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(endpoint, options?.params);
    const response = await fetch(url, {
      ...options,
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(options?.skipAuth),
        ...options?.headers,
      },
    });

    return this.handleResponse<T>(response, options?.skipAuth);
  }

  async post<T>(endpoint: string, data?: unknown, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(endpoint, options?.params);
    const response = await fetch(url, {
      ...options,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(options?.skipAuth),
        ...options?.headers,
      },
      body: data ? JSON.stringify(data) : undefined,
    });

    return this.handleResponse<T>(response, options?.skipAuth);
  }

  async put<T>(endpoint: string, data?: unknown, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(endpoint, options?.params);
    const response = await fetch(url, {
      ...options,
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(options?.skipAuth),
        ...options?.headers,
      },
      body: data ? JSON.stringify(data) : undefined,
    });

    return this.handleResponse<T>(response, options?.skipAuth);
  }

  async delete<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(endpoint, options?.params);
    const response = await fetch(url, {
      ...options,
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(options?.skipAuth),
        ...options?.headers,
      },
    });

    return this.handleResponse<T>(response, options?.skipAuth);
  }

  async patch<T>(endpoint: string, data?: unknown, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(endpoint, options?.params);
    const response = await fetch(url, {
      ...options,
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(options?.skipAuth),
        ...options?.headers,
      },
      body: data ? JSON.stringify(data) : undefined,
    });

    return this.handleResponse<T>(response, options?.skipAuth);
  }
}

export const apiClient = new ApiClient(API_BASE_URL);

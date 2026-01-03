// API base URL - uses Vite's proxy in development, can be overridden via env var
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

// Backend URL for direct access (health checks bypass proxy)
export const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  examples: '/api/v1/example',
} as const;

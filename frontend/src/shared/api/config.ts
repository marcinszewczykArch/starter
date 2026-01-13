// API base URL - uses relative URLs (works with both Vite proxy in dev and Nginx in prod)
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export const API_ENDPOINTS = {
  examples: '/api/v1/example',
} as const;

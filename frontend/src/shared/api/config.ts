// API base URL - uses relative URLs (works with both Vite proxy in dev and Nginx in prod)
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export const API_ENDPOINTS = {
  examples: '/api/v1/example',
} as const;

// Feature flags
export const FEATURES = {
  /**
   * Enable GPS location request on login.
   * When enabled, browser will ask user for location permission.
   * Set VITE_GPS_ENABLED=false to disable.
   */
  gpsEnabled: import.meta.env.VITE_GPS_ENABLED !== 'false',
} as const;

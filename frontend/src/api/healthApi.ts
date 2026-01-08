import { API_BASE_URL } from './config';
import type { HealthResponse } from './types';

export const healthApi = {
  check: async (): Promise<HealthResponse> => {
    const response = await fetch(`${API_BASE_URL}/actuator/health`);
    if (!response.ok) {
      throw new Error(`Health check failed: ${response.status}`);
    }
    return response.json();
  },
};

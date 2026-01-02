import { apiClient } from './client';
import { API_ENDPOINTS } from './config';
import type { Example } from './types';

export const exampleApi = {
  getAll: async (): Promise<Example[]> => {
    return apiClient.get<Example[]>(API_ENDPOINTS.examples);
  },
};

import { apiClient } from './client';
import { API_ENDPOINTS } from './config';
import type { Example } from './types';

interface CreateExampleRequest {
  name: string;
  description?: string;
}

export const exampleApi = {
  getAll: async (): Promise<Example[]> => {
    return apiClient.get<Example[]>(API_ENDPOINTS.examples);
  },

  create: async (request: CreateExampleRequest): Promise<Example> => {
    return apiClient.post<Example>(API_ENDPOINTS.examples, request);
  },
};

import { apiClient } from '../../../shared/api/client';
import { API_ENDPOINTS } from '../../../shared/api/config';
import type { Example } from '../../../shared/api/types';

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

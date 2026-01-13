import { describe, it, expect, vi, beforeEach } from 'vitest';
import { exampleApi } from '../features/example/api/exampleApi';
import type { Example } from '../shared/api/types';

describe('exampleApi', () => {
  const mockExample: Example = {
    id: 1,
    name: 'Test Example',
    description: 'Test description',
    active: true,
    createdAt: '2024-01-01T12:00:00',
    updatedAt: '2024-01-01T12:00:00',
  };

  beforeEach(() => {
    vi.resetAllMocks();
  });

  describe('getAll', () => {
    it('fetches all examples successfully', async () => {
      const mockExamples = [mockExample];

      vi.mocked(global.fetch).mockResolvedValueOnce({
        ok: true,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => mockExamples,
      } as Response);

      const result = await exampleApi.getAll();

      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/example'),
        expect.objectContaining({ method: 'GET' })
      );
      expect(result).toEqual(mockExamples);
    });

    it('throws error on failed request', async () => {
      vi.mocked(global.fetch).mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ error: 'SERVER_ERROR', message: 'Internal Server Error' }),
      } as Response);

      await expect(exampleApi.getAll()).rejects.toThrow('Internal Server Error');
    });
  });
});

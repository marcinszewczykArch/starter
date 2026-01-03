import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ApiStatus } from '../components/ApiStatus';

describe('ApiStatus', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('shows loading state initially', () => {
    vi.mocked(global.fetch).mockImplementation(() => new Promise(() => {}));

    render(<ApiStatus />);

    expect(screen.getByText('Checking API status...')).toBeInTheDocument();
  });

  it('shows healthy status when API is up', async () => {
    vi.mocked(global.fetch).mockResolvedValueOnce({
      ok: true,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({ status: 'UP' }),
    } as Response);

    render(<ApiStatus />);

    await waitFor(() => {
      expect(screen.getByText('Healthy')).toBeInTheDocument();
    });
  });

  it('shows unreachable status when API fails', async () => {
    vi.mocked(global.fetch).mockRejectedValueOnce(new Error('Network error'));

    render(<ApiStatus />);

    await waitFor(() => {
      expect(screen.getByText('Unreachable')).toBeInTheDocument();
    });
  });

  it('shows components when available', async () => {
    vi.mocked(global.fetch).mockResolvedValueOnce({
      ok: true,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({
        status: 'UP',
        components: {
          db: { status: 'UP' },
          diskSpace: { status: 'UP' },
        },
      }),
    } as Response);

    render(<ApiStatus />);

    await waitFor(() => {
      expect(screen.getByText('db')).toBeInTheDocument();
      expect(screen.getByText('diskSpace')).toBeInTheDocument();
    });
  });
});

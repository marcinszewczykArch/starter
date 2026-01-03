import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ExampleList } from '../components/ExampleList';
import type { Example } from '../api/types';

describe('ExampleList', () => {
  const mockExamples: Example[] = [
    {
      id: 1,
      name: 'Test Example 1',
      description: 'Description for example 1',
      active: true,
      createdAt: '2024-01-01T12:00:00',
      updatedAt: '2024-01-01T12:00:00',
    },
    {
      id: 2,
      name: 'Test Example 2',
      description: null,
      active: false,
      createdAt: '2024-01-02T12:00:00',
      updatedAt: '2024-01-02T12:00:00',
    },
  ];

  it('renders loading state', () => {
    render(<ExampleList examples={[]} loading={true} />);

    expect(screen.getByText('Loading examples...')).toBeInTheDocument();
  });

  it('renders empty state when no examples', () => {
    render(<ExampleList examples={[]} loading={false} />);

    expect(screen.getByText('No examples found')).toBeInTheDocument();
  });

  it('renders list of examples', () => {
    render(<ExampleList examples={mockExamples} loading={false} />);

    expect(screen.getByText('Test Example 1')).toBeInTheDocument();
    expect(screen.getByText('Test Example 2')).toBeInTheDocument();
    expect(screen.getByText('Description for example 1')).toBeInTheDocument();
  });

  it('shows active status for active examples', () => {
    render(<ExampleList examples={mockExamples} loading={false} />);

    expect(screen.getByText('Active')).toBeInTheDocument();
    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });

  it('displays example IDs', () => {
    render(<ExampleList examples={mockExamples} loading={false} />);

    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
  });
});

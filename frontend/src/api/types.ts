export interface Example {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface HealthResponse {
  status: 'UP' | 'DOWN';
  components?: Record<string, { status: string }>;
}

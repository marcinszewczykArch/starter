import { API_BASE_URL } from './config';

export interface MetricMeasurement {
  statistic: string;
  value: number;
}

export interface MetricResponse {
  name: string;
  description?: string;
  baseUnit?: string;
  measurements: MetricMeasurement[];
}

export interface SystemMetrics {
  jvmMemoryUsed: number;
  jvmMemoryMax: number;
  httpRequestsCount: number;
  httpRequestsTime: number;
  dbConnectionsActive: number;
  dbConnectionsIdle: number;
  dbConnectionsMax: number;
  appStartupTime: number;
  diskFree: number;
  diskTotal: number;
  threadsLive: number;
  cpuUsage: number;
}

async function fetchMetric(name: string): Promise<MetricResponse | null> {
  try {
    const response = await fetch(`${API_BASE_URL}/actuator/metrics/${name}`);
    if (!response.ok) return null;
    return response.json();
  } catch {
    return null;
  }
}

function getValue(metric: MetricResponse | null, statistic = 'VALUE'): number {
  if (!metric) return 0;
  const measurement = metric.measurements.find((m) => m.statistic === statistic);
  return measurement?.value ?? 0;
}

export const metricsApi = {
  getSystemMetrics: async (): Promise<SystemMetrics> => {
    const [
      jvmMemoryUsed,
      jvmMemoryMax,
      httpRequests,
      dbActive,
      dbIdle,
      dbMax,
      appStartup,
      diskFree,
      diskTotal,
      threads,
      cpu,
    ] = await Promise.all([
      fetchMetric('jvm.memory.used'),
      fetchMetric('jvm.memory.max'),
      fetchMetric('http.server.requests'),
      fetchMetric('hikaricp.connections.active'),
      fetchMetric('hikaricp.connections.idle'),
      fetchMetric('hikaricp.connections.max'),
      fetchMetric('application.started.time'),
      fetchMetric('disk.free'),
      fetchMetric('disk.total'),
      fetchMetric('jvm.threads.live'),
      fetchMetric('process.cpu.usage'),
    ]);

    return {
      jvmMemoryUsed: getValue(jvmMemoryUsed),
      jvmMemoryMax: getValue(jvmMemoryMax),
      httpRequestsCount: getValue(httpRequests, 'COUNT'),
      httpRequestsTime: getValue(httpRequests, 'TOTAL_TIME'),
      dbConnectionsActive: getValue(dbActive),
      dbConnectionsIdle: getValue(dbIdle),
      dbConnectionsMax: getValue(dbMax),
      appStartupTime: getValue(appStartup),
      diskFree: getValue(diskFree),
      diskTotal: getValue(diskTotal),
      threadsLive: getValue(threads),
      cpuUsage: getValue(cpu),
    };
  },
};

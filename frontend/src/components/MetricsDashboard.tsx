import { useState, useEffect } from 'react';
import { metricsApi, type SystemMetrics } from '../api/metricsApi';
import './MetricsDashboard.css';

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

function formatPercent(value: number): string {
  return (value * 100).toFixed(1) + '%';
}

function formatTime(seconds: number): string {
  if (seconds < 1) return (seconds * 1000).toFixed(0) + 'ms';
  return seconds.toFixed(2) + 's';
}

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: string;
  color?: string;
  progress?: number;
}

function MetricCard({ title, value, subtitle, icon, color = 'orange', progress }: MetricCardProps) {
  return (
    <div className={`metric-card metric-card--${color}`}>
      <div className="metric-icon">{icon}</div>
      <div className="metric-content">
        <div className="metric-title">{title}</div>
        <div className="metric-value">{value}</div>
        {subtitle && <div className="metric-subtitle">{subtitle}</div>}
        {progress !== undefined && (
          <div className="metric-progress">
            <div
              className="metric-progress-bar"
              style={{ width: `${Math.min(progress, 100)}%` }}
            />
          </div>
        )}
      </div>
    </div>
  );
}

export function MetricsDashboard() {
  const [metrics, setMetrics] = useState<SystemMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMetrics = async () => {
    try {
      setError(null);
      const data = await metricsApi.getSystemMetrics();
      setMetrics(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch metrics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 5000); // Refresh every 5s
    return () => clearInterval(interval);
  }, []);

  if (loading && !metrics) {
    return (
      <div className="metrics-dashboard metrics-dashboard--loading">
        <div className="loading-spinner" />
        <span>Loading metrics...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="metrics-dashboard metrics-dashboard--error">
        <span>⚠️ {error}</span>
      </div>
    );
  }

  if (!metrics) return null;

  const memoryPercent = metrics.jvmMemoryMax > 0 
    ? (metrics.jvmMemoryUsed / metrics.jvmMemoryMax) * 100 
    : 0;
  
  const diskPercent = metrics.diskTotal > 0 
    ? ((metrics.diskTotal - metrics.diskFree) / metrics.diskTotal) * 100 
    : 0;

  const dbUtilization = metrics.dbConnectionsMax > 0
    ? (metrics.dbConnectionsActive / metrics.dbConnectionsMax) * 100
    : 0;

  return (
    <div className="metrics-dashboard">
      <div className="metrics-header">
        <h2 className="metrics-title">📊 System Metrics</h2>
        <span className="metrics-refresh">Auto-refresh: 5s</span>
      </div>
      
      <div className="metrics-grid">
        <MetricCard
          icon="🧠"
          title="JVM Memory"
          value={formatBytes(metrics.jvmMemoryUsed)}
          subtitle={`of ${formatBytes(metrics.jvmMemoryMax)}`}
          color="blue"
          progress={memoryPercent}
        />
        
        <MetricCard
          icon="💾"
          title="Disk Usage"
          value={formatBytes(metrics.diskTotal - metrics.diskFree)}
          subtitle={`${formatBytes(metrics.diskFree)} free`}
          color="purple"
          progress={diskPercent}
        />
        
        <MetricCard
          icon="🔄"
          title="HTTP Requests"
          value={metrics.httpRequestsCount}
          subtitle={`Total time: ${formatTime(metrics.httpRequestsTime)}`}
          color="green"
        />
        
        <MetricCard
          icon="🗄️"
          title="DB Connections"
          value={`${metrics.dbConnectionsActive} active`}
          subtitle={`${metrics.dbConnectionsIdle} idle / ${metrics.dbConnectionsMax} max`}
          color="cyan"
          progress={dbUtilization}
        />
        
        <MetricCard
          icon="🧵"
          title="JVM Threads"
          value={metrics.threadsLive}
          subtitle="Live threads"
          color="yellow"
        />
        
        <MetricCard
          icon="⚡"
          title="CPU Usage"
          value={formatPercent(metrics.cpuUsage)}
          color="red"
          progress={metrics.cpuUsage * 100}
        />
        
        <MetricCard
          icon="🚀"
          title="Startup Time"
          value={formatTime(metrics.appStartupTime)}
          subtitle="Application boot"
          color="orange"
        />
      </div>
    </div>
  );
}


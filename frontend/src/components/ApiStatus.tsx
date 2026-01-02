import { useState, useEffect } from 'react';
import { healthApi } from '../api/healthApi';
import type { HealthResponse } from '../api/types';
import './ApiStatus.css';

export function ApiStatus() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const checkHealth = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await healthApi.check();
      setHealth(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check health');
      setHealth(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkHealth();

    // Poll health every 30 seconds
    const interval = setInterval(checkHealth, 30000);
    return () => clearInterval(interval);
  }, []);

  if (loading && !health) {
    return (
      <div className="api-status api-status--loading">
        <div className="status-indicator status-indicator--loading" />
        <span>Checking API status...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="api-status api-status--error">
        <div className="status-indicator status-indicator--down" />
        <div className="status-content">
          <span className="status-label">API Status</span>
          <span className="status-value">Unreachable</span>
        </div>
      </div>
    );
  }

  const isUp = health?.status === 'UP';

  return (
    <div className={`api-status ${isUp ? 'api-status--up' : 'api-status--down'}`}>
      <div className={`status-indicator ${isUp ? 'status-indicator--up' : 'status-indicator--down'}`} />
      <div className="status-content">
        <span className="status-label">API Status</span>
        <span className="status-value">{isUp ? 'Healthy' : 'Down'}</span>
      </div>
      {health?.components && (
        <div className="status-components">
          {Object.entries(health.components).map(([name, component]) => (
            <div key={name} className="component-item">
              <span className={`component-dot ${component.status === 'UP' ? 'up' : 'down'}`} />
              <span className="component-name">{name}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}


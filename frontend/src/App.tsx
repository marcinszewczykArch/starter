import { useState, useEffect } from 'react';
import { exampleApi } from './api/exampleApi';
import { healthApi } from './api/healthApi';
import { metricsApi, SystemMetrics } from './api/metricsApi';
import type { Example, HealthResponse } from './api/types';

function App() {
  const [examples, setExamples] = useState<Example[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [healthLoading, setHealthLoading] = useState(true);
  const [metrics, setMetrics] = useState<SystemMetrics | null>(null);

  const fetchExamples = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await exampleApi.getAll();
      setExamples(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch examples');
    } finally {
      setLoading(false);
    }
  };

  const fetchHealth = async () => {
    try {
      setHealthLoading(true);
      const data = await healthApi.check();
      setHealth(data);
    } catch {
      setHealth(null);
    } finally {
      setHealthLoading(false);
    }
  };

  const fetchMetrics = async () => {
    try {
      const data = await metricsApi.getSystemMetrics();
      setMetrics(data);
    } catch {
      setMetrics(null);
    }
  };

  useEffect(() => {
    fetchExamples();
    fetchHealth();
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <header className="border-b border-gray-200">
        <div className="max-w-5xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <span className="text-xl font-semibold text-gray-900">Starter</span>
            <span className="text-xs text-gray-400 border border-gray-200 rounded px-2 py-0.5">
              v0.0.1
            </span>
          </div>
          <nav className="flex gap-6">
            <a href="/swagger-ui/index.html" className="text-sm text-gray-600 hover:text-gray-900">
              API Docs
            </a>
            <a href="/actuator/health" className="text-sm text-gray-600 hover:text-gray-900">
              Health
            </a>
          </nav>
        </div>
      </header>

      {/* Main */}
      <main className="max-w-5xl mx-auto px-4 py-12">
        {/* Hero */}
        <div className="text-center mb-12">
          <h1 className="text-3xl font-semibold text-gray-900 mb-3">Welcome to Starter</h1>
          <p className="text-gray-500">
            A production-ready monorepo template with Spring Boot backend and React frontend.
          </p>
        </div>

        {/* Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-12">
          {/* API Status Card */}
          <div className="border border-gray-200 rounded-lg p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">API Status</h2>

            {healthLoading ? (
              <p className="text-gray-400">Checking...</p>
            ) : health?.status === 'UP' ? (
              <div>
                <div className="flex items-center gap-2 mb-4">
                  <span className="w-2 h-2 bg-green-500 rounded-full"></span>
                  <span className="text-green-600 font-medium">Healthy</span>
                </div>
                <div className="flex gap-4 text-sm text-gray-500">
                  {health.components?.db?.status === 'UP' && (
                    <span className="flex items-center gap-1">
                      <span className="w-1.5 h-1.5 bg-green-500 rounded-full"></span>
                      db
                    </span>
                  )}
                  {health.components?.diskSpace?.status === 'UP' && (
                    <span className="flex items-center gap-1">
                      <span className="w-1.5 h-1.5 bg-green-500 rounded-full"></span>
                      disk
                    </span>
                  )}
                  {health.components?.ping?.status === 'UP' && (
                    <span className="flex items-center gap-1">
                      <span className="w-1.5 h-1.5 bg-green-500 rounded-full"></span>
                      ping
                    </span>
                  )}
                </div>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 bg-red-500 rounded-full"></span>
                <span className="text-red-600">Unreachable</span>
              </div>
            )}
          </div>

          {/* Examples Card */}
          <div className="border border-gray-200 rounded-lg p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-medium text-gray-900">Examples</h2>
              <button
                onClick={fetchExamples}
                disabled={loading}
                className="text-sm text-gray-500 hover:text-gray-700 disabled:opacity-50"
              >
                ↻ Refresh
              </button>
            </div>

            {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

            {loading ? (
              <p className="text-gray-400">Loading...</p>
            ) : examples.length === 0 ? (
              <p className="text-gray-400">No examples found</p>
            ) : (
              <div className="space-y-3">
                {examples.map((example) => (
                  <div
                    key={example.id}
                    className="flex justify-between items-start py-3 border-b border-gray-100 last:border-0"
                  >
                    <div>
                      <p className="font-medium text-gray-900">{example.name}</p>
                      <p className="text-sm text-gray-500">{example.description}</p>
                    </div>
                    <span
                      className={`text-xs px-2 py-1 rounded ${
                        example.active ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500'
                      }`}
                    >
                      {example.active ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Metrics */}
        {metrics && (
          <div className="border border-gray-200 rounded-lg p-6 mb-12">
            <h2 className="text-lg font-medium text-gray-900 mb-6">System Metrics</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
              {/* Memory */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 uppercase">Memory</p>
                  <p className="text-sm font-medium text-gray-900">
                    {formatBytes(metrics.jvmMemoryUsed)}
                  </p>
                </div>
              </div>

              {/* CPU */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M13 10V3L4 14h7v7l9-11h-7z"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 uppercase">CPU</p>
                  <p className="text-sm font-medium text-gray-900">
                    {(metrics.cpuUsage * 100).toFixed(1)}%
                  </p>
                </div>
              </div>

              {/* Requests */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 uppercase">Requests</p>
                  <p className="text-sm font-medium text-gray-900">
                    {metrics.httpRequestsCount.toFixed(0)}
                  </p>
                </div>
              </div>

              {/* Threads */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M4 6h16M4 12h16M4 18h16"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 uppercase">Threads</p>
                  <p className="text-sm font-medium text-gray-900">
                    {metrics.threadsLive.toFixed(0)}
                  </p>
                </div>
              </div>

              {/* DB Connections */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 uppercase">DB Pool</p>
                  <p className="text-sm font-medium text-gray-900">
                    {metrics.dbConnectionsActive}/{metrics.dbConnectionsMax}
                  </p>
                </div>
              </div>

              {/* Disk */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 uppercase">Disk Free</p>
                  <p className="text-sm font-medium text-gray-900">
                    {formatBytes(metrics.diskFree)}
                  </p>
                </div>
              </div>

              {/* Uptime */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 uppercase">Startup</p>
                  <p className="text-sm font-medium text-gray-900">
                    {metrics.appStartupTime.toFixed(1)}s
                  </p>
                </div>
              </div>

              {/* Avg Response */}
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 uppercase">Avg Time</p>
                  <p className="text-sm font-medium text-gray-900">
                    {metrics.httpRequestsCount > 0
                      ? ((metrics.httpRequestsTime / metrics.httpRequestsCount) * 1000).toFixed(0)
                      : 0}
                    ms
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Tech Stack */}
        <div className="text-center">
          <h2 className="text-sm font-medium text-gray-400 uppercase tracking-wide mb-4">
            Built with
          </h2>
          <div className="flex flex-wrap justify-center gap-3">
            {['Java 21', 'Spring Boot', 'PostgreSQL', 'React', 'TypeScript', 'Vite'].map((tech) => (
              <span
                key={tech}
                className="text-sm text-gray-600 border border-gray-200 rounded-full px-3 py-1"
              >
                {tech}
              </span>
            ))}
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-gray-200 mt-12">
        <div className="max-w-5xl mx-auto px-4 py-6 text-center text-sm text-gray-400">
          Starter · Built with modern web technologies
        </div>
      </footer>
    </div>
  );
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`;
}

export default App;

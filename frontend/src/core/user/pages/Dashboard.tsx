import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { exampleApi } from '../../../features/example/api/exampleApi';
import { healthApi } from '../../../shared/api/healthApi';
import { metricsApi, SystemMetrics } from '../../../shared/api/metricsApi';
import type { Example, HealthResponse } from '../../../shared/api/types';
import { AdminUsersPanel } from '../../admin/components/AdminUsersPanel';
import { ChangePasswordModal } from '../../auth/components/ChangePasswordModal';
import { Header } from '../../common/components/Header';
import { MetricCard } from '../../common/components/MetricCard';
import { useAuth } from '../../auth/context/AuthContext';
import { formatBytes } from '../../../shared/utils/format';

export function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [examples, setExamples] = useState<Example[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [healthLoading, setHealthLoading] = useState(true);
  const [metrics, setMetrics] = useState<SystemMetrics | null>(null);

  // New example form
  const [newName, setNewName] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [creating, setCreating] = useState(false);

  // Change password modal
  const [showChangePassword, setShowChangePassword] = useState(false);

  const handlePasswordChanged = () => {
    setShowChangePassword(false);
    // Logout and redirect to login
    logout();
    navigate('/login', {
      state: { message: 'Password changed successfully. Please log in again.' },
    });
  };

  const fetchExamples = useCallback(async () => {
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
  }, []);

  const fetchHealth = useCallback(async () => {
    try {
      setHealthLoading(true);
      const data = await healthApi.check();
      setHealth(data);
    } catch {
      setHealth(null);
    } finally {
      setHealthLoading(false);
    }
  }, []);

  const fetchMetrics = useCallback(async () => {
    try {
      const data = await metricsApi.getSystemMetrics();
      setMetrics(data);
    } catch {
      setMetrics(null);
    }
  }, []);

  const handleCreateExample = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newName.trim()) return;

    setCreating(true);
    setError(null);
    try {
      await exampleApi.create({ name: newName, description: newDescription || undefined });
      setNewName('');
      setNewDescription('');
      fetchExamples();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create example');
    } finally {
      setCreating(false);
    }
  };

  useEffect(() => {
    fetchExamples();
    fetchHealth();
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 5000);
    return () => clearInterval(interval);
  }, [fetchExamples, fetchHealth, fetchMetrics]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-6xl mx-auto px-4 py-8">
        {/* Welcome Section */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900">
            Welcome back, {user?.email.split('@')[0]}!
          </h1>
          <p className="text-gray-600">Here's what's happening with your application.</p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          {/* API Status Card */}
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h2 className="text-sm font-medium text-gray-500 mb-3">API Status</h2>

            {healthLoading ? (
              <p className="text-gray-400">Checking...</p>
            ) : health?.status === 'UP' ? (
              <div>
                <div className="flex items-center gap-2 mb-3">
                  <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                  <span className="text-xl font-semibold text-green-600">Healthy</span>
                </div>
                <div className="flex gap-3 text-sm text-gray-500">
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
                <span className="w-3 h-3 bg-red-500 rounded-full"></span>
                <span className="text-xl font-semibold text-red-600">Unreachable</span>
              </div>
            )}
          </div>

          {/* Examples Count */}
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h2 className="text-sm font-medium text-gray-500 mb-3">Your Examples</h2>
            <p className="text-3xl font-bold text-gray-900">{examples.length}</p>
            <p className="text-sm text-gray-500 mt-1">
              {user?.role === 'ADMIN' ? 'Total in system' : 'Created by you'}
            </p>
          </div>

          {/* System Memory */}
          {metrics && (
            <div className="bg-white rounded-xl border border-gray-200 p-6">
              <h2 className="text-sm font-medium text-gray-500 mb-3">Memory Usage</h2>
              <p className="text-3xl font-bold text-gray-900">
                {formatBytes(metrics.jvmMemoryUsed)}
              </p>
              <p className="text-sm text-gray-500 mt-1">
                CPU: {(metrics.cpuUsage * 100).toFixed(1)}%
              </p>
            </div>
          )}
        </div>

        {/* Examples Section */}
        <div className="bg-white rounded-xl border border-gray-200 p-6 mb-8">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-lg font-semibold text-gray-900">Examples</h2>
            <button
              onClick={fetchExamples}
              disabled={loading}
              className="text-sm text-gray-500 hover:text-gray-700 disabled:opacity-50"
            >
              ↻ Refresh
            </button>
          </div>

          {/* Create Form */}
          <form onSubmit={handleCreateExample} className="mb-6">
            <div className="flex flex-col sm:flex-row gap-3">
              <input
                type="text"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                placeholder="Example name"
                className="flex-1 px-4 py-2 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              />
              <input
                type="text"
                value={newDescription}
                onChange={(e) => setNewDescription(e.target.value)}
                placeholder="Description (optional)"
                className="flex-1 px-4 py-2 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              />
              <button
                type="submit"
                disabled={creating || !newName.trim()}
                className="bg-indigo-600 text-white px-6 py-2 rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
              >
                {creating ? 'Adding...' : 'Add'}
              </button>
            </div>
          </form>

          {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

          {loading ? (
            <p className="text-gray-400">Loading...</p>
          ) : examples.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <p>No examples found</p>
              <p className="text-sm">Create your first example above!</p>
            </div>
          ) : (
            <div className="space-y-3">
              {examples.map((example) => (
                <div
                  key={example.id}
                  className="flex justify-between items-start py-3 border-b border-gray-100 last:border-0"
                >
                  <div>
                    <p className="font-medium text-gray-900">{example.name}</p>
                    <p className="text-sm text-gray-500">
                      {example.description || 'No description'}
                    </p>
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

        {/* Account Settings */}
        <div className="bg-white rounded-xl border border-gray-200 p-6 mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Account Settings</h2>
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <p className="text-sm text-gray-500">Email</p>
              <p className="font-medium text-gray-900">{user?.email}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Role</p>
              <p className="font-medium text-gray-900">{user?.role}</p>
            </div>
            <button
              onClick={() => setShowChangePassword(true)}
              className="inline-flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
                />
              </svg>
              Change Password
            </button>
          </div>
        </div>

        {/* Admin Users Panel - only visible for admins */}
        {user?.role === 'ADMIN' && (
          <div className="mb-8">
            <AdminUsersPanel />
          </div>
        )}

        {/* Metrics Grid */}
        {metrics && (
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">System Metrics</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
              <MetricCard icon="memory" label="Memory" value={formatBytes(metrics.jvmMemoryUsed)} />
              <MetricCard
                icon="cpu"
                label="CPU"
                value={`${(metrics.cpuUsage * 100).toFixed(1)}%`}
              />
              <MetricCard
                icon="requests"
                label="Requests"
                value={metrics.httpRequestsCount.toFixed(0)}
              />
              <MetricCard icon="threads" label="Threads" value={metrics.threadsLive.toFixed(0)} />
              <MetricCard
                icon="database"
                label="DB Pool"
                value={`${metrics.dbConnectionsActive}/${metrics.dbConnectionsMax}`}
              />
              <MetricCard icon="disk" label="Disk Free" value={formatBytes(metrics.diskFree)} />
              <MetricCard
                icon="clock"
                label="Startup"
                value={`${metrics.appStartupTime.toFixed(1)}s`}
              />
              <MetricCard
                icon="chart"
                label="Avg Time"
                value={`${
                  metrics.httpRequestsCount > 0
                    ? ((metrics.httpRequestsTime / metrics.httpRequestsCount) * 1000).toFixed(0)
                    : 0
                }ms`}
              />
            </div>
          </div>
        )}
      </main>

      {/* Change Password Modal */}
      <ChangePasswordModal
        isOpen={showChangePassword}
        onClose={() => setShowChangePassword(false)}
        onSuccess={handlePasswordChanged}
      />
    </div>
  );
}

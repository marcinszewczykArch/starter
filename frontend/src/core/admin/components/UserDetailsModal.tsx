import { useState, useEffect, useCallback } from 'react';
import { adminApi } from '../api/adminApi';
import type { AdminUser, LoginHistoryEntry, LoginHistoryPage } from '../../../shared/api/types';

interface UserDetailsModalProps {
  user: AdminUser;
  isOpen: boolean;
  onClose: () => void;
  onRoleChange: (userId: number, newRole: 'USER' | 'ADMIN') => Promise<void>;
  onDelete: (userId: number) => Promise<void>;
  currentUserId: number;
}

export function UserDetailsModal({
  user,
  isOpen,
  onClose,
  onRoleChange,
  onDelete,
  currentUserId,
}: UserDetailsModalProps) {
  const [loginHistory, setLoginHistory] = useState<LoginHistoryPage | null>(null);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchLoginHistory = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    try {
      const history = await adminApi.getLoginHistory(user.id, page, 10);
      setLoginHistory(history);
    } catch (error) {
      console.error('Failed to fetch login history:', error);
    } finally {
      setLoading(false);
    }
  }, [user, page]);

  useEffect(() => {
    if (isOpen && user) {
      setPage(0);
      fetchLoginHistory();
    }
  }, [isOpen, user, fetchLoginHistory]);

  useEffect(() => {
    if (isOpen) {
      fetchLoginHistory();
    }
  }, [page, isOpen, fetchLoginHistory]);

  const handleRoleChange = async () => {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    setActionLoading(true);
    try {
      await onRoleChange(user.id, newRole);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm(`Are you sure you want to delete ${user.email}?`)) return;
    setActionLoading(true);
    try {
      await onDelete(user.id);
      onClose();
    } finally {
      setActionLoading(false);
    }
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return 'Never';
    return new Date(dateStr).toLocaleString();
  };

  const formatRelativeTime = (dateStr: string | null) => {
    if (!dateStr) return 'Never';
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  const getLocationDisplay = (entry: LoginHistoryEntry) => {
    if (entry.city && entry.country) {
      return `${entry.city}, ${entry.country}`;
    }
    return 'Unknown';
  };

  const getCoordinatesDisplay = (entry: LoginHistoryEntry) => {
    if (entry.latitude != null && entry.longitude != null) {
      return `${entry.latitude.toFixed(4)}, ${entry.longitude.toFixed(4)}`;
    }
    return '-';
  };

  const isOwnAccount = user.id === currentUserId;
  const canChangeRole = !isOwnAccount;
  const canDelete = !isOwnAccount && user.role !== 'ADMIN';

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
          <h2 className="text-xl font-semibold text-gray-900">User Details</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {/* User Info */}
          <div className="mb-6">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center">
                <span className="text-indigo-600 font-semibold text-lg">
                  {user.email[0].toUpperCase()}
                </span>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">{user.email}</h3>
                <div className="flex items-center gap-2 text-sm text-gray-500">
                  <span
                    className={`px-2 py-0.5 rounded text-xs font-medium ${
                      user.role === 'ADMIN'
                        ? 'bg-purple-100 text-purple-700'
                        : 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    {user.role}
                  </span>
                  <span
                    className={`px-2 py-0.5 rounded text-xs ${
                      user.emailVerified
                        ? 'bg-green-100 text-green-700'
                        : 'bg-yellow-100 text-yellow-700'
                    }`}
                  >
                    {user.emailVerified ? '✓ Verified' : 'Unverified'}
                  </span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-gray-500">Created:</span>
                <span className="ml-2 text-gray-900">{formatDate(user.createdAt)}</span>
              </div>
              <div>
                <span className="text-gray-500">Last login:</span>
                <span className="ml-2 text-gray-900">{formatRelativeTime(user.lastLoginAt)}</span>
              </div>
            </div>
          </div>

          {/* Login History */}
          <div>
            <h4 className="font-medium text-gray-900 mb-3 flex items-center gap-2">
              📍 Login History
              {loginHistory && (
                <span className="text-sm font-normal text-gray-500">
                  ({loginHistory.totalElements} total)
                </span>
              )}
            </h4>

            {loading ? (
              <div className="text-center py-8 text-gray-500">Loading...</div>
            ) : loginHistory && loginHistory.content.length > 0 ? (
              <>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-gray-500 border-b">
                        <th className="pb-2 font-medium">Date</th>
                        <th className="pb-2 font-medium">Location</th>
                        <th className="pb-2 font-medium">Coordinates</th>
                        <th className="pb-2 font-medium">Source</th>
                        <th className="pb-2 font-medium">Device</th>
                        <th className="pb-2 font-medium">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {loginHistory.content.map((entry) => (
                        <tr key={entry.id} className={!entry.success ? 'bg-red-50' : ''}>
                          <td className="py-2 text-gray-900">
                            {formatRelativeTime(entry.loggedInAt)}
                          </td>
                          <td className="py-2 text-gray-600">{getLocationDisplay(entry)}</td>
                          <td className="py-2 text-gray-500 font-mono text-xs">
                            {getCoordinatesDisplay(entry)}
                          </td>
                          <td className="py-2">
                            {entry.locationSource === 'GPS' ? (
                              <span className="text-green-600">🛰️ GPS</span>
                            ) : entry.locationSource === 'IP' ? (
                              <span className="text-blue-600">🌐 IP</span>
                            ) : (
                              <span className="text-gray-400">-</span>
                            )}
                          </td>
                          <td className="py-2 text-gray-600">
                            {entry.browser && entry.os
                              ? `${entry.browser} / ${entry.os}`
                              : entry.deviceType || '-'}
                          </td>
                          <td className="py-2">
                            {entry.success ? (
                              <span className="text-green-600">✓</span>
                            ) : (
                              <span className="text-red-600" title={entry.failureReason || ''}>
                                ✗ {entry.failureReason}
                              </span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Pagination */}
                {loginHistory.totalPages > 1 && (
                  <div className="flex justify-center gap-2 mt-4">
                    <button
                      onClick={() => setPage((p) => Math.max(0, p - 1))}
                      disabled={page === 0}
                      className="px-3 py-1 text-sm border rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      ← Prev
                    </button>
                    <span className="px-3 py-1 text-sm text-gray-600">
                      Page {page + 1} of {loginHistory.totalPages}
                    </span>
                    <button
                      onClick={() => setPage((p) => Math.min(loginHistory.totalPages - 1, p + 1))}
                      disabled={page >= loginHistory.totalPages - 1}
                      className="px-3 py-1 text-sm border rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Next →
                    </button>
                  </div>
                )}
              </>
            ) : (
              <div className="text-center py-8 text-gray-500">No login history</div>
            )}
          </div>
        </div>

        {/* Footer Actions */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-between items-center bg-gray-50">
          <div className="text-sm text-gray-500">{isOwnAccount && 'This is your account'}</div>
          <div className="flex gap-2">
            {canChangeRole && (
              <button
                onClick={handleRoleChange}
                disabled={actionLoading}
                className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-100 disabled:opacity-50"
              >
                {user.role === 'ADMIN' ? 'Demote to USER' : 'Promote to ADMIN'}
              </button>
            )}
            {canDelete && (
              <button
                onClick={handleDelete}
                disabled={actionLoading}
                className="px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50"
              >
                Delete User
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

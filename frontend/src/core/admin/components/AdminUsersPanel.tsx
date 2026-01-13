import { useState, useEffect, useCallback } from 'react';
import { adminApi } from '../api/adminApi';
import type { AdminUser } from '../../../shared/api/types';
import { useAuth } from '../../auth/context/AuthContext';

export function AdminUsersPanel() {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await adminApi.getUsers();
      setUsers(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleChangeRole = async (userId: number, newRole: 'USER' | 'ADMIN') => {
    if (!confirm(`Are you sure you want to change this user's role to ${newRole}?`)) {
      return;
    }

    setActionLoading(userId);
    setError(null);
    try {
      await adminApi.changeRole(userId, newRole);
      fetchUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to change role');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeleteUser = async (userId: number, email: string) => {
    if (
      !confirm(`Are you sure you want to delete user "${email}"? This action cannot be undone.`)
    ) {
      return;
    }

    setActionLoading(userId);
    setError(null);
    try {
      await adminApi.deleteUser(userId);
      fetchUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete user');
    } finally {
      setActionLoading(null);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pl-PL', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">👥 User Management</h2>
        <p className="text-gray-400">Loading users...</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-lg font-semibold text-gray-900">👥 User Management</h2>
        <button
          onClick={fetchUsers}
          disabled={loading}
          className="text-sm text-gray-500 hover:text-gray-700 disabled:opacity-50"
        >
          ↻ Refresh
        </button>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
          {error}
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="text-left text-sm text-gray-500 border-b border-gray-200">
              <th className="pb-3 font-medium">Email</th>
              <th className="pb-3 font-medium">Role</th>
              <th className="pb-3 font-medium">Verified</th>
              <th className="pb-3 font-medium">Created</th>
              <th className="pb-3 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {users.map((user) => {
              const isCurrentUser = user.id === currentUser?.id;
              const isAdmin = user.role === 'ADMIN';
              const isLoading = actionLoading === user.id;

              return (
                <tr key={user.id} className={isCurrentUser ? 'bg-indigo-50/50' : ''}>
                  <td className="py-3">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-gray-900">{user.email}</span>
                      {isCurrentUser && (
                        <span className="text-xs bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded">
                          You
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="py-3">
                    <span
                      className={`text-xs px-2 py-1 rounded font-medium ${
                        isAdmin ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-600'
                      }`}
                    >
                      {user.role}
                    </span>
                  </td>
                  <td className="py-3">
                    {user.emailVerified ? (
                      <span className="text-green-600">✓</span>
                    ) : (
                      <span className="text-gray-400">✗</span>
                    )}
                  </td>
                  <td className="py-3 text-sm text-gray-500">{formatDate(user.createdAt)}</td>
                  <td className="py-3 text-right">
                    <div className="flex items-center justify-end gap-2">
                      {/* Role toggle - disabled for current user */}
                      {!isCurrentUser && (
                        <button
                          onClick={() => handleChangeRole(user.id, isAdmin ? 'USER' : 'ADMIN')}
                          disabled={isLoading}
                          className={`text-xs px-3 py-1.5 rounded font-medium transition-colors disabled:opacity-50 ${
                            isAdmin
                              ? 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                              : 'bg-purple-100 text-purple-700 hover:bg-purple-200'
                          }`}
                          title={isAdmin ? 'Demote to User' : 'Promote to Admin'}
                        >
                          {isLoading ? '...' : isAdmin ? '↓ Demote' : '↑ Promote'}
                        </button>
                      )}

                      {/* Delete - only for non-admin users, not current user */}
                      {!isCurrentUser && !isAdmin && (
                        <button
                          onClick={() => handleDeleteUser(user.id, user.email)}
                          disabled={isLoading}
                          className="text-xs px-3 py-1.5 rounded font-medium bg-red-100 text-red-700 hover:bg-red-200 transition-colors disabled:opacity-50"
                          title="Delete user"
                        >
                          {isLoading ? '...' : '🗑 Delete'}
                        </button>
                      )}

                      {/* Info for admins - cannot delete */}
                      {!isCurrentUser && isAdmin && (
                        <span
                          className="text-xs text-gray-400"
                          title="Admin accounts cannot be deleted through UI"
                        >
                          Protected
                        </span>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <div className="mt-4 text-sm text-gray-500">
        Total: {users.length} users ({users.filter((u) => u.role === 'ADMIN').length} admins)
      </div>
    </div>
  );
}

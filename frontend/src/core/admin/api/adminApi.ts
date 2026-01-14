import { apiClient } from '../../../shared/api/client';
import { AdminUser, ChangeRoleRequest, LoginHistoryPage } from '../../../shared/api/types';

const ADMIN_ENDPOINT = '/api/admin';

export const adminApi = {
  /**
   * Get all users (admin only)
   */
  getUsers: async (): Promise<AdminUser[]> => {
    return apiClient.get<AdminUser[]>(`${ADMIN_ENDPOINT}/users`);
  },

  /**
   * Change user role (admin only)
   */
  changeRole: async (userId: number, role: 'USER' | 'ADMIN'): Promise<AdminUser> => {
    const request: ChangeRoleRequest = { role };
    return apiClient.patch<AdminUser>(`${ADMIN_ENDPOINT}/users/${userId}/role`, request);
  },

  /**
   * Delete a user (admin only, cannot delete admins)
   * Returns void (204 No Content)
   */
  deleteUser: async (userId: number): Promise<void> => {
    await apiClient.delete<void>(`${ADMIN_ENDPOINT}/users/${userId}`);
  },

  /**
   * Get login history for a user (admin only)
   */
  getLoginHistory: async (userId: number, page = 0, size = 20): Promise<LoginHistoryPage> => {
    return apiClient.get<LoginHistoryPage>(`${ADMIN_ENDPOINT}/users/${userId}/logins`, {
      params: { page: String(page), size: String(size) },
    });
  },
};

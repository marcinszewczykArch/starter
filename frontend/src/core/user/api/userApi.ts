import { apiClient } from '../../../shared/api/client';
import type {
  UserProfile,
  UpdateProfileRequest,
  ChangeEmailRequest,
  DeleteAccountRequest,
  MessageResponse,
} from '../../../shared/api/types';

export const userApi = {
  /**
   * Get current user's profile
   */
  getProfile: () => apiClient.get<UserProfile>('/api/users/me/profile'),

  /**
   * Update current user's profile
   */
  updateProfile: (data: UpdateProfileRequest) =>
    apiClient.put<UserProfile>('/api/users/me/profile', data),

  /**
   * Upload avatar image
   * @param blob - Image blob (JPEG, already processed)
   */
  uploadAvatar: async (blob: Blob): Promise<MessageResponse> => {
    const formData = new FormData();
    formData.append('file', blob, 'avatar.jpg');

    const token = localStorage.getItem('auth_token');
    const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
    const response = await fetch(`${baseUrl}/api/users/me/avatar`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Failed to upload avatar');
    }

    return response.json();
  },

  /**
   * Delete current user's avatar
   */
  deleteAvatar: () => apiClient.delete<MessageResponse>('/api/users/me/avatar'),

  /**
   * Get avatar URL for a user
   */
  getAvatarUrl: (userId: number) => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
    return `${baseUrl}/api/users/${userId}/avatar`;
  },

  /**
   * Request email change (sends verification to new email)
   */
  changeEmail: (data: ChangeEmailRequest) =>
    apiClient.post<MessageResponse>('/api/users/me/change-email', data),

  /**
   * Delete (archive) current user's account
   */
  deleteAccount: (data: DeleteAccountRequest) =>
    apiClient.delete<MessageResponse>('/api/users/me', {
      body: JSON.stringify(data),
      headers: { 'Content-Type': 'application/json' },
    }),
};

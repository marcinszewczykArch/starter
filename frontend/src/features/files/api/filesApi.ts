import { apiClient } from '../../../shared/api/client';

export interface UserFile {
  id: number;
  filename: string;
  sizeBytes: number;
  contentType: string;
  createdAt: string;
}

export interface FileStats {
  fileCount: number;
  totalSizeBytes: number;
}

export interface StorageUsage {
  usedBytes: number;
  maxBytes: number;
  percentage: number;
}

export interface FileDownloadResponse {
  downloadUrl: string;
}

export interface FilePage {
  content: UserFile[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export const filesApi = {
  uploadFile: async (file: File): Promise<UserFile> => {
    const formData = new FormData();
    formData.append('file', file);

    const token = localStorage.getItem('auth_token');
    const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
    const response = await fetch(`${baseUrl}/api/files`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Failed to upload file');
    }

    return response.json() as Promise<UserFile>;
  },

  getFiles: async (
    page: number = 0,
    size: number = 20,
    contentType?: string,
    search?: string
  ): Promise<FilePage> => {
    const params: Record<string, string> = {
      page: page.toString(),
      size: size.toString(),
    };
    if (contentType) params.contentType = contentType;
    if (search) params.search = search;

    return apiClient.get<FilePage>('/api/files', { params });
  },

  getFileStats: (): Promise<FileStats> => {
    return apiClient.get<FileStats>('/api/files/stats');
  },

  getStorageUsage: (): Promise<StorageUsage> => {
    return apiClient.get<StorageUsage>('/api/files/storage/usage');
  },

  getDownloadUrl: (fileId: number): Promise<FileDownloadResponse> => {
    return apiClient.get<FileDownloadResponse>(`/api/files/${fileId}/download`);
  },

  deleteFile: (fileId: number): Promise<void> => {
    return apiClient.delete(`/api/files/${fileId}`);
  },
};

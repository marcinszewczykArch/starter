import { useState } from 'react';
import { filesApi, UserFile, FilePage } from '../api/filesApi';

interface FileListProps {
  files: FilePage;
  onDeleted: () => void;
  onPageChange: (page: number) => void;
}

export function FileList({ files, onDeleted, onPageChange }: FileListProps) {
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  };

  const handleDelete = async (fileId: number) => {
    if (!confirm('Are you sure you want to delete this file?')) return;

    try {
      setDeletingId(fileId);
      await filesApi.deleteFile(fileId);
      onDeleted();
    } catch (err) {
      alert('Failed to delete file');
    } finally {
      setDeletingId(null);
    }
  };

  const handleDownload = async (file: UserFile) => {
    try {
      const response = await filesApi.getDownloadUrl(file.id);
      window.open(response.downloadUrl, '_blank');
    } catch (err) {
      alert('Failed to get download URL');
    }
  };

  if (files.content.length === 0) {
    return (
      <div className="text-center py-12 text-gray-500">
        <p>No files uploaded yet.</p>
        <p className="text-sm mt-2">Upload your first file to get started!</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Files List */}
      <div className="space-y-2">
        {files.content.map((file) => (
          <div
            key={file.id}
            className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className="flex-1 min-w-0">
              <p className="font-medium text-gray-900 truncate">{file.filename}</p>
              <div className="flex items-center gap-4 mt-1 text-sm text-gray-500">
                <span>{formatFileSize(file.sizeBytes)}</span>
                <span>•</span>
                <span>{file.contentType}</span>
                <span>•</span>
                <span>{new Date(file.createdAt).toLocaleDateString()}</span>
              </div>
            </div>
            <div className="flex gap-2 ml-4">
              <button
                onClick={() => handleDownload(file)}
                className="px-3 py-1.5 text-sm text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
              >
                Download
              </button>
              <button
                onClick={() => handleDelete(file.id)}
                disabled={deletingId === file.id}
                className="px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
              >
                {deletingId === file.id ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      {files.totalPages > 1 && (
        <div className="flex items-center justify-between pt-4 border-t border-gray-200">
          <p className="text-sm text-gray-600">
            Showing {files.page * files.size + 1} to{' '}
            {Math.min((files.page + 1) * files.size, files.totalElements)} of {files.totalElements}{' '}
            files
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => onPageChange(files.page - 1)}
              disabled={files.page === 0}
              className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Previous
            </button>
            <span className="px-3 py-1.5 text-sm text-gray-700">
              Page {files.page + 1} of {files.totalPages}
            </span>
            <button
              onClick={() => onPageChange(files.page + 1)}
              disabled={files.page >= files.totalPages - 1}
              className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

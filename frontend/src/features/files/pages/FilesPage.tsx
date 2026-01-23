import { useState, useEffect, useCallback, useRef } from 'react';
import { Header } from '../../../core/common/components/Header';
import { filesApi, FilePage, StorageUsage, FileStats } from '../api/filesApi';
import { FileList } from '../components/FileList';
import { FileFilters } from '../components/FileFilters';

export function FilesPage() {
  const [files, setFiles] = useState<FilePage | null>(null);
  const [storageUsage, setStorageUsage] = useState<StorageUsage | null>(null);
  const [fileStats, setFileStats] = useState<FileStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [contentType, setContentType] = useState<string | null>(null);
  const [search, setSearch] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchFiles = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await filesApi.getFiles(page, 20, contentType || undefined, search || undefined);
      setFiles(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch files');
    } finally {
      setLoading(false);
    }
  }, [page, contentType, search]);

  const fetchStorageUsage = useCallback(async () => {
    try {
      const data = await filesApi.getStorageUsage();
      setStorageUsage(data);
    } catch (err) {
      // Silently fail - storage usage is not critical
      console.error('Failed to fetch storage usage', err);
    }
  }, []);

  const fetchFileStats = useCallback(async () => {
    try {
      const data = await filesApi.getFileStats();
      setFileStats(data);
    } catch (err) {
      // Silently fail - stats are not critical
      console.error('Failed to fetch file stats', err);
    }
  }, []);

  useEffect(() => {
    fetchFiles();
  }, [fetchFiles]);

  useEffect(() => {
    fetchStorageUsage();
    fetchFileStats();
  }, [fetchStorageUsage, fetchFileStats]);

  const handleUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    try {
      setUploading(true);
      setError(null);
      await filesApi.uploadFile(file);
      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      // Refresh files and stats
      await Promise.all([fetchFiles(), fetchStorageUsage(), fetchFileStats()]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to upload file');
    } finally {
      setUploading(false);
    }
  };

  const handleFileDeleted = () => {
    fetchFiles();
    fetchStorageUsage();
    fetchFileStats();
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  const formatBytes = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">My Files</h1>
          <p className="mt-2 text-gray-600">Upload and manage your files</p>
        </div>

        {/* Storage Usage */}
        {storageUsage && (
          <div className="mb-6 bg-white rounded-xl border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-gray-700">Storage Usage</span>
              <span className="text-sm text-gray-600">
                {formatBytes(storageUsage.usedBytes)} / {formatBytes(storageUsage.maxBytes)}
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-3">
              <div
                className={`h-3 rounded-full transition-all ${
                  storageUsage.percentage >= 90
                    ? 'bg-red-500'
                    : storageUsage.percentage >= 70
                    ? 'bg-yellow-500'
                    : 'bg-indigo-500'
                }`}
                style={{ width: `${Math.min(storageUsage.percentage, 100)}%` }}
              />
            </div>
            <p className="mt-2 text-xs text-gray-500">
              {storageUsage.percentage.toFixed(1)}% used
            </p>
          </div>
        )}

        {/* File Stats */}
        {fileStats && (
          <div className="mb-6 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-white rounded-xl border border-gray-200 p-4">
              <p className="text-sm text-gray-600">Total Files</p>
              <p className="text-2xl font-bold text-gray-900">{fileStats.fileCount}</p>
            </div>
            <div className="bg-white rounded-xl border border-gray-200 p-4">
              <p className="text-sm text-gray-600">Total Size</p>
              <p className="text-2xl font-bold text-gray-900">{formatBytes(fileStats.totalSizeBytes)}</p>
            </div>
          </div>
        )}

        {/* Upload Section */}
        <div className="mb-6 bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Upload File</h2>
          <div className="flex items-center gap-4">
            <input
              ref={fileInputRef}
              type="file"
              onChange={handleUpload}
              disabled={uploading}
              className="flex-1 text-sm text-gray-600 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 disabled:opacity-50"
            />
            {uploading && (
              <span className="text-sm text-gray-500">Uploading...</span>
            )}
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-sm text-red-800">{error}</p>
          </div>
        )}

        {/* Filters */}
        <div className="mb-6">
          <FileFilters
            contentType={contentType}
            search={search}
            onContentTypeChange={setContentType}
            onSearchChange={(value) => {
              setSearch(value);
              setPage(0); // Reset to first page when search changes
            }}
          />
        </div>

        {/* Files List */}
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          {loading ? (
            <div className="text-center py-12 text-gray-500">
              <p>Loading files...</p>
            </div>
          ) : files ? (
            <FileList files={files} onDeleted={handleFileDeleted} onPageChange={handlePageChange} />
          ) : (
            <div className="text-center py-12 text-gray-500">
              <p>Failed to load files</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

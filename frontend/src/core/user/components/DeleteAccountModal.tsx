import { useState, useEffect } from 'react';

interface DeleteAccountModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (password: string) => Promise<void>;
  email: string;
}

interface FileStats {
  fileCount: number;
  totalSizeBytes: number;
}

export function DeleteAccountModal({ isOpen, onClose, onConfirm, email }: DeleteAccountModalProps) {
  const [password, setPassword] = useState('');
  const [confirmText, setConfirmText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fileStats, setFileStats] = useState<FileStats | null>(null);

  // Fetch file statistics when modal opens
  useEffect(() => {
    if (isOpen) {
      fetchFileStats();
    }
  }, [isOpen]);

  const fetchFileStats = async () => {
    try {
      // Try to fetch file stats - if files feature is not implemented, this will fail gracefully
      const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
      const response = await fetch(`${baseUrl}/api/files/stats`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('auth_token')}`,
        },
      });
      if (response.ok) {
        const stats = await response.json();
        setFileStats(stats);
      } else {
        // Files feature not implemented or no files - set to null
        setFileStats(null);
      }
    } catch (err) {
      // Files feature not implemented - set to null
      setFileStats(null);
    }
  };

  if (!isOpen) return null;

  const canDelete = password.length > 0 && confirmText === 'delete my account';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canDelete) return;

    try {
      setLoading(true);
      setError(null);
      await onConfirm(password);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete account');
      setLoading(false);
    }
  };

  const handleClose = () => {
    setPassword('');
    setConfirmText('');
    setError(null);
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl max-w-md w-full overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 bg-red-50">
          <h2 className="text-xl font-semibold text-red-700">Delete Account</h2>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-800 font-semibold mb-2">
              ⚠️ Warning: This action cannot be undone!
            </p>
            <ul className="text-sm text-red-700 space-y-1 list-disc list-inside">
              <li>Your account will be permanently deactivated</li>
              <li>You won't be able to log in anymore</li>
              {fileStats && fileStats.fileCount > 0 && (
                <>
                  <li className="font-semibold">
                    All your files ({fileStats.fileCount} file{fileStats.fileCount !== 1 ? 's' : ''}
                    , {formatFileSize(fileStats.totalSizeBytes)}) will be permanently deleted
                  </li>
                </>
              )}
            </ul>
          </div>

          <p className="text-sm text-gray-600">
            You are about to delete the account for <strong>{email}</strong>.
            {fileStats && fileStats.fileCount > 0 && (
              <span className="block mt-2 text-red-600 font-medium">
                This will also delete all {fileStats.fileCount} of your uploaded files permanently.
              </span>
            )}
          </p>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
              Enter your password
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              placeholder="Your current password"
              className="w-full px-4 py-2 rounded-lg border border-gray-300 focus:ring-2 focus:ring-red-500 focus:border-red-500 disabled:bg-gray-50"
            />
          </div>

          <div>
            <label htmlFor="confirmText" className="block text-sm font-medium text-gray-700 mb-1">
              Type <span className="font-mono bg-gray-100 px-1 rounded">delete my account</span> to
              confirm
            </label>
            <input
              type="text"
              id="confirmText"
              value={confirmText}
              onChange={(e) => setConfirmText(e.target.value)}
              disabled={loading}
              placeholder="delete my account"
              className="w-full px-4 py-2 rounded-lg border border-gray-300 focus:ring-2 focus:ring-red-500 focus:border-red-500 disabled:bg-gray-50 font-mono"
            />
          </div>

          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-sm text-red-600">{error}</p>
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={handleClose}
              disabled={loading}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!canDelete || loading}
              className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Deleting...' : 'Delete Account'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

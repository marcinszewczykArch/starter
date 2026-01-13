import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api/authApi';

export function VerificationPendingPage() {
  const { user, logout } = useAuth();
  const [isResending, setIsResending] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleResendEmail = async () => {
    if (!user?.email) return;

    setIsResending(true);
    setMessage(null);
    setError(null);

    try {
      await authApi.resendVerification(user.email);
      setMessage('Verification email sent! Please check your inbox.');
    } catch (err) {
      setError('Failed to resend verification email. Please try again later.');
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white/10 backdrop-blur-lg rounded-2xl p-8 text-center">
        <div className="w-16 h-16 bg-yellow-500/20 rounded-full flex items-center justify-center mx-auto mb-6">
          <svg
            className="w-8 h-8 text-yellow-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
            />
          </svg>
        </div>

        <h1 className="text-2xl font-bold text-white mb-2">Verify Your Email</h1>

        <p className="text-gray-300 mb-6">
          We've sent a verification email to{' '}
          <span className="font-semibold text-white">{user?.email}</span>. Please check your inbox
          and click the verification link to continue.
        </p>

        <div className="bg-white/5 rounded-lg p-4 mb-6">
          <p className="text-sm text-gray-400">
            📧 Check your spam folder if you don't see the email
          </p>
        </div>

        {message && (
          <div className="bg-green-500/20 border border-green-500/50 text-green-300 px-4 py-3 rounded-lg mb-4">
            {message}
          </div>
        )}

        {error && (
          <div className="bg-red-500/20 border border-red-500/50 text-red-300 px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        <div className="space-y-3">
          <button
            onClick={handleResendEmail}
            disabled={isResending}
            className="w-full py-3 px-4 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-600/50 text-white font-medium rounded-lg transition-colors"
          >
            {isResending ? 'Sending...' : 'Resend Verification Email'}
          </button>

          <button
            onClick={logout}
            className="w-full py-3 px-4 bg-white/10 hover:bg-white/20 text-white font-medium rounded-lg transition-colors"
          >
            Sign Out
          </button>
        </div>

        <p className="text-xs text-gray-500 mt-6">
          Already verified?{' '}
          <button
            onClick={() => window.location.reload()}
            className="text-indigo-400 hover:text-indigo-300"
          >
            Refresh page
          </button>
        </p>
      </div>
    </div>
  );
}

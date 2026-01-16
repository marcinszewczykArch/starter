import { useState } from 'react';
import { ChangePasswordModal } from '../../auth/components/ChangePasswordModal';
import { DeleteAccountModal } from './DeleteAccountModal';

interface AccountSettingsProps {
  email: string;
  onChangeEmail?: (newEmail: string, password: string) => Promise<void>;
  onDeleteAccount: (password: string) => Promise<void>;
  onPasswordChanged: () => void;
}

export function AccountSettings({
  email,
  onChangeEmail,
  onDeleteAccount,
  onPasswordChanged,
}: AccountSettingsProps) {
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [showChangeEmail, setShowChangeEmail] = useState(false);
  const [showDeleteAccount, setShowDeleteAccount] = useState(false);

  // Change email form state
  const [newEmail, setNewEmail] = useState('');
  const [emailPassword, setEmailPassword] = useState('');
  const [emailChanging, setEmailChanging] = useState(false);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [emailSuccess, setEmailSuccess] = useState(false);

  const handleChangeEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!onChangeEmail || !newEmail || !emailPassword) return;

    try {
      setEmailChanging(true);
      setEmailError(null);
      await onChangeEmail(newEmail, emailPassword);
      setEmailSuccess(true);
      setShowChangeEmail(false);
      setNewEmail('');
      setEmailPassword('');
    } catch (err) {
      setEmailError(err instanceof Error ? err.message : 'Failed to change email');
    } finally {
      setEmailChanging(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Email Section */}
      <div className="pb-6 border-b border-gray-200">
        <h3 className="text-sm font-medium text-gray-900 mb-1">Email address</h3>
        <p className="text-sm text-gray-600 mb-3">{email}</p>

        {emailSuccess && (
          <div className="p-3 bg-green-50 border border-green-200 rounded-lg mb-3">
            <p className="text-sm text-green-600">
              Verification email sent! Check your inbox for the new email address.
            </p>
          </div>
        )}

        {showChangeEmail ? (
          <form onSubmit={handleChangeEmail} className="space-y-3">
            <div>
              <input
                type="email"
                value={newEmail}
                onChange={(e) => setNewEmail(e.target.value)}
                placeholder="New email address"
                disabled={emailChanging}
                className="w-full px-4 py-2 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50"
              />
            </div>
            <div>
              <input
                type="password"
                value={emailPassword}
                onChange={(e) => setEmailPassword(e.target.value)}
                placeholder="Your password"
                disabled={emailChanging}
                className="w-full px-4 py-2 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50"
              />
            </div>
            {emailError && <p className="text-sm text-red-600">{emailError}</p>}
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => {
                  setShowChangeEmail(false);
                  setNewEmail('');
                  setEmailPassword('');
                  setEmailError(null);
                }}
                disabled={emailChanging}
                className="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-50 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={emailChanging || !newEmail || !emailPassword}
                className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 disabled:opacity-50"
              >
                {emailChanging ? 'Sending...' : 'Send verification'}
              </button>
            </div>
          </form>
        ) : (
          onChangeEmail && (
            <button
              onClick={() => setShowChangeEmail(true)}
              className="text-sm text-indigo-600 hover:text-indigo-700 font-medium"
            >
              Change email
            </button>
          )
        )}
      </div>

      {/* Password Section */}
      <div className="pb-6 border-b border-gray-200">
        <h3 className="text-sm font-medium text-gray-900 mb-1">Password</h3>
        <p className="text-sm text-gray-600 mb-3">••••••••••••</p>
        <button
          onClick={() => setShowChangePassword(true)}
          className="text-sm text-indigo-600 hover:text-indigo-700 font-medium"
        >
          Change password
        </button>
      </div>

      {/* Danger Zone */}
      <div className="p-6 border border-red-200 rounded-xl bg-red-50/50">
        <h3 className="text-sm font-medium text-red-800 mb-1">Danger zone</h3>
        <p className="text-sm text-red-600 mb-4">
          Once you delete your account, there is no going back. Please be certain.
        </p>
        <button
          onClick={() => setShowDeleteAccount(true)}
          className="px-4 py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 font-medium"
        >
          Delete account
        </button>
      </div>

      {/* Modals */}
      <ChangePasswordModal
        isOpen={showChangePassword}
        onClose={() => setShowChangePassword(false)}
        onSuccess={onPasswordChanged}
      />

      <DeleteAccountModal
        isOpen={showDeleteAccount}
        onClose={() => setShowDeleteAccount(false)}
        onConfirm={onDeleteAccount}
        email={email}
      />
    </div>
  );
}

import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/context/AuthContext';
import { Header } from '../../common/components/Header';
import { AvatarUpload } from '../components/AvatarUpload';
import { ProfileForm } from '../components/ProfileForm';
import { AccountSettings } from '../components/AccountSettings';
import { userApi } from '../api/userApi';
import type { UserProfile, UpdateProfileRequest } from '../../../shared/api/types';

type SettingsTab = 'profile' | 'account';

export function SettingsPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<SettingsTab>('profile');
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await userApi.getProfile();
      setProfile(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const handleUpdateProfile = async (data: UpdateProfileRequest) => {
    const updated = await userApi.updateProfile(data);
    setProfile(updated);
  };

  const handleUploadAvatar = async (blob: Blob) => {
    await userApi.uploadAvatar(blob);
    // Refresh profile to get new avatar URL
    await fetchProfile();
  };

  const handleDeleteAvatar = async () => {
    await userApi.deleteAvatar();
    // Refresh profile to get updated avatar URL
    await fetchProfile();
  };

  const handleChangeEmail = async (newEmail: string, password: string) => {
    await userApi.changeEmail({ newEmail, password });
  };

  const handleDeleteAccount = async (password: string) => {
    await userApi.deleteAccount({ password });
    logout();
    navigate('/', { state: { message: 'Your account has been deleted.' } });
  };

  const handlePasswordChanged = () => {
    logout();
    navigate('/login', { state: { message: 'Password changed. Please log in again.' } });
  };

  const tabs: { id: SettingsTab; label: string; icon: React.ReactNode }[] = [
    {
      id: 'profile',
      label: 'Profile',
      icon: (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
          />
        </svg>
      ),
    },
    {
      id: 'account',
      label: 'Account',
      icon: (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
          />
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
          />
        </svg>
      ),
    },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-4xl mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <div className="mb-6">
          <Link
            to="/dashboard"
            className="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
            Back to Dashboard
          </Link>
        </div>

        {/* Page Title */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
          <p className="text-gray-600">Manage your account settings and profile</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-800">{error}</p>
          </div>
        )}

        <div className="flex flex-col md:flex-row gap-8">
          {/* Sidebar */}
          <nav className="w-full md:w-48 shrink-0">
            <ul className="flex md:flex-col gap-1">
              {tabs.map((tab) => (
                <li key={tab.id}>
                  <button
                    onClick={() => setActiveTab(tab.id)}
                    className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg text-left text-sm font-medium transition-colors ${
                      activeTab === tab.id
                        ? 'bg-indigo-50 text-indigo-700'
                        : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                    }`}
                  >
                    {tab.icon}
                    {tab.label}
                  </button>
                </li>
              ))}
            </ul>
          </nav>

          {/* Content */}
          <div className="flex-1 min-w-0">
            <div className="bg-white rounded-xl border border-gray-200 p-6 md:p-8">
              {loading ? (
                <div className="py-12 text-center text-gray-500">Loading...</div>
              ) : activeTab === 'profile' ? (
                <div className="space-y-8">
                  {/* Avatar Section */}
                  <div className="pb-8 border-b border-gray-200">
                    <h2 className="text-lg font-semibold text-gray-900 mb-6">Profile picture</h2>
                    <AvatarUpload
                      currentAvatarUrl={profile?.avatarUrl || null}
                      userInitial={user?.email.charAt(0).toUpperCase() || '?'}
                      onUpload={handleUploadAvatar}
                      onDelete={handleDeleteAvatar}
                    />
                  </div>

                  {/* Profile Form */}
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900 mb-6">
                      Profile information
                    </h2>
                    <ProfileForm profile={profile} onSave={handleUpdateProfile} />
                  </div>
                </div>
              ) : (
                <div>
                  <h2 className="text-lg font-semibold text-gray-900 mb-6">Account settings</h2>
                  <AccountSettings
                    email={user?.email || ''}
                    onChangeEmail={handleChangeEmail}
                    onDeleteAccount={handleDeleteAccount}
                    onPasswordChanged={handlePasswordChanged}
                  />
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

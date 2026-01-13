import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AdminOnly } from './AdminOnly';
import { LogoBrand } from './LogoBrand';

export function Header() {
  const { user, token, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
    setMenuOpen(false);
  };

  const handleCopyToken = async () => {
    if (token) {
      await navigator.clipboard.writeText(token);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="max-w-6xl mx-auto px-4 py-4">
        <div className="flex justify-between items-center">
          {/* Logo */}
          <LogoBrand to={isAuthenticated ? '/dashboard' : '/'} />

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center gap-6">
            {isAuthenticated ? (
              <>
                <AdminOnly>
                  <button
                    onClick={handleCopyToken}
                    className="text-sm text-gray-600 hover:text-gray-900 flex items-center gap-1"
                    title="Copy JWT token for Swagger"
                  >
                    {copied ? (
                      <>
                        <svg
                          className="w-4 h-4 text-green-600"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M5 13l4 4L19 7"
                          />
                        </svg>
                        <span className="text-green-600">Copied!</span>
                      </>
                    ) : (
                      <>
                        <svg
                          className="w-4 h-4"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
                          />
                        </svg>
                        Token
                      </>
                    )}
                  </button>
                  <a
                    href="/swagger-ui/index.html"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm text-gray-600 hover:text-gray-900"
                  >
                    API Docs
                  </a>
                  <a
                    href="/grafana"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm text-gray-600 hover:text-gray-900"
                    title="Available on production only"
                  >
                    Grafana
                  </a>
                  <a
                    href="/grafana/explore?orgId=1&left=%7B%22datasource%22:%22Loki%22%7D"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm text-gray-600 hover:text-gray-900"
                    title="Available on production only"
                  >
                    Logs
                  </a>
                </AdminOnly>

                <div className="flex items-center gap-4 pl-4 border-l border-gray-200">
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center">
                      <span className="text-sm font-medium text-indigo-600">
                        {user?.email.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">{user?.email}</p>
                      <p className="text-xs text-gray-500">{user?.role}</p>
                    </div>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="text-sm text-gray-600 hover:text-gray-900"
                  >
                    Logout
                  </button>
                </div>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm text-gray-600 hover:text-gray-900">
                  Login
                </Link>
                <Link
                  to="/register"
                  className="text-sm bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700"
                >
                  Register
                </Link>
              </>
            )}
          </nav>

          {/* Mobile: User avatar + Hamburger */}
          <div className="flex md:hidden items-center gap-3">
            {isAuthenticated && (
              <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center">
                <span className="text-sm font-medium text-indigo-600">
                  {user?.email.charAt(0).toUpperCase()}
                </span>
              </div>
            )}
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="p-2 text-gray-600 hover:text-gray-900"
              aria-label="Toggle menu"
            >
              {menuOpen ? (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              ) : (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                </svg>
              )}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {menuOpen && (
          <nav className="md:hidden mt-4 pt-4 border-t border-gray-200">
            {isAuthenticated ? (
              <div className="space-y-3">
                {/* User Info */}
                <div className="pb-3 border-b border-gray-100">
                  <p className="text-sm font-medium text-gray-900">{user?.email}</p>
                  <p className="text-xs text-gray-500">{user?.role}</p>
                </div>

                {/* Admin Links */}
                <AdminOnly>
                  <div className="space-y-3 pb-3 border-b border-gray-100">
                    <button
                      onClick={handleCopyToken}
                      className="w-full text-left text-sm text-gray-600 hover:text-gray-900 flex items-center gap-2"
                    >
                      <svg
                        className="w-4 h-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
                        />
                      </svg>
                      {copied ? 'Token Copied!' : 'Copy Token'}
                    </button>
                    <a
                      href="/swagger-ui/index.html"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block text-sm text-gray-600 hover:text-gray-900"
                      onClick={() => setMenuOpen(false)}
                    >
                      API Docs
                    </a>
                    <a
                      href="/grafana"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block text-sm text-gray-600 hover:text-gray-900"
                      onClick={() => setMenuOpen(false)}
                    >
                      Grafana
                    </a>
                    <a
                      href="/grafana/explore?orgId=1&left=%7B%22datasource%22:%22Loki%22%7D"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block text-sm text-gray-600 hover:text-gray-900"
                      onClick={() => setMenuOpen(false)}
                    >
                      Logs
                    </a>
                  </div>
                </AdminOnly>

                {/* Logout */}
                <button
                  onClick={handleLogout}
                  className="w-full text-left text-sm text-red-600 hover:text-red-700"
                >
                  Logout
                </button>
              </div>
            ) : (
              <div className="space-y-3">
                <Link
                  to="/login"
                  className="block text-sm text-gray-600 hover:text-gray-900"
                  onClick={() => setMenuOpen(false)}
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="block text-sm text-indigo-600 hover:text-indigo-700 font-medium"
                  onClick={() => setMenuOpen(false)}
                >
                  Register
                </Link>
              </div>
            )}
          </nav>
        )}
      </div>
    </header>
  );
}

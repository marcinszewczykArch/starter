import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { Dashboard } from './pages/Dashboard';
import VerifyEmailPage from './pages/VerifyEmailPage';
import { VerificationPendingPage } from './pages/VerificationPendingPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/ResetPasswordPage';

function AppRoutes() {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-500">Loading...</div>
      </div>
    );
  }

  // Determine where to redirect authenticated users
  const getAuthenticatedRedirect = () => {
    if (user && !user.emailVerified) {
      return '/verification-pending';
    }
    return '/dashboard';
  };

  return (
    <Routes>
      {/* Public routes - redirect to dashboard/verification if already logged in */}
      <Route
        path="/"
        element={
          isAuthenticated ? <Navigate to={getAuthenticatedRedirect()} replace /> : <LandingPage />
        }
      />
      <Route
        path="/login"
        element={
          isAuthenticated ? <Navigate to={getAuthenticatedRedirect()} replace /> : <LoginPage />
        }
      />
      <Route
        path="/register"
        element={
          isAuthenticated ? <Navigate to={getAuthenticatedRedirect()} replace /> : <RegisterPage />
        }
      />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      {/* Verification pending - for authenticated but unverified users */}
      <Route
        path="/verification-pending"
        element={
          <ProtectedRoute requireVerified={false}>
            <VerificationPendingPage />
          </ProtectedRoute>
        }
      />

      {/* Protected routes - require verified email */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

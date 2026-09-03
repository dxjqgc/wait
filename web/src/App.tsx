import { Navigate, Route, Routes } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuth } from './auth/useAuth';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import AuthCallbackPage from './pages/AuthCallbackPage';
import ProfilePage from './pages/ProfilePage';
import MatchListPage from './pages/MatchListPage';
import ConversationDetailPage from './pages/ConversationDetailPage';
import { useMeQuery } from './api/me';

function RequireAuth({ children }: { children: JSX.Element }) {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
}

function MyUserIdSync() {
  const { data } = useMeQuery();
  useEffect(() => {
    if (data?.id) localStorage.setItem('wait_my_user_id', String(data.id));
  }, [data?.id]);
  return null;
}

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/auth/callback" element={<AuthCallbackPage />} />
        <Route
          path="/"
          element={
            <RequireAuth>
              <HomePage />
            </RequireAuth>
          }
        />
        <Route
          path="/profile"
          element={
            <RequireAuth>
              <ProfilePage />
            </RequireAuth>
          }
        />
        <Route
          path="/match"
          element={
            <RequireAuth>
              <MatchListPage />
            </RequireAuth>
          }
        />
        <Route
          path="/c/:id"
          element={
            <RequireAuth>
              <ConversationDetailPage />
            </RequireAuth>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <RequireAuth>
        <MyUserIdSync />
      </RequireAuth>
    </>
  );
}

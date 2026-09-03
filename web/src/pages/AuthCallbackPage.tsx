import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import { userManager } from '../auth/userManager';

export default function AuthCallbackPage() {
  const navigate = useNavigate();
  const { refresh, signIn } = useAuth();

  useEffect(() => {
    let cancelled = false;
    // 如果 URL 里没有 code（比如 Casdoor 注册流程不带 code 直接跳回），
    // 直接走登录流程重新拿 token
    const url = new URL(window.location.href);
    const hasCode = url.searchParams.has('code');
    if (!hasCode) {
      signIn();
      return;
    }
    userManager.signinRedirectCallback()
      .then(async () => {
        if (cancelled) return;
        await refresh();
        navigate('/', { replace: true });
      })
      .catch((err) => {
        console.error('OIDC callback failed', err);
        navigate('/login', { replace: true });
      });
    return () => {
      cancelled = true;
    };
  }, [navigate, refresh, signIn]);

  return (
    <div className="min-h-screen flex items-center justify-center text-gray-500">
      正在处理登录回调...
    </div>
  );
}

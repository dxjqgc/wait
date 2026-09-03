import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

export default function LoginPage() {
  const { isAuthenticated, signIn } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) navigate('/', { replace: true });
  }, [isAuthenticated, navigate]);

  function signUp() {
    // 跳到 Casdoor 注册页，带 redirect_uri 和 client_id，
    // Casdoor 注册成功后会按 redirect_uri 跳回应用并产出 authorization code，
    // 走和登录一致的回调流程。
    const params = new URLSearchParams({
      client_id: import.meta.env.VITE_CASDOOR_CLIENT_ID,
      redirect_uri: import.meta.env.VITE_CASDOOR_REDIRECT_URI,
      response_type: 'code',
      scope: 'openid profile email phone',
      state: Math.random().toString(36).slice(2),
    });
    window.location.href = `${import.meta.env.VITE_CASDOOR_SERVER_URL}/signup/wait?${params.toString()}`;
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-6">
      <div className="text-center mb-10">
        <h1 className="text-5xl font-semibold tracking-tight mb-2">wait</h1>
        <p className="text-white/40 text-sm">少而精的交友系统</p>
      </div>
      <button
        onClick={() => signIn()}
        className="px-8 py-3 rounded-full bg-white text-black font-medium hover:bg-white/90 transition-colors"
      >
        使用 Casdoor 登录
      </button>
      <button
        onClick={signUp}
        className="mt-3 text-sm text-white/50 hover:text-white transition-colors"
      >
        没有账号？注册
      </button>
      <p className="text-xs text-white/30 mt-8 max-w-xs text-center">
        登录即同意本系统的会话与匹配规则
      </p>
    </div>
  );
}

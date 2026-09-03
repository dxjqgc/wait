import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Plus } from 'lucide-react';
import { useAuth } from '../auth/useAuth';
import { useMeQuery } from '../api/me';
import { fetchConversations } from '../api/conversation';
import { Avatar } from '../components/Avatar';

export default function HomePage() {
  const { data: me } = useMeQuery();
  const { signOut } = useAuth();
  const { data, isLoading, error } = useQuery({
    queryKey: ['conversations'],
    queryFn: fetchConversations,
    refetchInterval: 8000,
  });

  return (
    <div className="min-h-screen flex flex-col">
      <header className="px-5 py-4 flex items-center justify-between border-b border-white/[0.06]">
        <Link to="/profile" className="flex items-center gap-3 group">
          <Avatar src={me?.avatar} name={me?.nickname} size={36} />
          <span className="text-lg font-semibold tracking-tight">wait</span>
        </Link>
        <div className="flex items-center gap-1">
          <Link
            to="/match"
            className="w-9 h-9 rounded-full bg-white text-black flex items-center justify-center hover:bg-white/90 transition-colors"
            title="开始匹配"
          >
            <Plus size={20} />
          </Link>
          <button
            onClick={() => signOut()}
            className="btn-ghost text-xs ml-1"
            title="退出登录"
          >
            退出
          </button>
        </div>
      </header>

      <main className="flex-1 overflow-auto px-4 py-4">
        {error && (
          <div className="text-red-400 text-sm px-3 py-2 mb-3">
            {(error as Error).message}
          </div>
        )}

        {isLoading ? (
          <div className="text-white/40 text-sm py-12 text-center">加载中...</div>
        ) : data && data.length > 0 ? (
          <ul className="space-y-1 max-w-2xl mx-auto">
            {data.map((c) => {
              const otherName = c.iAmInitiator ? c.targetNickname : c.initiatorNickname;
              const otherAvatar = c.iAmInitiator ? c.targetAvatar : c.initiatorAvatar;
              const isActive = c.state === 'ACTIVE';
              const isPending = c.state === 'PENDING';
              const waitingForOther = isPending && c.iAmInitiator;
              const waitingForMe = isPending && c.iAmTargetPendingReply;

              return (
                <li key={c.id}>
                  <Link
                    to={`/c/${c.id}`}
                    className="flex items-center gap-3 px-3 py-3 rounded-xl hover:bg-white/[0.04] transition-colors"
                  >
                    <Avatar src={otherAvatar} name={otherName} size={48} />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-medium truncate">
                          {otherName || '未命名'}
                        </span>
                        {isActive && <span className="chip">进行中</span>}
                        {waitingForOther && (
                          <span className="chip">等待回复</span>
                        )}
                        {waitingForMe && (
                          <span className="chip bg-amber-500/20 text-amber-300">待你回复</span>
                        )}
                      </div>
                      <p className="text-sm text-white/50 truncate mt-0.5">
                        {c.greetingMsg}
                      </p>
                    </div>
                  </Link>
                </li>
              );
            })}
          </ul>
        ) : (
          <EmptyState />
        )}
      </main>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="max-w-md mx-auto py-20 text-center">
      <div className="w-14 h-14 rounded-full bg-white/[0.06] mx-auto mb-4 flex items-center justify-center text-white/40 text-2xl">
        ·
      </div>
      <h2 className="text-lg font-medium mb-1">还没有会话</h2>
      <p className="text-sm text-white/40 mb-6">点右上角加号，去匹配列表认识人</p>
      <Link to="/match" className="btn-primary inline-block">
        开始匹配
      </Link>
    </div>
  );
}

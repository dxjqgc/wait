import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Send } from 'lucide-react';
import {
  endConversation,
  fetchConversation,
  fetchMessages,
  markRead,
  preReply,
  replyConversation,
  sendMessageHttp,
  type Message,
} from '../api/conversation';
import { useChatWebSocket } from '../hooks/useChatWebSocket';
import { Avatar } from '../components/Avatar';

export default function ConversationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const convId = Number(id);
  const navigate = useNavigate();

  const { data: conv, refetch: refetchConv } = useQuery({
    queryKey: ['conv', convId],
    queryFn: () => fetchConversation(convId),
  });

  const [messages, setMessages] = useState<Message[]>([]);
  const [loadingMsgs, setLoadingMsgs] = useState(true);
  const [text, setText] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [preReplyItems, setPreReplyItems] = useState<{ key: string; value: string }[] | null>(null);

  const ws = useChatWebSocket({
    onMessage: (m) => {
      if (m.conversationId !== convId) return;
      setMessages((cur) => (cur.some((x) => x.id === m.id) ? cur : [...cur, m]));
    },
  });

  const scrollRef = useRef<HTMLDivElement>(null);
  const myUserId = Number(localStorage.getItem('wait_my_user_id') ?? '0');

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight });
  }, [messages]);

  useEffect(() => {
    fetchMessages(convId)
      .then((list) => {
        setMessages(list.reverse());
        markRead(convId).catch(() => {});
      })
      .catch((e) => setError((e as Error)?.message ?? '加载消息失败'))
      .finally(() => setLoadingMsgs(false));
  }, [convId]);

  useEffect(() => {
    if (conv?.iAmTargetPendingReply) {
      preReply(convId)
        .then((r) => setPreReplyItems(r.items.map((it) => ({ key: it.key, value: it.value }))))
        .catch((e) => setError((e as Error)?.message ?? '加载要求失败'));
    }
  }, [convId, conv?.iAmTargetPendingReply]);

  async function send() {
    if (!text.trim()) return;
    setError(null);
    try {
      if (conv?.iAmTargetPendingReply) {
        await replyConversation(convId, text.trim());
      } else if (ws.connected) {
        await ws.send(convId, text.trim());
      } else {
        await sendMessageHttp(convId, text.trim());
      }
      setText('');
      refetchConv();
    } catch (e) {
      setError((e as Error)?.message ?? '发送失败');
    }
  }

  async function end() {
    if (!confirm('确定结束此次会话？结束后双方恢复自由身。')) return;
    try {
      await endConversation(convId);
      await refetchConv();
    } catch (e) {
      setError((e as Error)?.message ?? '结束失败');
    }
  }

  if (!conv) {
    return (
      <div className="min-h-screen flex flex-col">
        <header className="px-5 py-4 flex items-center gap-3 border-b border-white/[0.06]">
          <button onClick={() => navigate('/')} className="btn-ghost -ml-2">
            <ArrowLeft size={18} />
          </button>
          <span className="text-lg font-semibold flex-1">会话</span>
        </header>
        <div className="flex-1 flex items-center justify-center text-white/40 text-sm">
          加载中...
        </div>
      </div>
    );
  }

  const otherName = conv.iAmInitiator ? conv.targetNickname : conv.initiatorNickname;
  const otherAvatar = conv.iAmInitiator ? conv.targetAvatar : conv.initiatorAvatar;
  const isEnded = conv.state === 'ENDED';

  return (
    <div className="h-screen flex flex-col">
      {/* 顶部 */}
      <header className="px-5 py-3 flex items-center gap-3 border-b border-white/[0.06]">
        <button onClick={() => navigate('/')} className="btn-ghost -ml-2">
          <ArrowLeft size={18} />
        </button>
        <Avatar src={otherAvatar} name={otherName} size={36} />
        <div className="flex-1 min-w-0">
          <div className="font-medium truncate">{otherName || '未命名'}</div>
          <div className="text-[11px] text-white/40 flex items-center gap-1.5">
            <span className={`w-1.5 h-1.5 rounded-full ${ws.connected ? 'bg-emerald-400' : 'bg-white/30'}`} />
            {conv.state === 'ACTIVE' ? '进行中' : conv.state === 'PENDING' ? '待回复' : '已结束'}
          </div>
        </div>
        {!isEnded && (
          <button onClick={() => end()} className="btn-outline text-xs">结束会话</button>
        )}
      </header>

      {/* 待回复时的对方要求 */}
      {conv.iAmTargetPendingReply && preReplyItems && preReplyItems.length > 0 && (
        <div className="px-5 py-3 bg-amber-500/[0.08] border-b border-amber-500/20">
          <div className="text-xs text-amber-300/80 mb-1.5">对方对你的标签要求 · 回复即视为已确认</div>
          <ul className="text-sm space-y-1">
            {preReplyItems.map((it) => (
              <li key={it.key} className="flex justify-between">
                <span className="text-white/60">{it.key}</span>
                <span className="text-white/90">{it.value || '-'}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* 招呼语 */}
      {conv.greetingMsg && (
        <div className="px-5 py-2 bg-white/[0.03] border-b border-white/[0.04] text-sm">
          <span className="text-white/40 text-xs mr-2">招呼语</span>
          <span className="text-white/80">{conv.greetingMsg}</span>
        </div>
      )}

      {error && (
        <div className="px-5 py-2 text-red-400 text-sm bg-red-500/[0.06]">{error}</div>
      )}

      {/* 消息流 */}
      <div ref={scrollRef} className="flex-1 overflow-auto px-4 py-4">
        <div className="max-w-2xl mx-auto space-y-2">
          {loadingMsgs ? (
            <div className="text-center text-white/40 text-sm py-8">加载中...</div>
          ) : messages.length === 0 ? (
            <div className="text-center text-white/40 text-sm py-8">
              {conv.iAmTargetPendingReply ? '回复对方开启对话吧' : '暂无消息'}
            </div>
          ) : (
            messages.map((m) => {
              const mine = m.senderId === myUserId;
              return (
                <div key={m.id} className={`flex ${mine ? 'justify-end' : 'justify-start'}`}>
                  <div
                    className={`max-w-[75%] px-3.5 py-2 rounded-2xl text-sm break-words ${
                      mine
                        ? 'bg-white text-black rounded-br-sm'
                        : 'bg-white/[0.08] text-white rounded-bl-sm'
                    }`}
                  >
                    {m.content}
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* 输入栏 */}
      {!isEnded && (
        <div className="px-3 py-3 border-t border-white/[0.06] flex items-center gap-2">
          <input
            value={text}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); } }}
            className="input flex-1"
            placeholder={conv.iAmTargetPendingReply ? '回复后才能继续聊天...' : '输入消息'}
          />
          <button
            onClick={send}
            disabled={!text.trim()}
            className="w-10 h-10 rounded-full bg-white text-black flex items-center justify-center disabled:opacity-30 hover:bg-white/90"
          >
            <Send size={16} />
          </button>
        </div>
      )}
    </div>
  );
}

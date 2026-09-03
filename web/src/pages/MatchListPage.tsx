import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, MapPin, Check } from 'lucide-react';
import { fetchCandidates, preGreet, greet, type MatchCandidate } from '../api/match';
import { Avatar } from '../components/Avatar';

export default function MatchListPage() {
  const navigate = useNavigate();
  const [lng, setLng] = useState<number | null>(null);
  const [lat, setLat] = useState<number | null>(null);
  const [radius, setRadius] = useState(10);
  const [picked, setPicked] = useState<MatchCandidate | null>(null);

  useEffect(() => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLng(pos.coords.longitude);
        setLat(pos.coords.latitude);
      },
      () => { /* user denied */ },
      { enableHighAccuracy: true, timeout: 8000 },
    );
  }, []);

  const { data, isLoading, error, refetch, isFetching } = useQuery({
    queryKey: ['match-candidates', lng, lat, radius],
    queryFn: () => fetchCandidates(lng ?? undefined, lat ?? undefined, radius),
    enabled: lng != null && lat != null,
  });

  return (
    <div className="min-h-screen flex flex-col">
      <header className="px-5 py-4 flex items-center gap-3 border-b border-white/[0.06]">
        <button onClick={() => navigate('/')} className="btn-ghost -ml-2">
          <ArrowLeft size={18} />
        </button>
        <h1 className="text-lg font-semibold flex-1">匹配</h1>
        <div className="flex items-center gap-2 text-sm">
          <MapPin size={14} className="text-white/40" />
          <select
            value={radius}
            onChange={(e) => setRadius(Number(e.target.value))}
            className="bg-transparent text-white/70 text-sm outline-none"
          >
            <option value={5} className="bg-[#141414]">5km</option>
            <option value={10} className="bg-[#141414]">10km</option>
            <option value={30} className="bg-[#141414]">30km</option>
            <option value={100} className="bg-[#141414]">100km</option>
          </select>
        </div>
      </header>

      {lng == null && (
        <div className="px-5 py-3 text-sm text-white/50 bg-white/[0.03] border-b border-white/[0.04]">
          正在获取定位...或检查浏览器权限
        </div>
      )}

      <main className="flex-1 overflow-auto px-4 py-4">
        {error && (
          <div className="text-red-400 text-sm px-3 py-2 mb-3 surface">
            {(error as Error).message}
          </div>
        )}

        {isLoading ? (
          <div className="text-white/40 text-sm py-12 text-center">加载中...</div>
        ) : data && data.length > 0 ? (
          <ul className="max-w-2xl mx-auto space-y-2">
            {data.map((c) => (
              <li key={c.userId}>
                <button
                  onClick={() => setPicked(c)}
                  disabled={!c.requirementMatched}
                  className="surface px-4 py-3 flex items-center gap-3 w-full text-left hover:border-white/15 transition-colors disabled:opacity-50"
                >
                  <Avatar src={c.avatar} name={c.nickname} size={48} />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="font-medium truncate">{c.nickname}</span>
                      <span className="text-xs text-white/40">
                        {c.gender === 1 ? '男' : c.gender === 2 ? '女' : ''} · {c.age ?? '-'}岁 · {c.height ?? '-'}cm
                      </span>
                      {c.distanceKm != null && (
                        <span className="text-xs text-white/40 flex items-center gap-0.5">
                          <MapPin size={11} /> {c.distanceKm.toFixed(1)}km
                        </span>
                      )}
                    </div>
                    <div className="text-sm text-white/50 mt-0.5 truncate">
                      {c.city} {c.district} · {c.profession || '职业未填'} · {c.education || '学历未填'}
                    </div>
                    <div className="flex flex-wrap gap-1 mt-1.5">
                      {[...(c.hobbies ?? []), ...(c.tags ?? [])].slice(0, 5).map((t) => (
                        <span key={t} className="chip">{t}</span>
                      ))}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-xl font-semibold text-white/80">
                      {c.matchScore}
                    </div>
                    <div className="text-[10px] text-white/30 uppercase tracking-wider">匹配</div>
                  </div>
                </button>
              </li>
            ))}
          </ul>
        ) : (
          !isLoading && (
            <div className="max-w-md mx-auto py-20 text-center text-white/40">
              <p className="mb-1">附近没有匹配候选</p>
              <p className="text-xs">尝试扩大半径或完善自己的资料</p>
            </div>
          )
        )}
      </main>

      {picked && (
        <GreetDialog
          candidate={picked}
          onClose={() => setPicked(null)}
          onSuccess={() => {
            setPicked(null);
            refetch();
          }}
        />
      )}
    </div>
  );
}

function GreetDialog({
  candidate, onClose, onSuccess,
}: {
  candidate: MatchCandidate;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [view, setView] = useState<{ key: string; value: string }[] | null>(null);
  const [greeting, setGreeting] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    preGreet(candidate.userId)
      .then((r) => setView(r.items.map((it) => ({ key: it.key, value: it.value }))))
      .catch((e) => setError((e as Error)?.message ?? '加载失败'));
  }, [candidate.userId]);

  async function submit() {
    if (!greeting.trim()) return;
    setSubmitting(true);
    setError(null);
    try {
      const confirmations = (view ?? []).map((it) => ({
        key: it.key,
        value: it.value ?? '',
        confirmed: true,
      }));
      await greet(candidate.userId, greeting.trim(), confirmations);
      onSuccess();
    } catch (e) {
      setError((e as Error)?.message ?? '打招呼失败');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 px-4">
      <div className="surface p-6 w-[480px] max-w-full max-h-[85vh] overflow-auto">
        <h2 className="text-lg font-semibold mb-1">向 {candidate.nickname} 打招呼</h2>
        {error && <div className="text-red-400 text-sm mb-3">{error}</div>}

        <div className="mb-4">
          <div className="text-xs text-white/40 mb-1.5">对方的标签要求</div>
          {!view ? (
            <div className="text-sm text-white/30">加载中...</div>
          ) : view.length === 0 ? (
            <div className="text-sm text-white/30">无要求</div>
          ) : (
            <ul className="space-y-1 text-sm">
              {view.map((it) => (
                <li key={it.key} className="flex justify-between items-center">
                  <span className="text-white/60">{it.key}</span>
                  <span className="flex items-center gap-1">
                    <span className="text-white/90">{it.value || '-'}</span>
                    <Check size={12} className="text-emerald-400" />
                  </span>
                </li>
              ))}
            </ul>
          )}
          <p className="text-xs text-white/30 mt-2">点击下方按钮即视为你已确认满足对方要求</p>
        </div>

        <div className="mb-4">
          <div className="text-xs text-white/40 mb-1.5">招呼语</div>
          <textarea
            value={greeting}
            onChange={(e) => setGreeting(e.target.value)}
            maxLength={256}
            rows={3}
            autoFocus
            className="input resize-none"
            placeholder="真诚地说点什么..."
          />
          <div className="text-xs text-white/30 mt-1 text-right">{greeting.length}/256</div>
        </div>

        <div className="flex justify-end gap-2">
          <button onClick={onClose} className="btn-ghost">取消</button>
          <button
            onClick={submit}
            disabled={submitting || !greeting.trim()}
            className="btn-primary"
          >
            {submitting ? '发送中...' : '发送打招呼'}
          </button>
        </div>
      </div>
    </div>
  );
}

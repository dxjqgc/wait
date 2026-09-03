import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useEffect, useState } from 'react';
import {
  fetchMyProfile,
  updateMyProfile,
  type ProfileVO,
  type UpdateProfileDTO,
} from '../api/profile';
import { Avatar } from '../components/Avatar';
import { RegionCascader } from '../components/RegionCascader';

const EDUCATION_OPTIONS = ['', '高中', '大专', '本科', '硕士', '博士'];

function empty(): ProfileVO {
  return {
    userId: 0, nickname: '', avatar: '', gender: 0,
    realName: '', age: undefined, birthday: '',
    provinceCode: '', provinceName: '',
    cityCode: '', cityName: '',
    districtCode: '', districtName: '',
    profession: '',
    height: undefined, education: '',
    personality: [], appearances: [], hobbies: [], tags: [],
    requirementPreset: {}, requirementCustom: [],
    matchVisibility: true,
  };
}

export default function ProfilePage() {
  const navigate = useNavigate();
  const [p, setP] = useState<ProfileVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState(false);

  useEffect(() => {
    fetchMyProfile()
      .then(setP)
      .catch((e) => setError(e?.message ?? '加载失败'))
      .finally(() => setLoading(false));
  }, []);

  function patch<K extends keyof ProfileVO>(key: K, value: ProfileVO[K]) {
    setP((cur) => (cur ? { ...cur, [key]: value } : cur));
  }

  async function save() {
    if (!p) return;
    setSaving(true);
    setError(null);
    try {
      const dto: UpdateProfileDTO = {
        nickname: p.nickname,
        gender: p.gender,
        realName: p.realName,
        age: p.age,
        birthday: p.birthday,
        provinceCode: p.provinceCode,
        cityCode: p.cityCode,
        districtCode: p.districtCode,
        profession: p.profession,
        height: p.height,
        education: p.education,
        personality: p.personality,
        appearances: p.appearances,
        hobbies: p.hobbies,
        tags: p.tags,
        matchVisibilityValue: p.matchVisibility ? 1 : 0,
      };
      const updated = await updateMyProfile(dto);
      setP(updated);
      setToast(true);
      setTimeout(() => setToast(false), 1500);
    } catch (e: unknown) {
      setError((e as Error)?.message ?? '保存失败');
    } finally {
      setSaving(false);
    }
  }

  async function locate() {
    if (!navigator.geolocation) {
      setError('浏览器不支持定位');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        try {
          const { updateMyLocation } = await import('../api/profile');
          await updateMyLocation(pos.coords.longitude, pos.coords.latitude);
          setToast(true);
          setTimeout(() => setToast(false), 1500);
        } catch (e: unknown) {
          setError((e as Error)?.message ?? '定位上报失败');
        }
      },
      (err) => setError('定位失败：' + err.message),
      { enableHighAccuracy: true, timeout: 8000 },
    );
  }

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center text-white/40">加载中...</div>;
  }
  if (!p) {
    return <div className="min-h-screen flex items-center justify-center text-red-400">{error ?? '无数据'}</div>;
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header className="px-5 py-4 flex items-center gap-3 border-b border-white/[0.06]">
        <button onClick={() => navigate('/')} className="btn-ghost -ml-2">
          <ArrowLeft size={18} />
        </button>
        <h1 className="text-lg font-semibold flex-1">我的资料</h1>
        <button onClick={() => locate()} className="btn-outline">更新定位</button>
        <button onClick={() => save()} disabled={saving} className="btn-primary">
          {saving ? '保存中...' : '保存'}
        </button>
      </header>

      <main className="flex-1 overflow-auto px-4 py-6">
        <div className="max-w-2xl mx-auto space-y-4">
          {error && (
            <div className="surface px-3 py-2 text-red-400 text-sm">{error}</div>
          )}

          {/* 头像 + 昵称 */}
          <section className="surface p-5 flex items-center gap-4">
            <Avatar src={p.avatar} name={p.nickname} size={64} />
            <div className="flex-1">
              <label className="text-xs text-white/40">昵称</label>
              <input
                value={p.nickname}
                onChange={(e) => patch('nickname', e.target.value)}
                className="input"
              />
            </div>
            <div className="w-24">
              <label className="text-xs text-white/40">性别</label>
              <select
                value={p.gender}
                onChange={(e) => patch('gender', Number(e.target.value))}
                className="input"
              >
                <option value={0}>未填</option>
                <option value={1}>男</option>
                <option value={2}>女</option>
              </select>
            </div>
          </section>

          {/* 基础信息 */}
          <section className="surface p-5">
            <h3 className="text-xs text-white/40 mb-3 uppercase tracking-wide">基础信息</h3>
            <div className="grid grid-cols-2 gap-4 mb-4">
              <Field label="真实姓名">
                <input value={p.realName ?? ''} onChange={(e) => patch('realName', e.target.value)} className="input" />
              </Field>
              <Field label="生日">
                <input type="date" value={p.birthday ?? ''} onChange={(e) => patch('birthday', e.target.value)} className="input" />
              </Field>
              <Field label="职业">
                <input value={p.profession ?? ''} onChange={(e) => patch('profession', e.target.value)} className="input" />
              </Field>
              <Field label="身高 cm">
                <input type="number" value={p.height ?? ''} onChange={(e) => patch('height', e.target.value ? Number(e.target.value) : undefined)} className="input" />
              </Field>
              <Field label="学历">
                <select value={p.education ?? ''} onChange={(e) => patch('education', e.target.value)} className="input">
                  {EDUCATION_OPTIONS.map((x) => (
                    <option key={x} value={x}>{x || '未填'}</option>
                  ))}
                </select>
              </Field>
              <Field label="年龄（自动按生日，可手填）">
                <input type="number" value={p.age ?? ''} onChange={(e) => patch('age', e.target.value ? Number(e.target.value) : undefined)} className="input" />
              </Field>
            </div>
            <div className="mb-1">
              <label className="block text-xs text-white/40 mb-1">常住地</label>
              <RegionCascader
                provinceCode={p.provinceCode ?? undefined}
                cityCode={p.cityCode ?? undefined}
                districtCode={p.districtCode ?? undefined}
                onChange={(next) => setP((cur) => (cur ? { ...cur, ...next } : cur))}
              />
            </div>
          </section>

          {/* 标签 */}
          <section className="surface p-5">
            <h3 className="text-xs text-white/40 mb-3 uppercase tracking-wide">标签</h3>
            <div className="space-y-4">
              <TagEditor label="性格" items={p.personality ?? []} onAdd={(v) => patch('personality', [...(p.personality ?? []), v])} onRemove={(i) => patch('personality', (p.personality ?? []).filter((_, idx) => idx !== i))} />
              <TagEditor label="外形" items={p.appearances ?? []} onAdd={(v) => patch('appearances', [...(p.appearances ?? []), v])} onRemove={(i) => patch('appearances', (p.appearances ?? []).filter((_, idx) => idx !== i))} />
              <TagEditor label="兴趣" items={p.hobbies ?? []} onAdd={(v) => patch('hobbies', [...(p.hobbies ?? []), v])} onRemove={(i) => patch('hobbies', (p.hobbies ?? []).filter((_, idx) => idx !== i))} />
              <TagEditor label="自定义标签" items={p.tags ?? []} onAdd={(v) => patch('tags', [...(p.tags ?? []), v])} onRemove={(i) => patch('tags', (p.tags ?? []).filter((_, idx) => idx !== i))} />
            </div>
          </section>

          {/* 可见性 */}
          <section className="surface p-5 flex items-center justify-between">
            <div>
              <h3 className="text-sm font-medium">允许被匹配</h3>
              <p className="text-xs text-white/40 mt-0.5">关闭后不会出现在别人的匹配列表里</p>
            </div>
            <button
              onClick={() => patch('matchVisibility', !p.matchVisibility)}
              className={`w-12 h-7 rounded-full transition-colors ${p.matchVisibility ? 'bg-white' : 'bg-white/15'}`}
            >
              <span className={`block w-5 h-5 rounded-full bg-black transition-transform ${p.matchVisibility ? 'translate-x-6' : 'translate-x-1'}`} />
            </button>
          </section>
        </div>
      </main>

      {toast && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 surface px-4 py-2 text-sm">
          已保存
        </div>
      )}
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="block text-xs text-white/40 mb-1">{label}</label>
      {children}
    </div>
  );
}

function TagEditor({
  label, items, onAdd, onRemove,
}: {
  label: string;
  items: string[];
  onAdd: (v: string) => void;
  onRemove: (i: number) => void;
}) {
  const [value, setValue] = useState('');
  return (
    <div>
      <div className="text-xs text-white/40 mb-1.5">{label}</div>
      <div className="flex flex-wrap gap-1.5 mb-2">
        {items.map((t, i) => (
          <span key={i} className="chip group">
            {t}
            <button
              onClick={() => onRemove(i)}
              className="ml-1 text-white/30 hover:text-white/70"
            >
              ×
            </button>
          </span>
        ))}
        {items.length === 0 && <span className="text-xs text-white/30">未填</span>}
      </div>
      <div className="flex gap-2">
        <input
          value={value}
          onChange={(e) => setValue(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && value.trim()) {
              e.preventDefault();
              onAdd(value.trim());
              setValue('');
            }
          }}
          className="input flex-1"
          placeholder="输入后回车"
        />
        <button
          type="button"
          onClick={() => {
            if (value.trim()) {
              onAdd(value.trim());
              setValue('');
            }
          }}
          className="btn-outline"
        >
          添加
        </button>
      </div>
    </div>
  );
}

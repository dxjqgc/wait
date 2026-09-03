interface AvatarProps {
  src?: string | null;
  name?: string | null;
  size?: number;
  className?: string;
}

export function Avatar({ src, name, size = 40, className = '' }: AvatarProps) {
  const initial = (name?.trim()?.[0] ?? '?').toUpperCase();
  return (
    <div
      className={`rounded-full overflow-hidden flex-shrink-0 flex items-center justify-center bg-white/[0.08] text-white/60 font-medium ${className}`}
      style={{ width: size, height: size, fontSize: size * 0.4 }}
    >
      {src ? (
        <img src={src} alt="" className="w-full h-full object-cover" />
      ) : (
        <span>{initial}</span>
      )}
    </div>
  );
}

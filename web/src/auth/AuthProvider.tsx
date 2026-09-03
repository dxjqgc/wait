import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { userManager, type OidcUser } from './userManager';

interface AuthContextValue {
  user: OidcUser | null;
  isAuthenticated: boolean;
  loading: boolean;
  signIn: (extraQueryParams?: Record<string, string>) => Promise<void>;
  signOut: () => Promise<void>;
  refresh: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<OidcUser | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    const u = await userManager.getUser();
    setUser(u);
  }, []);

  useEffect(() => {
    let mounted = true;
    userManager.getUser()
      .then((u) => {
        if (mounted) {
          setUser(u);
          setLoading(false);
        }
      })
      .catch(() => {
        if (mounted) setLoading(false);
      });
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    // 监听 oidc-client-ts 的 user 加载/卸载事件，确保回调完成后状态同步
    const onUpdate = () => {
      userManager.getUser().then((u) => setUser(u));
    };
    const onUnloaded = () => setUser(null);
    userManager.events.addUserLoaded(onUpdate);
    userManager.events.addUserUnloaded(onUnloaded);
    return () => {
      userManager.events.removeUserLoaded(onUpdate);
      userManager.events.removeUserUnloaded(onUnloaded);
    };
  }, []);

  const signIn = useCallback(async (extraQueryParams?: Record<string, string>) => {
    await userManager.signinRedirect({
      extraQueryParams: extraQueryParams ?? {},
    });
  }, []);

  const signOut = useCallback(async () => {
    await userManager.signoutRedirect();
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isAuthenticated: !!user && !user.expired,
    loading,
    signIn,
    signOut,
    refresh,
  }), [user, loading, signIn, signOut, refresh]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

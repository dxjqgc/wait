import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import { userManager } from '../auth/userManager';

export const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL + '/api',
  timeout: 15000,
});

api.interceptors.request.use(async (config) => {
  const user = await userManager.getUser();
  if (user?.access_token) {
    config.headers.Authorization = `Bearer ${user.access_token}`;
  }
  return config;
});

const RETRY_FLAG = '___retried';

api.interceptors.response.use(
  (resp) => resp,
  async (error) => {
    const original = error.config as InternalAxiosRequestConfig & { [RETRY_FLAG]?: boolean };
    if (error.response?.status === 401 && !original[RETRY_FLAG]) {
      original[RETRY_FLAG] = true;
      try {
        // 尝试静默续期；若 user store 没有有效 token 则抛错走 catch
        const refreshed = await userManager.signinSilent();
        if (refreshed?.access_token) {
          original.headers.Authorization = `Bearer ${refreshed.access_token}`;
          return api(original);
        }
      } catch {
        // 静默续期失败再跳登录页
        userManager.signinRedirect().catch(() => {});
      }
    }
    return Promise.reject(error);
  },
);

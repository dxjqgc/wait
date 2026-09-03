import { UserManager, WebStorageStateStore, type User } from 'oidc-client-ts';

const settings = {
  authority: import.meta.env.VITE_CASDOOR_SERVER_URL,
  client_id: import.meta.env.VITE_CASDOOR_CLIENT_ID,
  client_secret: import.meta.env.VITE_CASDOOR_CLIENT_SECRET,
  redirect_uri: import.meta.env.VITE_CASDOOR_REDIRECT_URI,
  scope: 'openid profile email phone',
  response_type: 'code',
  post_logout_redirect_uri: window.location.origin,
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  // 关闭静默续签：Casdoor 的 authorization code 是一次性的，
  // 静默续签和正常回调并发时会拿同一个 code 换 token，第二次 400。
  // token 过期就重新走登录流程。
  automaticSilentRenew: false,
};

export const userManager = new UserManager(settings);

export type OidcUser = User;

/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_CASDOOR_SERVER_URL: string;
  readonly VITE_CASDOOR_CLIENT_ID: string;
  readonly VITE_CASDOOR_CLIENT_SECRET: string;
  readonly VITE_CASDOOR_REDIRECT_URI: string;
  readonly VITE_API_BASE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

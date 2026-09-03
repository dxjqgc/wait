import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  define: {
    // sockjs-client 依赖 Node 的 global，浏览器需要 shim
    global: 'globalThis',
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:13001',
        changeOrigin: true,
      },
      '/ws': {
        target: 'http://localhost:13001',
        changeOrigin: true,
        ws: true,
        rewrite: (p) => '/api' + p,
      },
    },
  },
});

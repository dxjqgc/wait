import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { userManager } from '../auth/userManager';
import type { Message } from '../api/conversation';

interface UseWsArgs {
  onMessage: (m: Message) => void;
}

export function useChatWebSocket({ onMessage }: UseWsArgs) {
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);
  const onMessageRef = useRef(onMessage);
  onMessageRef.current = onMessage;

  useEffect(() => {
    let stopped = false;
    let stompClient: Client | null = null;

    async function connect() {
      const user = await userManager.getUser();
      if (!user?.access_token) {
        setError('no access token');
        return;
      }
      // 走相对路径 /ws，由 Vite proxy 转发到 http://localhost:13001/api/ws
      // 避免跨端口 CORS 问题；生产环境配 nginx 同源即可
      const wsUrl = '/ws';
      stompClient = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        connectHeaders: { Authorization: `Bearer ${user.access_token}` },
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        onConnect: () => {
          if (stopped) return;
          setConnected(true);
          setError(null);
          stompClient?.subscribe('/user/queue/messages', (frame) => {
            try {
              const msg = JSON.parse(frame.body) as Message;
              onMessageRef.current(msg);
            } catch (e) {
              console.error('parse ws message failed', e);
            }
          });
        },
        onStompError: (frame) => {
          setError(frame.headers['message'] ?? 'stomp error');
        },
        onWebSocketError: (e) => {
          setError((e as Error)?.message ?? 'ws error');
        },
      });
      stompClient.activate();
      clientRef.current = stompClient;
    }

    connect().catch((e) => setError((e as Error)?.message ?? 'connect failed'));

    return () => {
      stopped = true;
      stompClient?.deactivate().catch(() => {});
      clientRef.current = null;
      setConnected(false);
    };
  }, []);

  async function send(conversationId: number, content: string) {
    const client = clientRef.current;
    if (!client || !client.connected) {
      throw new Error('ws not connected');
    }
    client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ conversationId, content }),
    });
  }

  return { connected, error, send };
}

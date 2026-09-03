import { api } from './client';

export interface Conversation {
  id: number;
  initiatorId: number;
  initiatorNickname: string;
  initiatorAvatar: string;
  targetId: number;
  targetNickname: string;
  targetAvatar: string;
  greetingMsg: string;
  state: string;
  createdAt: string;
  lastMsgAt: string;
  initiatorReadAt: string;
  targetReadAt: string;
  iAmTargetPendingReply: boolean;
  iAmInitiator: boolean;
}

export interface Message {
  id: number;
  conversationId: number;
  senderId: number;
  content: string;
  createdAt: string;
}

export async function fetchConversations(): Promise<Conversation[]> {
  const { data } = await api.get<{ code: number; message: string; data: Conversation[] }>(
    '/conversations',
  );
  return data.data;
}

export async function fetchConversation(id: number): Promise<Conversation> {
  const { data } = await api.get<{ code: number; message: string; data: Conversation }>(
    `/conversations/${id}`,
  );
  return data.data;
}

export async function endConversation(id: number): Promise<void> {
  await api.post(`/conversations/${id}/end`);
}

export async function markRead(id: number): Promise<void> {
  await api.post(`/conversations/${id}/read`);
}

export async function fetchMessages(
  id: number,
  beforeId?: number,
  limit = 50,
): Promise<Message[]> {
  const params = new URLSearchParams();
  if (beforeId) params.set('beforeId', String(beforeId));
  params.set('limit', String(limit));
  const { data } = await api.get<{ code: number; message: string; data: Message[] }>(
    `/conversations/${id}/messages?${params.toString()}`,
  );
  return data.data;
}

export async function replyConversation(id: number, content: string): Promise<number> {
  const { data } = await api.post<{ code: number; message: string; data: number }>(
    `/conversations/${id}/reply`,
    { content },
  );
  return data.data;
}

export async function sendMessageHttp(id: number, content: string): Promise<number> {
  const { data } = await api.post<{ code: number; message: string; data: number }>(
    `/conversations/${id}/messages`,
    { content },
  );
  return data.data;
}

export async function preReply(id: number) {
  const { data } = await api.post<{
    code: number;
    message: string;
    data: {
      ownerId: number;
      preset: Record<string, unknown>;
      custom: { key: string; value: string }[];
      items: { key: string; value: string; satisfied: boolean; confirmed: boolean }[];
    };
  }>(`/conversations/${id}/pre-reply`);
  return data.data;
}

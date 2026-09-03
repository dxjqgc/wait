import { useQuery } from '@tanstack/react-query';
import { api } from './client';

export interface MeVO {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  email: string;
  phone: string;
  gender: number;
  status: number;
  createdAt: string;
}

export async function fetchMe(): Promise<MeVO> {
  const { data } = await api.get<{ code: number; message: string; data: MeVO }>('/me');
  return data.data;
}

export function useMeQuery() {
  return useQuery({
    queryKey: ['me'],
    queryFn: fetchMe,
  });
}

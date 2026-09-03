import { api } from './client';

export interface ProfileVO {
  userId: number;
  nickname: string;
  avatar: string;
  gender: number;
  realName: string;
  age: number;
  birthday: string;
  city: string;
  district: string;
  profession: string;
  height: number;
  education: string;
  personality: string[];
  appearances: string[];
  hobbies: string[];
  tags: string[];
  requirementPreset: Record<string, unknown>;
  requirementCustom: { key: string; value: string }[];
  matchVisibility: boolean;
}

export interface UpdateProfileDTO {
  nickname?: string;
  avatar?: string;
  gender?: number;
  realName?: string;
  age?: number;
  birthday?: string;
  city?: string;
  district?: string;
  profession?: string;
  height?: number;
  education?: string;
  personality?: string[];
  appearances?: string[];
  hobbies?: string[];
  tags?: string[];
  requirementPreset?: Record<string, unknown>;
  requirementCustom?: { key: string; value: string }[];
  matchVisibilityValue?: 0 | 1;
}

export async function fetchMyProfile(): Promise<ProfileVO> {
  const { data } = await api.get<{ code: number; message: string; data: ProfileVO }>(
    '/me/profile',
  );
  return data.data;
}

export async function updateMyProfile(dto: UpdateProfileDTO): Promise<ProfileVO> {
  const { data } = await api.put<{ code: number; message: string; data: ProfileVO }>(
    '/me/profile',
    dto,
  );
  return data.data;
}

export async function updateMyLocation(lng: number, lat: number): Promise<void> {
  await api.post('/me/location', { lng, lat });
}

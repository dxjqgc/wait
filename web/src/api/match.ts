import { api } from './client';

export interface MatchCandidate {
  userId: number;
  nickname: string;
  avatar: string;
  gender: number;
  age: number;
  city: string;
  district: string;
  profession: string;
  height: number;
  education: string;
  personality: string[];
  appearances: string[];
  hobbies: string[];
  tags: string[];
  distanceKm: number | null;
  matchScore: number;
  requirementPreset: Record<string, unknown>;
  requirementCustom: { key: string; value: string }[];
  requirementMatched: boolean;
}

export async function fetchCandidates(
  lng?: number,
  lat?: number,
  radiusKm?: number,
): Promise<MatchCandidate[]> {
  const params = new URLSearchParams();
  if (lng != null) params.set('lng', String(lng));
  if (lat != null) params.set('lat', String(lat));
  if (radiusKm != null) params.set('radiusKm', String(radiusKm));
  const { data } = await api.get<{ code: number; message: string; data: MatchCandidate[] }>(
    `/match/candidates?${params.toString()}`,
  );
  return data.data;
}

export interface TagRequirementView {
  ownerId: number;
  preset: Record<string, unknown>;
  custom: { key: string; value: string }[];
  items: { key: string; value: string; satisfied: boolean; confirmed: boolean }[];
}

export async function preGreet(targetId: number): Promise<TagRequirementView> {
  const { data } = await api.post<{ code: number; message: string; data: TagRequirementView }>(
    `/match/${targetId}/pre-greet`,
  );
  return data.data;
}

export async function greet(
  targetId: number,
  greeting: string,
  confirmations: { key: string; value: string; confirmed: boolean }[],
): Promise<number> {
  const { data } = await api.post<{ code: number; message: string; data: number }>(
    `/match/${targetId}/greet`,
    { greeting, confirmations },
  );
  return data.data;
}

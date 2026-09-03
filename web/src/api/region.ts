import { api } from '../api/client';

export interface Region {
  code: string;
  name: string;
  level: number;
  parentCode: string | null;
}

export async function fetchRegions(parentCode?: string): Promise<Region[]> {
  const params = new URLSearchParams();
  if (parentCode) params.set('parentCode', parentCode);
  const { data } = await api.get<{ code: number; message: string; data: Region[] }>(
    `/regions?${params.toString()}`,
  );
  return data.data;
}

import { useEffect, useState } from 'react';
import { fetchRegions, type Region } from '../api/region';

interface Props {
  provinceCode?: string;
  cityCode?: string;
  districtCode?: string;
  onChange: (next: { provinceCode?: string; cityCode?: string; districtCode?: string }) => void;
  /** 是否必填区县（农村/海外用户可能只到市） */
  requireDistrict?: boolean;
}

export function RegionCascader({
  provinceCode,
  cityCode,
  districtCode,
  onChange,
  requireDistrict = true,
}: Props) {
  const [provinces, setProvinces] = useState<Region[]>([]);
  const [cities, setCities] = useState<Region[]>([]);
  const [districts, setDistricts] = useState<Region[]>([]);

  // 加载省
  useEffect(() => {
    fetchRegions().then(setProvinces).catch(() => {});
  }, []);

  // 省 → 市
  useEffect(() => {
    if (!provinceCode) {
      setCities([]);
      setDistricts([]);
      return;
    }
    fetchRegions(provinceCode).then(setCities).catch(() => {});
    setDistricts([]);
  }, [provinceCode]);

  // 市 → 区
  useEffect(() => {
    if (!cityCode) {
      setDistricts([]);
      return;
    }
    fetchRegions(cityCode).then(setDistricts).catch(() => {});
  }, [cityCode]);

  function handleProvince(code: string) {
    onChange({ provinceCode: code, cityCode: undefined, districtCode: undefined });
  }
  function handleCity(code: string) {
    onChange({ provinceCode, cityCode: code, districtCode: undefined });
  }
  function handleDistrict(code: string) {
    onChange({ provinceCode, cityCode, districtCode: code });
  }

  return (
    <div className="grid grid-cols-3 gap-2">
      <select
        value={provinceCode ?? ''}
        onChange={(e) => handleProvince(e.target.value)}
        className="input"
      >
        <option value="">省</option>
        {provinces.map((r) => (
          <option key={r.code} value={r.code}>{r.name}</option>
        ))}
      </select>
      <select
        value={cityCode ?? ''}
        onChange={(e) => handleCity(e.target.value)}
        disabled={!provinceCode}
        className="input disabled:opacity-40"
      >
        <option value="">市</option>
        {cities.map((r) => (
          <option key={r.code} value={r.code}>{r.name}</option>
        ))}
      </select>
      <select
        value={districtCode ?? ''}
        onChange={(e) => handleDistrict(e.target.value)}
        disabled={!cityCode}
        className="input disabled:opacity-40"
      >
        <option value="">{requireDistrict ? '区/县' : '区/县（可选）'}</option>
        {districts.map((r) => (
          <option key={r.code} value={r.code}>{r.name}</option>
        ))}
      </select>
    </div>
  );
}

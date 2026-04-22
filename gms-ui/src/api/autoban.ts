import axios from 'axios';

export interface AutobanConfigResult {
  id: number;
  type: string;
  name: string;
  disabled: boolean;
  points: number | null;
  expireTimeSeconds: number | null;
  description: string;
  defaultPoints: number;
  defaultExpireTimeSeconds: number;
  changePoints: boolean;
  changeExpireTime: boolean;
}

export function getAutobanConfigList() {
  return axios.get('/autoban/v1/getConfigList');
}

export function updateAutobanConfig(data: AutobanConfigResult) {
  return axios.post('/autoban/v1/updateConfig', data);
}

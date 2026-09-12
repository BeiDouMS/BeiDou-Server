import axios from 'axios';

export type AutobanLogAction = 'ALERT' | 'POINT' | 'AUTOBAN' | 'DISCONNECT';

export const AUTOBAN_LOG_ACTIONS: AutobanLogAction[] = [
  'ALERT',
  'POINT',
  'AUTOBAN',
  'DISCONNECT',
];

export interface AutobanLogSearchReq {
  pageNo?: number;
  pageSize?: number;
  characterName?: string;
  accountId?: number;
  characterId?: number;
  type?: string;
  action?: string;
  minPoints?: number;
  startTime?: string;
  endTime?: string;
}

export interface AutobanLogSummaryReq {
  startTime?: string;
  endTime?: string;
  limit?: number;
  action?: string;
}

export interface AutobanLogRecord {
  id: number;
  world: number;
  channel: number;
  accountId?: number | null;
  accountName?: string | null;
  characterId?: number | null;
  characterName?: string | null;
  mapId?: number | null;
  ip?: string | null;
  type: string;
  action: AutobanLogAction;
  points?: number | null;
  threshold?: number | null;
  reason?: string | null;
  autoBanEnabled: boolean;
  createTime: string;
}

export interface AutobanLogPage {
  records: AutobanLogRecord[];
  pageNumber: number;
  pageSize: number;
  totalPage: number;
  totalRow: number;
}

export interface AutobanLogSummary {
  characterId?: number | null;
  characterName?: string | null;
  accountId?: number | null;
  accountName?: string | null;
  total: number;
  alertCount: number;
  pointCount: number;
  autobanCount: number;
  disconnectCount: number;
  types?: string | null;
  lastTime?: string | null;
  lastIp?: string | null;
}

export interface AutobanLogTypeOption {
  value: string;
  label: string;
}

export function getAutobanLogList(data: AutobanLogSearchReq) {
  return axios.post<AutobanLogPage>('/autoban/v1/getLogList', data);
}

export function getAutobanLogSummary(data: AutobanLogSummaryReq) {
  return axios.post<AutobanLogSummary[]>('/autoban/v1/getLogSummary', data);
}

export function getAutobanLogTypeOptions() {
  return axios.get<AutobanLogTypeOption[]>('/autoban/v1/getLogTypeOptions');
}

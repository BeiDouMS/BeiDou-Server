import axios from 'axios';
import { PageState } from '@/store/page';

export interface AuditLogSearch {
  pageNo: number;
  pageSize: number;
  operatorName?: string;
  uri?: string;
  operation?: string;
  result?: number;
  startTime?: string;
  endTime?: string;
}

export interface AuditLogResult {
  id: number;
  operatorId: number;
  operatorName: string;
  ip: string;
  httpMethod: string;
  uri: string;
  operation: string;
  params: string;
  result: number;
  errorMsg: string;
  costMs: number;
  createdAt: string;
}

export function getAuditLogList(data: AuditLogSearch) {
  return axios.post<PageState>('/auditlog/v1/list', data);
}

export function getAuditLogOperationOptions() {
  return axios.get<string[]>('/auditlog/v1/operation/options');
}

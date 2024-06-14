import axios from 'axios';
import type { RouteRecordNormalized } from 'vue-router';
import { UserState } from '@/store/modules/user/types';

export interface LoginData {
  username: string;
  password: string;
}

export interface SubmitBody {
  requestId: string;
  data: any;
}

/* eslint-disable no-bitwise */
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export interface LoginRes {
  token: string;
}
export function login(data: LoginData) {
  const submitBody = {
    requestId: generateUUID(),
    data,
  };
  return axios.post<LoginRes>('/auth/v1/login', submitBody);
}

export function logout() {
  return axios.delete<LoginRes>('/auth/v1/logout');
}

export function getUserInfo() {
  return axios.get<UserState>('/account/v1/info');
}

export function getMenuList() {
  return axios.get<RouteRecordNormalized[]>('/account/v1/menu');
}

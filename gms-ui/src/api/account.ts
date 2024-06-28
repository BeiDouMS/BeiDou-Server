import axios from 'axios';
import { isValidString } from '@/utils/stringUtils';
import { PageState } from '@/store/page';

export interface RegisterForm {
  name?: string;
  password?: string;
  checkPassword?: string;
  birthday?: string;
  language?: number;
}

export interface GMUpdateForm {
  newPwd?: string;
  newPwdCheck?: string;
  pin?: string;
  pic?: string;
  birthday?: string;
  nxCredit?: number;
  maplePoint?: number;
  nxPrepaid?: number;
  characterslots?: number;
  gender?: number;
  webadmin?: boolean;
  nick?: string;
  mute?: boolean;
  email?: string;
  rewardpoints?: number;
  votepoints?: number;
  language?: number;
}

export function getAccountList(
  page: number,
  size: number,
  id?: number,
  name?: string,
  lastLoginStart?: string,
  lastLoginEnd?: string,
  createdAtStart?: string,
  createdAtEnd?: string
) {
  let url = `/account/v1?page=${page}&size=${size}`;
  if (id !== undefined && id > 0) url += `&id=${id}`;
  if (isValidString(name)) url += `&name=${name}`;
  if (isValidString(lastLoginStart)) url += `&lastLoginStart=${lastLoginStart}`;
  if (isValidString(lastLoginEnd)) url += `&lastLoginEnd=${lastLoginEnd}`;
  if (isValidString(createdAtStart)) url += `&createdAtStart=${createdAtStart}`;
  if (isValidString(createdAtEnd)) url += `&createdAtEnd=${createdAtEnd}`;
  return axios.get<PageState>(url);
}

export function addAccount(data: RegisterForm) {
  return axios.post('/account/v1', data);
}

export function updateAccountByGM(id: number, data: GMUpdateForm) {
  return axios.put(`/account/v1/${id}`, data);
}

export function deleteAccount(id: number) {
  return axios.delete(`/account/v1/${id}`);
}

export function banAccount(id: number, reason?: string) {
  return axios.put(`/account/v1/${id}/ban`, { reason });
}

export function unbanAccount(id: number) {
  return axios.put(`/account/v1/${id}/unban`);
}

export function resetLoggedIn(id: number) {
  return axios.put(`/account/v1/${id}/reset/logged`);
}

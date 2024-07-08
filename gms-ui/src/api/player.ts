import axios from 'axios';
import { isValidString } from '@/utils/stringUtils';
import { PageState } from '@/store/page';

export interface GiveForm {
  playerId?: number;
  player?: string;
  type: number;
  id?: number;
  quantity?: number;
  rate?: number;
  str?: number;
  dex?: number;
  int?: number;
  luk?: number;
  hp?: number;
  mp?: number;
  pAtk?: number;
  mAtk?: number;
  pDef?: number;
  mDef?: number;
  acc?: number;
  avoid?: number;
  speed?: number;
  jump?: number;
  expire?: number;
}

export function getPlayerList(
  page: number,
  size: number,
  id?: number,
  name?: string,
  map?: number
) {
  let url = `/player/v1?page=${page}&size=${size}`;
  if (id !== undefined && id > 0) url += `&id=${id}`;
  if (isValidString(name)) url += `&name=${name}`;
  if (isValidString(map)) url += `&map=${map}`;
  return axios.get<PageState>(url);
}

export function givePlayerSrc(data: GiveForm) {
  return axios.post(`/player/v1/src`, data);
}

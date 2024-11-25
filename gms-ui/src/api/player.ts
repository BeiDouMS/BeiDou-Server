import axios from 'axios';

export interface GiveForm {
  worldId?: number;
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
  hands?: number;
  speed?: number;
  jump?: number;
  upgradeSlot?: number;
  expire?: number;
}

export function getPlayerList(
  pageNo: number,
  pageSize: number,
  id?: number,
  name?: string,
  map?: number
) {
  return axios.post('/character/v1/online/list', {
    pageNo,
    pageSize,
    id,
    name,
    map,
  });
}

export function givePlayerSrc(data: GiveForm) {
  return axios.post(`/give/v1/resource`, data);
}

export function getEquInitialInfo(id: number) {
  return axios.post(`/common/v1/getEquipmentInfoByItemId`, { id });
}

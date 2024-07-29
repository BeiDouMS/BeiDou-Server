import axios from 'axios';
import { NpcShopItemState } from '@/store/modules/npcShop/type';

export interface getShopFilter {
  pageNo?: number;
  pageSize?: number;
  onlyTotal: boolean;
  notPage: boolean;
  shopId?: number;
  npcId?: number;
  npcName?: string;
  itemId?: number;
  itemName?: string;
}

export function getShopList(data: getShopFilter) {
  return axios.post('/shop/v1/getShopList', data);
}

export function getShopItemList(data: getShopFilter) {
  return axios.post('/shop/v1/getShopItemList', data);
}

export function deleteShopItem(id: number) {
  return axios.delete(`/shop/v1/deleteShopItem/${id}`);
}

export function addShopItem(data: NpcShopItemState) {
  return axios.put(`/shop/v1/addShopItem`, data);
}

export function updateShopItem(data: NpcShopItemState) {
  return axios.post(`/shop/v1/updateShopItem`, data);
}

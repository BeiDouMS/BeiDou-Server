import axios from 'axios';
import { InventoryState } from '@/store/modules/inventory/type';

export interface InventoryCondition {
  inventoryType?: number;
  characterId?: number;
  characterName?: string;
  accountId?: number;
  pageNo: number;
  pageSize: number;
}

export function getInventoryTypeList() {
  return axios.get('/inventory/v1/getInventoryTypeList');
}

export function getCharacterList(condition: InventoryCondition) {
  return axios.post('/inventory/v1/getCharacterList', condition);
}

export function getInventoryList(condition: InventoryCondition) {
  return axios.post('/inventory/v1/getInventoryList', condition);
}

export function updateInventory(data: InventoryState) {
  return axios.post('/inventory/v1/updateInventory', data);
}

export function deleteInventory(data: InventoryState) {
  return axios.post('/inventory/v1/deleteInventory', data);
}

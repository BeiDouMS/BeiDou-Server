import axios from 'axios';

export interface inventoryCondition {
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

export function getCharacterList(condition: inventoryCondition) {
  return axios.post('/inventory/v1/getCharacterList', condition);
}

export function getInventoryList(condition: inventoryCondition) {
  return axios.post('/inventory/v1/getInventoryList', condition);
}

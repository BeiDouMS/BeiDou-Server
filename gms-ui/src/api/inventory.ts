import axios from 'axios';

export function getInventoryTypeList() {
  return axios.get('/inventory/v1/getInventoryTypeList');
}

export function getInventoryList(type: number) {
  return axios.post('/inventory/v1/getInventoryList', { type });
}

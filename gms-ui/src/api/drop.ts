import axios from 'axios';
import { DropState } from '@/store/modules/drop/type';

export interface DropConditionState {
  dropperId?: number;
  continent?: number;
  itemId?: number;
  questId?: number;
  pageNo?: number;
  pageSize?: number;
  onlyTotal?: boolean;
  notPage?: boolean;
}

export function getDrop(data: DropConditionState) {
  return axios.post('/drop/v1/getDropList', data);
}

export function updateDrop(data: DropState) {
  return axios.post('/drop/v1/updateDropData', data);
}

export function insertDrop(data: DropState) {
  return axios.put('/drop/v1/addDropData', data);
}

export function deleteDrop(data: DropState) {
  return axios.delete(`/drop/v1/deleteDropData/${data.id}`);
}

export function getGlobalDrop(data: DropConditionState) {
  return axios.post('/drop/v1/getGlobalDropList', data);
}

export function updateGlobalDrop(data: DropState) {
  return axios.post('/drop/v1/updateGlobalDropData', data);
}

export function insertGlobalDrop(data: DropState) {
  return axios.put('/drop/v1/addGlobalDropData', data);
}

export function deleteGlobalDrop(data: DropState) {
  return axios.delete(`/drop/v1/deleteGlobalDropData/${data.id}`);
}

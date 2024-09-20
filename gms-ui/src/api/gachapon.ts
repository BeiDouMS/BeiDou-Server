import axios from 'axios';
import {
  GachaponPoolState,
  GachaponRewardState,
} from '@/store/modules/gachapon/type';

export interface GachaponPoolSearchCondition {
  gachaponId?: number;
  pageNo: number;
  pageSize: number;
}

export function getPools(condition: GachaponPoolSearchCondition) {
  return axios.post('/gachapon/v1/getPools', condition);
}

export function updatePool(data: GachaponPoolState) {
  return axios.post('/gachapon/v1/updatePool', data);
}

export function deletePool(data: GachaponPoolState) {
  return axios.post('/gachapon/v1/deletePool', data);
}

export function getRewards(condition: GachaponPoolState) {
  return axios.post('/gachapon/v1/getRewards', condition);
}

export function updateReward(data: GachaponRewardState) {
  return axios.post('/gachapon/v1/updateReward', data);
}

export function deleteReward(data: GachaponRewardState) {
  return axios.post('/gachapon/v1/deleteReward', data);
}

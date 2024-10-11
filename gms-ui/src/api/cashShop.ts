import axios from 'axios';
import { cashShopState } from '@/store/modules/cashShop/type';

export interface conditionState {
  id: number;
  subId: number;
  onSale?: number;
  pageNo: number;
  itemId?: number;
}

export interface cashShopFormState {
  sn: number;
  itemId: number;
  count?: number;
  price?: number;
  bonus?: number;
  priority?: number;
  period?: number;
  maplePoint?: number;
  meso?: number;
  forPremiumUser?: number;
  commodityGender?: number;
  onSale?: number;
  clz?: number;
  limit?: number;
  pbCash?: number;
  pbPoint?: number;
  pbGift?: number;
  packageSn?: number;
}

export interface batchFormState {
  data: cashShopState[];
  type: string;
  value?: number;
}

export function getAllCategoryList() {
  return axios.get('/cashShop/v1/getAllCategoryList');
}

export function getCommodityByCategory(condition: conditionState) {
  return axios.post('/cashShop/v1/getCommodityByCategory', condition);
}

export function onSale(data: cashShopFormState) {
  return axios.post('/cashShop/v1/onSale', data);
}

export function offSale(data: cashShopFormState) {
  return axios.post('/cashShop/v1/offSale', data);
}

export function batchOnSale(data: batchFormState) {
  return axios.post('/cashShop/v1/batchOnSale', data);
}

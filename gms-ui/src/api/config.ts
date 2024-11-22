import axios from 'axios';
import { RequestOption } from '@arco-design/web-vue';

export interface ConfigSearch {
  type: string;
  subType: string;
  filter: string;
  pageNo: number;
  pageSize: number;
}

export interface ConfigResult {
  id: number;
  configType: string;
  configSubType: string;
  configClazz: string;
  configCode: string;
  configValue: string;
  configDesc: string;
}

export function getConfigTypeList() {
  return axios.get('/config/v1/getConfigTypeList');
}

export function getConfigList(data: ConfigSearch) {
  return axios.post('/config/v1/getConfigList', data);
}

export function addConfig(data: ConfigResult) {
  return axios.post('/config/v1/addConfig', data);
}

export function updateConfig(data: ConfigResult) {
  return axios.post('/config/v1/updateConfig', data);
}

export function deleteConfig(id: number) {
  return axios.delete(`/config/v1/deleteConfig/${id}`);
}

export function deleteConfigList(ids: number[]) {
  return axios.post(`/config/v1/deleteConfigList`, ids);
}

export function importYml(option: RequestOption) {
  const formData = new FormData();
  formData.append('file', option.fileItem.file as Blob);
  return axios
    .post(option.action as string, formData, {
      headers: { 'Content-type': 'multipart/form-data' },
    })
    .then((response) => {
      option.onSuccess(response);
    });
}

export function exportYml() {
  return axios.get(`/config/v1/exportYml`, {
    responseType: 'blob',
  });
}

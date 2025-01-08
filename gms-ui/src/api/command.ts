import axios from 'axios';

export interface CommandReq {
  id?: number;
  level?: number;
  levelList?: number[];
  syntax?: string;
  defaultLevel?: number;
  defaultLevelList?: number[];
  clazz?: string;
  description?: string;
  enabled?: boolean;
}

export function getCommandList(data: any) {
  return axios.post('/command/v1/getCommandListFromDB', data);
}

export function updateCommand(data: CommandReq) {
  return axios.post('/command/v1/updateCommand', data);
}

export function reloadEventsByGMCommand() {
  return axios.get('/command/v1/reloadEventsByGMCommand');
}

export function reloadPortalsByGMCommand() {
  return axios.get('/command/v1/reloadPortalsByGMCommand');
}

export function reloadMapsByGMCommand() {
  return axios.get('/command/v1/reloadMapsByGMCommand');
}

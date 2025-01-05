import axios from 'axios';

export function getServerStatus() {
  return axios.get<boolean>('/server/v1/online');
}

export function startServer() {
  return axios.get('/server/v1/startServer');
}

interface StopServerParams {
  minutes: number;
  shutdownMsg: string;
  showServerMsg: boolean;
  showCenterMsg: boolean;
  showChatMsg: boolean;
}

export function stopServer(params: StopServerParams) {
  return axios.post('/server/v1/stopServerWithMsgAndInternal', params);
}

export function restartServer() {
  return axios.get('/server/v1/restartServer');
}

export function shutdown() {
  return axios.get('/server/v1/shutdown');
}

export function getVersion() {
  return axios.get('/server/v1/version');
}

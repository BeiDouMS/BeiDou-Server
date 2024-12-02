import axios from 'axios';

export function getServerStatus() {
  return axios.get<boolean>('/server/v1/online');
}

export function startServer() {
  return axios.get('/server/v1/startServer');
}

export function stopServer() {
  return axios.get('/server/v1/stopServer');
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

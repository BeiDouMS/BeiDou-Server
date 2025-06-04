import axios from 'axios';

export interface FileTreeForm {
  currentKey: string;
}

export interface ReadForm {
  currentKey: string;
  title: string;
}
export interface WriteForm {
  currentKey: string;
  title: string;
  content: string;
}

export function treeFile(data: FileTreeForm) {
  return axios.post('/file/v1/tree', data);
}

export function readFile(data: ReadForm) {
  return axios.post('/file/v1/tree/read', data);
}

export function writeFile(data: WriteForm) {
  return axios.post('/file/v1/tree/write', data);
}

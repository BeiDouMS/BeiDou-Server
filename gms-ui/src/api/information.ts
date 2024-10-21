import axios from 'axios';

export interface InformationSearch {
  types: [];
  filter: string;
}

export interface InformationResult {
  type: string;
  id: number;
  name: string;
  desc: string;
}

export function informationSearch(condition: InformationSearch) {
  return axios.post('/common/v1/informationSearch', condition);
}

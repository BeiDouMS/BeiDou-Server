import axios from 'axios';

export interface CharacterListItem {
  id: number;
  name: string;
  job: number;
  jobName: string;
  level: number;
  world: number;
  worldName: string;
  gm: number;
  meso: number;
  fame: number;
  guildid: number;
  createdate: string;
  lastLogoutTime: string;
  online: boolean;
}

export function getAccountCharacters(accountId: number) {
  return axios.get<CharacterListItem[]>(`/character/v1/account/${accountId}`);
}

export function deleteCharacter(cid: number) {
  return axios.delete(`/character/v1/${cid}`);
}

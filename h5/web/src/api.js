const TOKEN_KEY = 'h5.token';
const ACCOUNT_KEY = 'h5.account';
const CHARACTER_KEY = 'h5.character';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function setSession(token, account) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(ACCOUNT_KEY, JSON.stringify(account || null));
}

export function getAccount() {
  try {
    return JSON.parse(localStorage.getItem(ACCOUNT_KEY) || 'null');
  } catch {
    return null;
  }
}

export function getSavedCharacter() {
  try {
    return JSON.parse(localStorage.getItem(CHARACTER_KEY) || 'null');
  } catch {
    return null;
  }
}

export function saveCharacter(character) {
  if (character) localStorage.setItem(CHARACTER_KEY, JSON.stringify(character));
  else localStorage.removeItem(CHARACTER_KEY);
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(ACCOUNT_KEY);
  localStorage.removeItem(CHARACTER_KEY);
}

async function request(path, body) {
  const resp = await fetch(`/api/${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
    },
    body: JSON.stringify(body || {}),
  });
  let json = null;
  try {
    json = await resp.json();
  } catch {
    throw new Error(`服务返回异常（HTTP ${resp.status}）`);
  }
  if (resp.status === 401) {
    clearSession();
    window.location.reload();
    throw new Error(json?.message || '登录已过期，请重新登录');
  }
  if (!resp.ok || !json.ok) throw new Error(json?.message || '请求失败');
  return json.data === undefined ? json : json.data;
}

async function requestImage(path) {
  const resp = await fetch(`/api/${path}`, {
    headers: getToken() ? { Authorization: `Bearer ${getToken()}` } : {},
  });
  if (resp.status === 401) {
    clearSession();
    window.location.reload();
    throw new Error('登录已过期，请重新登录');
  }
  if (!resp.ok) throw new Error(`图片读取失败（HTTP ${resp.status}）`);
  return URL.createObjectURL(await resp.blob());
}

export const api = {
  login: (username, password) => request('login', { username, password }),
  logout: () => request('logout'),
  characters: () => request('characters'),
  characterInfo: (characterId) => request('character/info', { characterId }),
  /** 纸娃娃形象：返回 blob objectURL（图片请求无法带 Authorization 头，这里手动拉取） */
  avatar: (characterId) => requestImage(`avatar/${characterId}`),
  maps: (characterId) => request('maps', { characterId }),
  mapDetail: (characterId, mapId) => request('map/detail', { characterId, mapId }),
  drops: (characterId, mapId) => request('drops', { characterId, mapId }),
  selectMap: (characterId, mapId) => request('map/select', { characterId, mapId }),
  saveWhitelist: (characterId, mapId, itemIds) => request('whitelist', { characterId, mapId, itemIds }),
  state: (characterId) => request('state', { characterId }),
  start: (characterId, mapId) => request('start', { characterId, mapId }),
  progress: (characterId) => request('progress', { characterId }),
  settle: (characterId, mapId) => request('settle', { characterId, mapId }),
  claim: (characterId) => request('claim', { characterId }),
  discardRewardItems: (characterId, itemIds) => request('reward/discard', { characterId, itemIds }),
};

export function itemIconUrl(itemId) {
  // v2：修复装备图标按前缀错配 + UOL 图标失效，用版本参数让浏览器丢弃旧缓存
  return `/api/item-icon/${itemId}?v=2`;
}

export function mapThumbUrl(mapId) {
  return `/api/map-thumb/${mapId}?v=1`;
}

export function mobIconUrl(mobId) {
  return `/api/mob-icon/${mobId}?v=1`;
}

export function formatNumber(value) {
  const n = Number(value || 0);
  if (n >= 100000000) return `${(n / 100000000).toFixed(2)}亿`;
  if (n >= 10000) return `${(n / 10000).toFixed(1)}万`;
  return String(Math.round(n));
}

export function formatDuration(seconds) {
  const s = Math.max(0, Math.floor(Number(seconds || 0)));
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  if (h > 0) return `${h}时${m}分`;
  if (m > 0) return `${m}分`;
  return `${s}秒`;
}

export function jobName(job) {
  const j = Number(job || 0);
  if (j === 0) return '初学者';
  const first = Math.floor(j / 100);
  const names = {
    100: '战士',
    200: '法师',
    300: '弓箭手',
    400: '飞侠',
    500: '海盗',
  };
  if (j >= 1000) {
    if (j === 2000) return '管理员';
  }
  return names[first] || `职业${j}`;
}

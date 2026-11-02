/**
 * H5 挂机后端。
 * - 账号登录（校验 accounts 表的 bcrypt 密码）
 * - 会话 token（内存）
 * - 转发游戏服务端的 /h5/v1/* 权威接口（持有 h5.secret）
 * - 按需从客户端 WZ 提取物品图标
 */
import express from 'express';
import compression from 'compression';
import bcrypt from 'bcryptjs';
import crypto from 'crypto';
import path from 'node:path';
import fs from 'node:fs';
import { fileURLToPath } from 'node:url';
import mysql from 'mysql2/promise';
import { getItemIcon } from './wz-icons.js';
import { getMapThumb, getMobIcon } from './wz-map.js';
import { getAvatarPng } from './wz-avatar.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const H5_ROOT = path.resolve(__dirname, '../..');

/** 纸娃娃合成逻辑版本号：升级合成规则时递增，用于让浏览器缓存失效 */
const AVATAR_RENDER_VERSION = 3;

const CONFIG = {
  port: Number(process.env.PORT || 8090),
  gameApi: process.env.GAME_API || 'http://127.0.0.1:8686/h5/v1',
  secret: process.env.H5_SECRET || 'beidou-h5-hangup-secret',
  db: {
    host: process.env.DB_HOST || '127.0.0.1',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASS || 'root',
    database: process.env.DB_NAME || 'beidou',
    charset: 'utf8mb4',
  },
  sessionTtlMs: 7 * 24 * 3600 * 1000,
  webDist: path.join(H5_ROOT, 'web/dist'),
  assets: path.join(H5_ROOT, 'data/assets'),
};

const pool = mysql.createPool({ ...CONFIG.db, connectionLimit: 8, waitForConnections: true });
const sessions = new Map();

const app = express();
app.use(compression());
app.use(express.json({ limit: '256kb' }));
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'content-type, authorization');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,OPTIONS');
  if (req.method === 'OPTIONS') return res.sendStatus(204);
  next();
});

app.use((req, res, next) => {
  if (!req.path.startsWith('/api/')) return next();
  const started = Date.now();
  res.on('finish', () => {
    console.log(`[h5] ${req.method} ${req.path} ${res.statusCode} ${Date.now() - started}ms`);
  });
  next();
});

function issueToken(account) {
  const token = crypto.randomBytes(24).toString('hex');
  sessions.set(token, { accountId: account.id, name: account.name, expiresAt: Date.now() + CONFIG.sessionTtlMs });
  return token;
}

function auth(req, res, next) {
  const token = (req.headers.authorization || '').replace(/^Bearer\s+/i, '');
  const session = sessions.get(token);
  if (!session || session.expiresAt < Date.now()) {
    return res.status(401).json({ ok: false, message: '登录已过期，请重新登录' });
  }
  req.session = session;
  next();
}

async function callGame(action, data) {
  const resp = await fetch(`${CONFIG.gameApi}/${action}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-H5-Secret': CONFIG.secret },
    body: JSON.stringify({ data }),
  });
  const json = await resp.json();
  if (!resp.ok) throw new Error(`游戏服返回 HTTP ${resp.status}`);
  // ResultBody: code 20000 表示成功
  if (json.code !== 20000) throw new Error(json.message || '游戏服处理失败');
  return json.data;
}

/** 校验角色归属，避免越权操作他人角色 */
async function assertOwned(accountId, characterId) {
  const id = Number(characterId);
  if (!Number.isInteger(id) || id <= 0) throw new Error('角色参数错误');
  const [rows] = await pool.query('SELECT id FROM characters WHERE id = ? AND accountid = ?', [id, accountId]);
  if (rows.length === 0) throw new Error('角色不存在或不属于当前账号');
  return id;
}

const wrap = (fn) => (req, res) => {
  Promise.resolve(fn(req, res)).catch((err) => {
    console.warn('[h5]', req.path, err.message);
    res.status(400).json({ ok: false, message: err.message || '请求失败' });
  });
};

app.post('/api/login', wrap(async (req, res) => {
  const { username, password } = req.body || {};
  if (!username || !password) throw new Error('请输入账号和密码');
  const [rows] = await pool.query('SELECT id, name, password, banned, banreason FROM accounts WHERE name = ?', [username]);
  if (rows.length === 0) throw new Error('账号或密码错误');
  const account = rows[0];
  if (account.banned) throw new Error(account.banreason || '账号已被封禁');
  // 北斗使用 $2y$ 前缀的 bcrypt，bcryptjs 用 $2b$ 校验
  const hash = String(account.password).replace(/^\$2y\$/, '$2b$');
  if (!bcrypt.compareSync(password, hash)) throw new Error('账号或密码错误');
  res.json({ ok: true, token: issueToken(account), account: { id: account.id, name: account.name } });
}));

app.post('/api/logout', auth, (req, res) => {
  const token = (req.headers.authorization || '').replace(/^Bearer\s+/i, '');
  sessions.delete(token);
  res.json({ ok: true });
});

app.post('/api/characters', auth, wrap(async (req, res) => {
  const data = await callGame('characters', { accountId: req.session.accountId });
  for (const chr of data) {
    await assertOwned(req.session.accountId, chr.characterId);
  }
  res.json({ ok: true, data });
}));

app.post('/api/character/info', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  res.json({ ok: true, data: await callGame('character/info', { characterId }) });
}));

app.post('/api/maps', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  res.json({ ok: true, data: await callGame('maps', { characterId }) });
}));

app.post('/api/map/detail', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  const mapId = Number(req.body?.mapId);
  if (!mapId) throw new Error('请选择地图');
  res.json({ ok: true, data: await callGame('map/detail', { characterId, mapId }) });
}));

app.post('/api/map/select', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  const mapId = Number(req.body?.mapId);
  if (!mapId) throw new Error('请选择地图');
  res.json({ ok: true, data: await callGame('map/select', { characterId, mapId }) });
}));

app.post('/api/drops', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  const mapId = Number(req.body?.mapId) || 0;
  res.json({ ok: true, data: await callGame('drops', { characterId, mapId }) });
}));

app.post('/api/whitelist', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  const mapId = Number(req.body?.mapId) || 0;
  const itemIds = Array.isArray(req.body?.itemIds) ? req.body.itemIds.map(Number).filter((v) => v > 0) : [];
  // 勾选数量不设上限
  res.json({ ok: true, data: await callGame('whitelist/save', { characterId, mapId, itemIds }) });
}));

app.post('/api/state', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  res.json({ ok: true, data: await callGame('state', { characterId }) });
}));

app.post('/api/start', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  const mapId = Number(req.body?.mapId) || undefined;
  res.json({ ok: true, data: await callGame('start', { characterId, mapId }) });
}));

app.post('/api/progress', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  res.json({ ok: true, data: await callGame('progress', { characterId }) });
}));

app.post('/api/settle', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  const mapId = Number(req.body?.mapId) || undefined;
  res.json({ ok: true, data: await callGame('settle', { characterId, mapId }) });
}));

app.post('/api/claim', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  res.json({ ok: true, data: await callGame('claim', { characterId }) });
}));

app.post('/api/reward/discard', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.body?.characterId);
  const itemIds = (req.body?.itemIds || []).map(Number).filter((id) => Number.isInteger(id) && id > 0);
  res.json({ ok: true, data: await callGame('reward/discard', { characterId, itemIds }) });
}));

/** 角色纸娃娃形象：按 skincolor/hair/face + 已穿戴装备合成 PNG */
app.get('/api/avatar/:characterId', auth, wrap(async (req, res) => {
  const characterId = await assertOwned(req.session.accountId, req.params.characterId);
  const [chrRows] = await pool.query('SELECT skincolor, hair, face FROM characters WHERE id = ?', [characterId]);
  const chr = chrRows[0];
  if (!chr) return res.sendStatus(404);
  const [itemRows] = await pool.query(
    'SELECT itemid FROM inventoryitems WHERE characterid = ? AND inventorytype IN (1,-1) AND position < 0',
    [characterId]
  );
  const items = itemRows.map((row) => Number(row.itemid)).filter((id) => id > 0);
  const spec = { skin: Number(chr.skincolor) || 0, hair: Number(chr.hair) || 30000, face: Number(chr.face) || 20000, items };
  // 外观未变化时直接 304，避免每次进页面都重新合成；带渲染版本号，合成逻辑升级后强制重新取图
  const etag = `"${crypto.createHash('md5').update(JSON.stringify({ v: AVATAR_RENDER_VERSION, spec })).digest('hex').slice(0, 16)}"`;
  res.setHeader('ETag', etag);
  res.setHeader('Cache-Control', 'private, no-cache');
  if (req.headers['if-none-match'] === etag) return res.status(304).end();
  const png = await getAvatarPng(spec);
  if (!png) return res.sendStatus(404);
  res.setHeader('Content-Type', 'image/png');
  res.send(png);
}));

app.get('/api/item-icon/:itemId', wrap(async (req, res) => {
  const itemId = Number(req.params.itemId);
  const png = await getItemIcon(itemId);
  if (!png) return res.sendStatus(404);
  res.setHeader('Content-Type', 'image/png');
  res.setHeader('Cache-Control', 'public, max-age=604800');
  res.send(png);
}));

app.get('/api/map-thumb/:mapId', wrap(async (req, res) => {
  const mapId = Number(req.params.mapId);
  const png = await getMapThumb(mapId);
  if (!png) return res.sendStatus(404);
  res.setHeader('Content-Type', 'image/png');
  res.setHeader('Cache-Control', 'public, max-age=604800');
  res.send(png);
}));

app.get('/api/mob-icon/:mobId', wrap(async (req, res) => {
  const mobId = Number(req.params.mobId);
  const png = await getMobIcon(mobId);
  if (!png) return res.sendStatus(404);
  res.setHeader('Content-Type', 'image/png');
  res.setHeader('Cache-Control', 'public, max-age=604800');
  res.send(png);
}));

app.get('/api/health', (req, res) => res.json({ ok: true, now: Date.now() }));

app.use('/assets', express.static(CONFIG.assets, { maxAge: '7d' }));
if (fs.existsSync(CONFIG.webDist)) {
  app.use(express.static(CONFIG.webDist, {
    maxAge: '1h',
    setHeaders: (res, filePath) => {
      // 入口 HTML 不缓存，保证发版后拿到新的 hash 资源
      if (filePath.endsWith('.html')) res.setHeader('Cache-Control', 'no-cache');
    },
  }));
  app.get('*', (req, res, next) => {
    if (req.path.startsWith('/api/')) return next();
    res.sendFile(path.join(CONFIG.webDist, 'index.html'));
  });
}

app.listen(CONFIG.port, () => {
  console.log(`[h5] http://127.0.0.1:${CONFIG.port}  game=${CONFIG.gameApi}`);
});

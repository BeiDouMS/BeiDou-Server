/**
 * 按需从客户端 WZ 提取地图缩略图与怪物图像（PNG），带缓存。
 * - 地图缩略图：Map/Map/Map{n}/{pad9(mapId)}.img → miniMap/canvas
 * - 怪物图像：Mob/{pad7(mobId)}.img → 动作第一帧（stand/fly/move/...），info/link 指向的实体怪优先
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import * as wz from '@tybys/wz';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DATA_DIR = path.resolve(__dirname, '../../../BeiDou-ClientV17/BeiDou-Client/Data');

const pngCache = new Map();
const fileCache = new Map();
const MAX_FILES = 24;
const MOB_ACTIONS = ['stand', 'fly', 'move', 'jump', 'attack1', 'attack', 'hit1', 'regen'];

function pad(num, len) {
  return String(num).padStart(len, '0');
}

let initialized = false;

async function loadImage(fileRel) {
  if (fileCache.has(fileRel)) {
    const entry = fileCache.get(fileRel);
    fileCache.delete(fileRel);
    fileCache.set(fileRel, entry);
    return entry;
  }
  const full = path.join(DATA_DIR, fileRel);
  if (!fs.existsSync(full)) return null;
  if (!initialized) {
    await wz.init();
    initialized = true;
  }
  const img = wz.WzImage.createFromFile(full, wz.WzMapleVersion.GMS);
  const ok = await img.parseImage(true);
  if (!ok) return null;
  const entry = { img };
  fileCache.set(fileRel, entry);
  while (fileCache.size > MAX_FILES) {
    const oldestKey = fileCache.keys().next().value;
    fileCache.delete(oldestKey);
  }
  return entry;
}

async function canvasToPng(canvas) {
  if (!canvas) return null;
  const bmp = await canvas.getBitmap();
  if (!bmp) return null;
  return Buffer.from(await bmp.getBufferAsync('image/png'));
}

function firstCanvas(node) {
  if (!node) return null;
  if (node.constructor?.name === 'WzCanvasProperty') return node;
  try {
    for (const child of node.wzProperties) {
      const found = firstCanvas(child);
      if (found) return found;
    }
  } catch { /* not iterable */ }
  return null;
}

function cachePut(key, buf) {
  if (pngCache.size > 3000) pngCache.clear();
  pngCache.set(key, buf);
}

/** 地图缩略图：miniMap/canvas（部分地图没有，返回 null 走占位） */
export async function getMapThumb(mapId) {
  const id = Number(mapId);
  if (!Number.isInteger(id) || id <= 0) return null;
  const key = `map:${id}`;
  if (pngCache.has(key)) return pngCache.get(key);

  const pad9 = pad(id, 9);
  const fileRel = `Map/Map/Map${pad9[0]}/${pad9}.img`;
  const entry = await loadImage(fileRel);
  if (!entry) return null;
  try {
    const miniMap = entry.img.at('miniMap');
    // v83 结构：miniMap/canvas（部分为 miniMap 直接遍历）
    const canvas = (miniMap && (miniMap.at('canvas') || firstCanvas(miniMap))) || firstCanvas(entry.img.at('miniMap'));
    const buf = await canvasToPng(canvas);
    if (buf) cachePut(key, buf);
    return buf;
  } catch {
    return null;
  }
}

/** 怪物图像：动作第一帧，info/link 链接的实体怪优先 */
export async function getMobIcon(mobId) {
  const id = Number(mobId);
  if (!Number.isInteger(id) || id <= 0) return null;
  const key = `mob:${id}`;
  if (pngCache.has(key)) return pngCache.get(key);
  const buf = await loadMobPng(id, 0);
  if (buf) cachePut(key, buf);
  return buf;
}

async function loadMobPng(mobId, depth) {
  const entry = await loadImage(`Mob/${pad(mobId, 7)}.img`);
  if (!entry) return null;
  try {
    // link：解析到真实怪物 img（部分 boss/变身怪共享形象）
    if (depth < 3) {
      const info = entry.img.at('info');
      const link = info && info.at('link');
      if (link) {
        const linkId = Number(link.toString().replace(/[^0-9]/g, ''));
        if (linkId > 0 && linkId !== mobId) {
          const linked = await loadMobPng(linkId, depth + 1);
          if (linked) return linked;
        }
      }
    }
    // 依次尝试常见动作节点，取第一帧 canvas
    for (const act of MOB_ACTIONS) {
      const actNode = entry.img.at(act);
      if (!actNode) continue;
      const canvas = actNode.at('0') || firstCanvas(actNode);
      const buf = await canvasToPng(canvas);
      if (buf) return buf;
    }
    // 兜底：全树找第一个 canvas
    return await canvasToPng(firstCanvas(entry.img));
  } catch {
    return null;
  }
}

/**
 * 按需从客户端 WZ 提取物品图标（PNG），带文件级缓存。
 * 普通物品走 Item.wz，装备（1xxxxxx）走 Character.wz。
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import * as wz from '@tybys/wz';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DATA_DIR = path.resolve(__dirname, '../../../BeiDou-ClientV17/BeiDou-Client/Data');

const CATEGORY_DIR = { 2: 'Consume', 3: 'Install', 4: 'Etc', 5: 'Cash', 9: 'Special' };
const iconCache = new Map();
const fileCache = new Map();
const MAX_FILES = 24;
let initialized = false;
let equipIndex = null;

function pad(num, len) {
  return String(num).padStart(len, '0');
}

/** 建立“装备ID → 所在 img”索引，例如 1382007 → Character/Weapon/01382007.img。
 *  Character.wz 中每件装备一个 img，文件名即 8 位 itemId，必须精确匹配，
 *  否则同前缀（如 0138）的几十件装备会全部显示成第一件的图标。 */
function equipFileRel(itemId) {
  if (!equipIndex) {
    equipIndex = new Map();
    const root = path.join(DATA_DIR, 'Character');
    let dirs = [];
    try {
      dirs = fs.readdirSync(root);
    } catch {
      return null;
    }
    for (const dir of dirs) {
      // 特效目录不含可穿戴道具
      if (dir === 'Afterimage') continue;
      const abs = path.join(root, dir);
      let names = [];
      try {
        names = fs.readdirSync(abs);
      } catch {
        continue;
      }
      for (const name of names) {
        if (!/^01\d{6}\.img$/.test(name)) continue;
        equipIndex.set(name.slice(0, 8), `Character/${dir}/${name}`);
      }
    }
  }
  return equipIndex.get(pad(itemId, 8)) || null;
}

async function loadCategoryFile(fileRel) {
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
    try { fileCache.get(oldestKey).img.dispose(); } catch { /* ignore */ }
    fileCache.delete(oldestKey);
  }
  return entry;
}

export async function getItemIcon(itemId) {
  if (!Number.isInteger(itemId) || itemId <= 0) return null;
  if (iconCache.has(itemId)) return iconCache.get(itemId);

  // 装备：Character.wz 内每件装备一个 img，图标在根节点 info/icon
  if (Math.floor(itemId / 1000000) === 1) {
    const equipRel = equipFileRel(itemId);
    if (!equipRel) return null;
    const equipEntry = await loadCategoryFile(equipRel);
    if (!equipEntry) return null;
    return iconToBuffer(findIconNode(equipEntry.img), itemId);
  }

  const dir = CATEGORY_DIR[Math.floor(itemId / 1000000)];
  if (!dir) return null;
  const fileRel = `Item/${dir}/${pad(Math.floor(itemId / 10000), 4)}.img`;
  const entry = await loadCategoryFile(fileRel);
  if (!entry) return null;

  const node = entry.img.at(pad(itemId, 8));
  if (!node) return null;
  return iconToBuffer(findIconNode(node), itemId);
}

/** 图标节点：优先 info/icon，其次 info/iconRaw，再退回根节点上的 icon/iconRaw */
function findIconNode(node) {
  if (!node) return null;
  const info = node.at('info');
  const fromInfo = info && (info.at('icon') || info.at('iconRaw'));
  return fromInfo || node.at('icon') || node.at('iconRaw') || null;
}

async function iconToBuffer(iconNode, itemId) {
  if (!iconNode) return null;
  try {
    const bmp = await iconNode.getBitmap();
    if (!bmp) return null;
    // 部分道具的 icon 是 UOL 链接（如 ../../02040002/info/icon），指向同一张共享 canvas，
    // 这里不能 dispose，否则后续链接到该 canvas 的道具会取不到图。
    const buf = Buffer.from(await bmp.getBufferAsync('image/png'));
    if (iconCache.size > 2000) iconCache.clear();
    iconCache.set(itemId, buf);
    return buf;
  } catch {
    return null;
  }
}

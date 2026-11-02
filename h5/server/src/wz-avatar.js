/**
 * 纸娃娃（角色形象）合成。
 *
 * 客户端 Character.wz 中每个部件是一张带 `origin`（自身锚点）和 `map`（挂点向量，
 * 相对 origin）的 canvas。合成时不能用统一的 origin 对齐，而要按挂点对齐：
 *   body 放原点 → arm 的 navel 对准 body 的 navel
 *              → head 的 neck 对准 body 的 neck
 *              → hair / face / cap / 面饰 的 brow 对准 head 的 brow
 *              → coat / pants / shoes / glove / cape / shield 的 navel 对准 body 的 navel
 *              → weapon 的 hand 对准 arm 的 hand
 * 图层顺序由 canvas 自带的 `z` 字符串决定。
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import * as wz from '@tybys/wz';
import Jimp from 'jimp';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DATA_DIR = path.resolve(__dirname, '../../../../BeiDou-ClientV17/BeiDou-Client/Data');

/** 参与合成的装备目录（Character.wz 子目录名） */
const EQUIP_DIRS = ['Cap', 'Accessory', 'Coat', 'Longcoat', 'Pants', 'Shoes', 'Glove', 'Cape', 'Shield', 'Weapon'];

/** 图层 z 排序表（内置兜底，优先使用客户端 Data/zmap.img 的官方顺序） */
const Z_ORDER_FALLBACK = {
  backHairBelowCap: 1,
  backHead: 2,
  backHair: 3,
  capeBelowBody: 4,
  shieldBelowBody: 5,
  capAccessoryBelowBody: 6,
  hairBelowBody: 7,
  cape: 8,
  body: 10,
  pants: 12,
  shoes: 14,
  armBelowHead: 15,
  arm: 16,
  head: 20,
  face: 22,
  hair: 30,
  hairOverHead: 31,
  hairShade: 32,
  mailChestOverPants: 33,
  mailChest: 34,
  mailChestOverHighest: 35,
  mail: 36,
  mailArm: 38,
  mailArmOverHair: 39,
  gloveOverBody: 40,
  glove: 41,
  shield: 42,
  weapon: 50,
  weaponOverHair: 51,
  armOverHair: 52,
  cap: 60,
  capAccessory: 61,
  accessoryFace: 62,
  accessoryEye: 63,
  accessoryEar: 64,
};

const PAD_TOP = 6;
const PAD_BOTTOM = 10;
const MIN_WIDTH = 60;

const fileCache = new Map();
const MAX_FILES = 40;
const pngCache = new Map();
const MAX_PNG = 200;

let initialized = false;
let equipIndex = null;
/** z 值 → 层级序号（越大越靠前），来自客户端 Data/zmap.img */
let zOrder = null;

function pad8(id) {
  return String(id).padStart(8, '0');
}

async function ensureInit() {
  if (!initialized) {
    await wz.init();
    initialized = true;
  }
}

/** 读取客户端 zmap.img：属性顺序即从最前图层到最底层，反转后作为绘制顺序 */
async function loadZOrder() {
  if (zOrder) return zOrder;
  zOrder = new Map();
  const full = path.join(DATA_DIR, 'zmap.img');
  try {
    await ensureInit();
    const img = wz.WzImage.createFromFile(full, wz.WzMapleVersion.GMS);
    if (await img.parseImage(true)) {
      const names = Array.from(img.wzProperties || []).map((p) => p.name);
      const total = names.length;
      names.forEach((name, i) => zOrder.set(name, total - i));
      try {
        img.dispose();
      } catch {
        /* ignore */
      }
    }
  } catch {
    /* 读取失败时退回内置排序表 */
  }
  return zOrder;
}

function zRank(z) {
  return zOrder?.get(z) ?? Z_ORDER_FALLBACK[z] ?? 20;
}

/** 装备 itemId → { dir, rel }，只在参与合成的目录内建立索引 */
function equipFile(itemId) {
  if (!equipIndex) {
    equipIndex = new Map();
    for (const dir of EQUIP_DIRS) {
      const abs = path.join(DATA_DIR, 'Character', dir);
      let names = [];
      try {
        names = fs.readdirSync(abs);
      } catch {
        continue;
      }
      for (const name of names) {
        if (!/^01\d{6}\.img$/.test(name)) continue;
        equipIndex.set(name.slice(0, 8), { dir, rel: `Character/${dir}/${name}` });
      }
    }
  }
  return equipIndex.get(pad8(itemId)) || null;
}

async function loadImage(rel) {
  const cached = fileCache.get(rel);
  if (cached) {
    fileCache.delete(rel);
    fileCache.set(rel, cached);
    return cached;
  }
  const full = path.join(DATA_DIR, rel);
  if (!fs.existsSync(full)) return null;
  await ensureInit();
  const img = wz.WzImage.createFromFile(full, wz.WzMapleVersion.GMS);
  const ok = await img.parseImage(true);
  if (!ok) return null;
  fileCache.set(rel, img);
  while (fileCache.size > MAX_FILES) {
    const oldest = fileCache.keys().next().value;
    try {
      fileCache.get(oldest).dispose();
    } catch {
      /* ignore */
    }
    fileCache.delete(oldest);
  }
  return img;
}

function unwrap(node) {
  let cur = node;
  for (let i = 0; i < 8 && cur; i++) {
    if (cur.constructor.name !== 'WzUOLProperty') return cur;
    cur = cur.linkValue;
  }
  return cur;
}

function num(node) {
  if (!node) return 0;
  return typeof node.value === 'number' ? node.value : 0;
}

/** canvas 的挂点 → 世界坐标（相对 canvas 放置点） */
function attachPoints(canvas) {
  const pts = {};
  const origin = canvas.at('origin');
  if (!origin) return pts;
  const ox = num(origin.x);
  const oy = num(origin.y);
  const map = canvas.at('map');
  for (const v of Array.from(map?.wzProperties || [])) {
    pts[v.name] = { x: ox + num(v.x), y: oy + num(v.y) };
  }
  return pts;
}

/** 取某个动作第 frame 帧下的所有 canvas */
function frameCanvases(img, action, frame = 0) {
  if (!img) return [];
  const act = img.at(action) || img.at('default');
  if (!act) return [];
  const fr = act.at(String(frame)) || act;
  const out = [];
  const collect = (node, depth) => {
    if (depth > 3) return;
    if (node.constructor.name === 'WzCanvasProperty') {
      out.push(node);
      return;
    }
    for (const p of Array.from(node.wzProperties || [])) {
      const real = unwrap(p);
      if (real && real.constructor.name === 'WzCanvasProperty') out.push(real);
      else if (p.constructor.name === 'WzSubProperty' && p.name !== 'hairShade') collect(p, depth + 1);
    }
  };
  collect(fr, 0);
  return out;
}

function canvasZ(canvas, fallback) {
  const z = canvas.at('z')?.value;
  return typeof z === 'string' && z ? z : fallback;
}

function pickPoint(canvas, names) {
  const pts = attachPoints(canvas);
  for (const name of names) {
    if (pts[name]) return pts[name];
  }
  return null;
}

function layerPos(canvas, anchorNames, target) {
  const own = pickPoint(canvas, anchorNames);
  if (!own) return null;
  return { x: target.x - own.x, y: target.y - own.y };
}

function worldPoint(pos, canvas, anchorNames) {
  const own = pickPoint(canvas, anchorNames);
  return own ? { x: pos.x + own.x, y: pos.y + own.y } : null;
}

function push(layers, block, canvas, pos, fallbackZ) {
  if (pos) layers.push({ block, canvas, x: pos.x, y: pos.y, z: canvasZ(canvas, fallbackZ) });
}

/**
 * 收集所有图层并计算放置坐标。
 * @param {{skin:number, hair:number, face:number, items:number[]}} spec
 */
async function buildLayers(spec) {
  const skin = Number.isInteger(spec.skin) ? spec.skin : 0;
  const layers = [];

  // 1) 身体 + 手臂
  const bodyImg = await loadImage(`Character/${pad8(2000 + skin)}.img`);
  const [body, arm] = frameCanvases(bodyImg, 'stand1', 0);
  if (!body) return [];
  const bodyPos = { x: 0, y: 0 };
  push(layers, 'body', body, bodyPos, 'body');
  const neck = worldPoint(bodyPos, body, ['neck']);
  const navel = worldPoint(bodyPos, body, ['navel']);
  let handPoint = null;
  if (arm && navel) {
    const armPos = layerPos(arm, ['navel'], navel);
    push(layers, 'arm', arm, armPos, 'arm');
    if (armPos) handPoint = worldPoint(armPos, arm, ['hand']);
  }

  // 2) 头 + 脸 + 头发 + 帽子/面饰
  const headImg = await loadImage(`Character/${pad8(12000 + skin)}.img`);
  const [head] = frameCanvases(headImg, 'front', 0);
  if (!head || !neck) return layers;
  const headPos = layerPos(head, ['neck'], neck);
  push(layers, 'head', head, headPos, 'head');
  const brow = headPos ? worldPoint(headPos, head, ['brow']) : null;

  const hairImg = await loadImage(`Character/Hair/${pad8(spec.hair || 30000)}.img`);
  if (brow) {
    for (const c of frameCanvases(hairImg, 'backDefault', 0)) {
      push(layers, 'hair', c, layerPos(c, ['brow'], brow), 'backHair');
    }
    for (const c of frameCanvases(hairImg, 'default', 0)) {
      push(layers, 'hair', c, layerPos(c, ['brow'], brow), 'hair');
    }
    const faceImg = await loadImage(`Character/Face/${pad8(spec.face || 20000)}.img`);
    for (const c of frameCanvases(faceImg, 'default', 0)) {
      push(layers, 'face', c, layerPos(c, ['brow'], brow), 'face');
    }
  }

  // 3) 已穿戴装备
  const ids = Array.isArray(spec.items) ? spec.items : [];
  const seen = new Set();
  for (const itemId of ids) {
    if (seen.has(itemId)) continue;
    seen.add(itemId);
    const file = equipFile(itemId);
    if (!file) continue;
    const img = await loadImage(file.rel);
    if (!img) continue;
    for (const c of frameCanvases(img, 'stand1', 0)) {
      const pointTarget = ['Cap', 'Accessory'].includes(file.dir) ? brow : file.dir === 'Weapon' ? handPoint : navel;
      if (!pointTarget) continue;
      const anchorNames = file.dir === 'Weapon' ? ['hand', 'navel'] : ['navel', 'brow', 'neck'];
      const fallbackZ =
        file.dir === 'Cap' ? 'cap' : file.dir === 'Accessory' ? 'accessoryFace' : file.dir === 'Weapon' ? 'weapon' : 'mail';
      push(layers, file.dir, c, layerPos(c, anchorNames, pointTarget), fallbackZ);
    }
  }

  return layers;
}

/** 合成并输出 PNG Buffer */
export async function getAvatarPng(spec) {
  const key = [
    spec.skin ?? 0,
    spec.hair ?? 0,
    spec.face ?? 0,
    (spec.items || []).slice().sort((a, b) => a - b).join(','),
  ].join('|');
  if (pngCache.has(key)) {
    const hit = pngCache.get(key);
    pngCache.delete(key);
    pngCache.set(key, hit);
    return hit;
  }

  let layers = await buildLayers(spec);
  if (!layers.length) return null;
  await loadZOrder();
  layers.sort((a, b) => zRank(a.z) - zRank(b.z));

  const sprites = [];
  for (const layer of layers) {
    try {
      const bmp = await layer.canvas.getBitmap();
      if (!bmp) continue;
      sprites.push({ jimp: bmp.getCanvas(), x: layer.x, y: layer.y });
    } catch {
      /* 单张图失败不影响整体 */
    }
  }
  if (!sprites.length) return null;

  let minX = Infinity;
  let minY = Infinity;
  let maxX = -Infinity;
  let maxY = -Infinity;
  for (const s of sprites) {
    minX = Math.min(minX, s.x);
    minY = Math.min(minY, s.y);
    maxX = Math.max(maxX, s.x + s.jimp.bitmap.width);
    maxY = Math.max(maxY, s.y + s.jimp.bitmap.height);
  }

  // 以「脚底中心」为固定基准，保证不同外形/发型/武器与界面布局稳定对齐
  const bodyLeft = -minX;
  const bodyRight = maxX - 16;
  const half = Math.max(bodyLeft, bodyRight, MIN_WIDTH / 2);
  const width = Math.ceil(half * 2) + 4;
  const height = Math.ceil(31 - minY) + PAD_TOP + PAD_BOTTOM;
  const feetX = Math.round(half) + 2;
  const feetY = height - PAD_BOTTOM;
  const offsetX = feetX - 16;
  const offsetY = feetY - 31;

  const dest = new Jimp(width, height, 0x00000000);
  for (const s of sprites) {
    dest.composite(s.jimp, s.x + offsetX, s.y + offsetY);
  }

  const png = Buffer.from(await dest.getBufferAsync('image/png'));
  if (pngCache.size >= MAX_PNG) pngCache.clear();
  pngCache.set(key, png);
  return png;
}

export const AVATAR_EQUIP_DIRS = EQUIP_DIRS;

/**
 * 从客户端 Data（解包后的 WZ img）生成 H5 挂机地图目录。
 * 输出：data/catalog.sql（MySQL 种子数据）、data/catalog-summary.json
 *
 * 分类：String.wz/Map.img 的一级节点（maple / victoria / ossyria ...）= 地区
 * 过滤：info.town=1（城镇）、info.fieldLimit!=0（特殊限制）、无刷怪点、info.link（镜像）全部跳过
 */
const fs = require('fs');
const path = require('path');
const wz = require('@tybys/wz');

const CLIENT = 'd:/迅雷下载/1.12/BeiDou-ClientV17/BeiDou-Client/Data';
const OUT_DIR = path.resolve(__dirname, '../data');

const REGION_CN = {
  maple: '彩虹岛', victoria: '金银岛', ossyria: '奥西利亚', elin: '艾琳森林',
  singapore: '新加坡', MasteriaGL: '玛斯特利亚', jp: '日本', china: '中国',
  thai: '泰国', event: '活动地图', HalloweenGL: '万圣节', weddingGL: '婚礼村',
  Episode1GL: '章节地图', etc: '其他地图',
};

async function loadImg(relPath) {
  const full = path.join(CLIENT, relPath);
  if (!fs.existsSync(full)) return null;
  const img = wz.WzImage.createFromFile(full, wz.WzMapleVersion.GMS);
  const ok = await img.parseImage(true);
  if (!ok) return null;
  return img;
}

function num(prop, def = 0) {
  if (!prop) return def;
  const v = prop.wzValue;
  const n = Number(v);
  return Number.isFinite(n) ? n : def;
}

function str(prop, def = '') {
  if (!prop) return def;
  const v = prop.wzValue;
  return v === null || v === undefined ? def : String(v);
}

function pad9(id) { return String(id).padStart(9, '0'); }
function pad7(id) { return String(id).padStart(7, '0'); }

function mapImgPath(id) {
  return `Map/Map/Map${Math.floor(id / 100000000)}/${pad9(id)}.img`;
}

(async () => {
  await wz.init();
  console.log('loading String.wz ...');
  const stringMap = await loadImg('String/Map.img');
  const stringMob = await loadImg('String/Mob.img');

  // mapId -> {name, street}
  const mapStrings = new Map();
  // region -> [mapId]
  const regions = [];
  let order = 0;
  for (const cat of stringMap.wzProperties) {
    const ids = [];
    for (const entry of cat.wzProperties) {
      const id = parseInt(entry.name, 10);
      if (!Number.isFinite(id)) continue;
      mapStrings.set(id, { name: str(entry.at('mapName')), street: str(entry.at('streetName')) });
      ids.push(id);
    }
    regions.push({ key: cat.name, name: REGION_CN[cat.name] || cat.name, order: order++, ids });
  }

  const mobNames = new Map();
  if (stringMob) {
    for (const entry of stringMob.wzProperties) {
      const id = parseInt(entry.name, 10);
      if (!Number.isFinite(id)) continue;
      mobNames.set(id, str(entry.at('name')));
    }
  }
  console.log('regions:', regions.length, 'map strings:', mapStrings.size, 'mob names:', mobNames.size);

  const maps = [];
  const mobStatsCache = new Map();
  const usedMobIds = new Set();
  let scanned = 0, parseFail = 0;

  async function getMobStats(mobId) {
    if (mobStatsCache.has(mobId)) return mobStatsCache.get(mobId);
    let stat = null;
    const img = await loadImg(`Mob/${pad7(mobId)}.img`);
    if (img) {
      const info = img.at('info');
      if (info) {
        stat = {
          id: mobId,
          name: mobNames.get(mobId) || `怪物${mobId}`,
          level: num(info.at('level'), 1),
          hp: num(info.at('maxHP'), 1),
          exp: num(info.at('exp'), 0),
          boss: num(info.at('boss'), 0) > 0 ? 1 : 0,
        };
      }
      img.dispose();
    }
    mobStatsCache.set(mobId, stat);
    return stat;
  }

  for (const region of regions) {
    for (const mapId of region.ids) {
      scanned++;
      const img = await loadImg(mapImgPath(mapId));
      if (!img) { parseFail++; continue; }

      const info = img.at('info');
      if (!info) { img.dispose(); continue; }
      if (str(info.at('link'), '') !== '') { img.dispose(); continue; }
      if (num(info.at('town'), 0) !== 0) { img.dispose(); continue; }
      if (num(info.at('fieldLimit'), 0) !== 0) { img.dispose(); continue; }
      const returnMap = num(info.at('returnMap'), 0);
      const mobRate = num(info.at('mobRate'), 1);
      const createMobIntervalSec = Math.round(num(info.at('createMobInterval'), 5000) / 1000);

      const life = img.at('life');
      const mobs = [];
      let hasBoss = 0;
      if (life && life.wzProperties) {
        for (const l of life.wzProperties) {
          if (str(l.at('type'), '') !== 'm') continue;
          const mobId = num(l.at('id'), 0);
          if (!mobId) continue;
          const hide = num(l.at('hide'), 0);
          const mobTime = num(l.at('mobTime'), 0);
          mobs.push({ mobId, mobTime, hide });
          usedMobIds.add(mobId);
        }
      }
      img.dispose();
      if (mobs.length === 0) continue;

      const names = mapStrings.get(mapId) || {};
      // 合并同类怪刷怪点
      const grouped = new Map();
      for (const m of mobs) {
        const key = `${m.mobId}|${m.mobTime}`;
        const cur = grouped.get(key);
        if (cur) cur.count++;
        else grouped.set(key, { mobId: m.mobId, mobTime: m.mobTime, hide: m.hide, count: 1 });
      }
      maps.push({
        id: mapId,
        region: region.key,
        regionName: region.name,
        regionOrder: region.order,
        name: names.name || String(mapId),
        street: names.street || '',
        returnMap, mobRate, createMobIntervalSec,
        mobs: [...grouped.values()],
      });
    }
    console.log(`  ${region.key}: ${region.ids.length} ids scanned, kept ${maps.filter(m => m.region === region.key).length}`);
  }
  console.log(`scanned ${scanned}, parseFail ${parseFail}, kept maps ${maps.length}, mobs used ${usedMobIds.size}`);

  console.log('loading mob stats ...');
  const mobStats = [];
  for (const mobId of usedMobIds) {
    const s = await getMobStats(mobId);
    if (s) { mobStats.push(s); if (s.boss) { /* keep */ } }
  }
  console.log('mob stats:', mobStats.length);

  // 过滤掉没有任何可用怪物数据的刷怪点
  const statMap = new Map(mobStats.map(s => [s.id, s]));
  for (const m of maps) {
    m.mobs = m.mobs.filter(x => statMap.has(x.mobId));
  }
  const validMaps = maps.filter(m => m.mobs.length > 0);

  // 二级分区：按 returnMap 指向的城镇划分，同一地区内按城镇地图 ID 升序编号
  const subOrders = new Map();
  for (const r of regions) {
    const towns = [...new Set(validMaps.filter(m => m.region === r.key && m.returnMap).map(m => m.returnMap))].sort((a, b) => a - b);
    subOrders.set(r.key, new Map(towns.map((t, i) => [t, i + 1])));
  }

  fs.mkdirSync(OUT_DIR, { recursive: true });
  const summary = {
    regions: regions.map(r => ({ key: r.key, name: r.name, maps: validMaps.filter(m => m.region === r.key).length })),
    maps: validMaps.length, mobs: mobStats.length,
  };
  fs.writeFileSync(path.join(OUT_DIR, 'catalog-summary.json'), JSON.stringify(summary, null, 2));
  fs.writeFileSync(path.join(OUT_DIR, 'catalog.json'), JSON.stringify({ regions: summary.regions, maps: validMaps, mobs: mobStats }));

  const esc = s => String(s).replace(/'/g, "''");
  const lines = [];
  lines.push('-- 由 tools/gen-catalog.js 自动生成，请勿手工修改');
  lines.push('DELETE FROM h5_hangup_map_mob;');
  lines.push('DELETE FROM h5_hangup_map;');
  lines.push('DELETE FROM h5_hangup_mob;');
  for (const s of mobStats) {
    lines.push(`INSERT INTO h5_hangup_mob VALUES (${s.id},'${esc(s.name)}',${s.level},${s.hp},${s.exp},${s.boss});`);
  }
  for (const m of validMaps) {
    const mobs = m.mobs;
    const levels = mobs.map(x => statMap.get(x.mobId).level);
    const minLv = Math.min(...levels), maxLv = Math.max(...levels);
    const totalSpawn = mobs.reduce((a, x) => a + x.count, 0);
    const wSum = mobs.reduce((a, x) => a + (x.mobTime > 0 ? x.mobTime : m.createMobIntervalSec || 5) * x.count, 0);
    const respawn = Math.round(wSum / Math.max(1, totalSpawn));
    const hasBoss = mobs.some(x => statMap.get(x.mobId).boss);
    const town = m.returnMap || 0;
    const townName = town ? (mapStrings.get(town) || {}).name || '' : '';
    const townOrder = (subOrders.get(m.region) || new Map()).get(town) || 0;
    lines.push(`INSERT INTO h5_hangup_map VALUES (${m.id},'${esc(m.region)}','${esc(m.regionName)}','${town}','${esc(townName)}',${townOrder},${m.regionOrder},'${esc(m.name)}','${esc(m.street)}',${minLv},${maxLv},${totalSpawn},${respawn},${hasBoss ? 1 : 0});`);
    for (const x of mobs) {
      lines.push(`INSERT INTO h5_hangup_map_mob VALUES (${m.id},${x.mobId},${x.count},${x.mobTime},${x.hide});`);
    }
  }
  fs.writeFileSync(path.join(OUT_DIR, 'catalog.sql'), lines.join('\n'));
  console.log('written:', path.join(OUT_DIR, 'catalog.sql'), lines.length, 'lines');
  console.log(JSON.stringify(summary, null, 2));
})().catch(e => { console.error('FAIL', e); process.exit(1); });

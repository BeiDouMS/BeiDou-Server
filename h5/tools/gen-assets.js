/**
 * 从客户端 UI.wz 导出 H5 可用的 UI 贴图（按钮、勾选框、Logo）。
 * 输出：data/assets/ui/**.png + data/assets/ui/manifest.json
 */
const fs = require('fs');
const path = require('path');
const wz = require('@tybys/wz');

const CLIENT = 'd:/迅雷下载/1.12/BeiDou-ClientV17/BeiDou-Client/Data';
const OUT = path.resolve(__dirname, '../data/assets/ui');

const TARGETS = [
  { img: 'UI/Basic.img', nodes: ['BtOK', 'BtCancel', 'BtClaim', 'BtReceive', 'BtYes', 'BtNo', 'BtClose', 'BtDecide', 'BtSend', 'CheckBox'] },
  { img: 'UI/Logo.img', nodes: ['Nexon', 'Wizet'] },
];

const manifest = [];

async function exportNode(node, prefix) {
  for (const child of node.wzProperties) {
    const type = child.propertyType;
    if (type === wz.WzPropertyType.Canvas) {
      try {
        const bmp = await child.getBitmap();
        if (!bmp) continue;
        const w = bmp.getWidth();
        const buf = await bmp.getBufferAsync('image/png');
        const rel = `${prefix}/${child.name}.png`;
        const full = path.join(OUT, rel);
        fs.mkdirSync(path.dirname(full), { recursive: true });
        fs.writeFileSync(full, Buffer.from(buf));
        manifest.push({ path: rel.replace(/\\/g, '/'), width: w, height: bmp.getCanvas().bitmap ? bmp.getCanvas().bitmap.height : 0 });
        bmp.dispose();
      } catch (e) {
        console.error('canvas fail', prefix, child.name, e.message);
      }
    } else if (child.wzProperties) {
      await exportNode(child, `${prefix}/${child.name}`);
    }
  }
}

(async () => {
  await wz.init();
  fs.mkdirSync(OUT, { recursive: true });
  for (const target of TARGETS) {
    const img = wz.WzImage.createFromFile(`${CLIENT}/${target.img}`, wz.WzMapleVersion.GMS);
    await img.parseImage(true);
    for (const name of target.nodes) {
      const node = img.at(name);
      if (!node) { console.log('missing', name); continue; }
      await exportNode(node, `${path.basename(target.img, '.img')}/${name}`);
      console.log('exported', name);
    }
  }
  fs.writeFileSync(path.join(OUT, 'manifest.json'), JSON.stringify(manifest, null, 2));
  console.log('total', manifest.length);
})().catch(e => { console.error(e); process.exit(1); });

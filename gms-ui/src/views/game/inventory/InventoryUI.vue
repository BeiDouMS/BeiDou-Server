<template>
  <div>
    <a-spin v-if="loading" />
    <div v-else>
      <canvas
        id="inventoryCanvas"
        :width="canvasWidth"
        :height="canvasHeight"
      ></canvas>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, PropType, ref, watch, onMounted } from 'vue';
  import { getInventoryList } from '@/api/inventory'; // 假设这个函数已经定义
  import axios from 'axios';
  import invImage from '@/assets/inv_full.png';
  import beidouBook from '@/assets/2430033.png';

  const maplestoryioAPI = axios.create({
    baseURL: 'https://maplestory.io/api',
  });

  async function getFromCacheOrDownload(itemId: number): Promise<string> {
    try {
      const response = await maplestoryioAPI.get(
        `/GMS/83/item/${itemId}/icon?resize=4`,
        { responseType: 'blob' }
      );
      return URL.createObjectURL(response.data);
    } catch (error) {
      console.error('获取道具图标失败：', error);
      return '';
    }
  }

  function calculatePositionOffsets() {
    const a = { x: 9, y: 51 };
    const b = { x: 45, y: 51 };
    const c = { x: 9, y: 85 };
    const d = { x: 158, y: 51 };
    const page = 4; // 页面数量
    const pageOffset = d.x - a.x;
    const initxByPage = [];

    for (let p = 0; p < page; p += 1) {
      initxByPage.push({ x: a.x + pageOffset * p, y: a.y });
    }

    const offSetX = b.x - a.x;
    const offSetY = c.y - a.y;
    const slotX = 4; // 每页槽位的 X 数量
    const slotY = 6; // 每页槽位的 Y 数量
    const posArr = [];

    for (let p = 0; p < page; p += 1) {
      for (let y = 0; y < slotY; y += 1) {
        for (let x = 0; x < slotX; x += 1) {
          const currentX = initxByPage[p].x + x * offSetX;
          const currentY = initxByPage[p].y + y * offSetY;
          posArr.push({ x: currentX, y: currentY });
        }
      }
    }

    return posArr;
  }

  export default defineComponent({
    props: {
      characterId: {
        type: [String, Number] as PropType<string | number>,
        required: true,
      },
      inventoryType: {
        type: [String, Number] as PropType<string | number>,
        required: true,
      },
    },
    setup(props) {
      const loading = ref(false);
      const inventory = ref([]);

      const canvasWidth = 600;
      const canvasHeight = 290;

      const fetchInventoryData = async () => {
        loading.value = true;
        try {
          const condition = {
            notPage: true,
            inventoryType: props.inventoryType,
            characterId: Number(props.characterId),
          };

          // 请求库存数据
          const data = await getInventoryList(condition);

          // 确保 data 是数组，如果不是则设为 []
          if (Array.isArray(data.data)) {
            inventory.value = data.data;
          } else {
            inventory.value = [];
          }

          console.log(inventory.value);
        } catch (error) {
          console.error('获取库存数据失败：', error);
          inventory.value = []; // 出错时也要确保 inventory 是数组
        } finally {
          loading.value = false;
        }
      };

      watch(
        () => [props.characterId, props.inventoryType],
        () => {
          fetchInventoryData();
        },
        { immediate: true }
      );

      const drawInventory = async () => {
        setTimeout(async () => {
          const id = document.getElementById('inventoryCanvas');
          if (!id) return;
          const ctx = id.getContext('2d');
          if (!ctx) return;

          const backgroundImg = new Image();
          backgroundImg.src = invImage;
          backgroundImg.onload = async () => {
            ctx.drawImage(backgroundImg, 0, 0, canvasWidth, canvasHeight);

            const posArr = calculatePositionOffsets();

            const imagePromises = inventory.value.map(async (item) => {
              const { position, quantity, itemId } = item;
              // 确保position是从1开始
              const index = (position - 1) % posArr.length;
              const pos = posArr[index]; // 使用 mod 运算来避免越界
              let itemImgSrc;
              if (itemId === 2430033) {
                itemImgSrc = beidouBook; // 使用本地图片
              } else {
                itemImgSrc = await getFromCacheOrDownload(itemId); // 从 API 获取图片
              }
              const itemImg = new Image();
              return new Promise((resolve) => {
                itemImg.src = itemImgSrc;
                itemImg.onload = () => {
                  ctx.drawImage(itemImg, pos.x, pos.y, 30, 30);
                  // 检查物品类型
                  if (props.inventoryType !== 1) {
                    ctx.fillStyle = 'white'; // 填充颜色
                    ctx.fillText(`${quantity}`, pos.x, pos.y + 25); // 绘制填充文本
                    ctx.strokeStyle = 'black'; // 文本轮廓颜色
                    ctx.lineWidth = 3; // 轮廓线宽
                    ctx.strokeText(`${quantity}`, pos.x, pos.y + 25); // 绘制轮廓
                    ctx.fillStyle = 'white'; // 填充颜色
                    ctx.fillText(`${quantity}`, pos.x, pos.y + 25); // 绘制填充文本
                  }
                  resolve();
                };
              });
            });

            await Promise.all(imagePromises);
          };
        }, 0);
      };

      watch(inventory, () => {
        drawInventory();
      });

      return {
        loading,
        inventory,
        canvasWidth,
        canvasHeight,
      };
    },
  });
</script>

<style scoped>
  /* 添加你的样式 */
</style>
}

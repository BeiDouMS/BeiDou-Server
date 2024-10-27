<template>
  <div>
    <a-spin v-if="loading" />
    <div v-else>
      <canvas
        id="inventoryCanvas"
        :width="canvasWidth"
        :height="canvasHeight"
        @mousemove="handleMouseMove"
        @mouseleave="handleMouseLeave"
        :class="{ 'hide-cursor': hideCursor }"
      ></canvas>
      <div
        class="tooltip"
        :style="{
          left: `${tooltip.x}px`,
          top: `${tooltip.y}px`,
          display: tooltip.show ? 'block' : 'none',
        }"
      >
        {{ tooltip.text }}
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, PropType, ref, watch, onMounted } from 'vue';
  import { getInventoryList } from '@/api/inventory';
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
      const posArr = ref(calculatePositionOffsets());

      const canvasWidth = 600;
      const canvasHeight = 290;

      const tooltip = ref({
        x: 0,
        y: 0,
        text: '',
        show: false,
      });

      const hideCursor = ref(false);

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

            const imagePromises = inventory.value.map(async (item) => {
              const { position, quantity, itemId } = item;
              // 确保position是从1开始
              const index = (position - 1) % posArr.value.length;
              const pos = posArr.value[index]; // 使用 mod 运算来避免越界
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

      const handleMouseMove = (event: MouseEvent) => {
        const rect = event.currentTarget.getBoundingClientRect();
        const mouseX = event.clientX - rect.left;
        const mouseY = event.clientY - rect.top;
        const foundItem = inventory.value.find((item) => {
          const { position } = item;
          const index = (position - 1) % posArr.value.length;
          const pos = posArr.value[index];
          return (
            mouseX > pos.x &&
            mouseX < pos.x + 30 &&
            mouseY > pos.y &&
            mouseY < pos.y + 30
          );
        });

        if (foundItem) {
          // 计算提示框的位置，使其远离鼠标指针
          const offsetX = 15; // X 方向的偏移量
          const offsetY = 15; // Y 方向的偏移量
          tooltip.value = {
            x: mouseX + offsetX,
            y: mouseY + offsetY,
            text: `Item ID: ${foundItem.itemId}`,
            show: true,
          };
          hideCursor.value = true; // 隐藏鼠标指针
        } else {
          tooltip.value.show = false;
          hideCursor.value = false; // 显示鼠标指针
        }
      };

      const handleMouseLeave = () => {
        tooltip.value.show = false;
        hideCursor.value = false; // 显示鼠标指针
      };

      watch(inventory, () => {
        drawInventory();
      });

      return {
        loading,
        inventory,
        canvasWidth,
        canvasHeight,
        tooltip,
        handleMouseMove,
        handleMouseLeave,
        hideCursor,
      };
    },
  });
</script>

<style scoped>
  .tooltip {
    position: absolute;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 5px;
    border-radius: 5px;
    font-size: 12px;
    z-index: 1000;
    pointer-events: none; /* 防止提示框影响鼠标事件 */
  }

  .hide-cursor {
    cursor: none !important; /* 隐藏鼠标指针 */
  }
</style>

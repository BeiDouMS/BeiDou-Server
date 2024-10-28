<template>
  <div>
    <a-spin v-if="loading" />
    <div v-else>
      <canvas
        id="inventoryCanvas"
        :width="canvasWidth"
        :height="canvasHeight"
        :class="{ 'hide-cursor': hideCursor }"
        @mousemove="handleMouseMove"
        @mouseleave="handleMouseLeave"
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
  import { defineComponent, ref, watch, onMounted } from 'vue';
  import { getInventoryList } from '@/api/inventory';
  import axios from 'axios';
  import invImage from '@/assets/inv_full.png';
  import beidouBook from '@/assets/2430033.png';
  import { useI18n } from 'vue-i18n';

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

  interface InventoryCondition {
    notPage: boolean;
    inventoryType: number; // 确保 inventoryType 是 number 类型
    characterId: number;
    page: number;
    pageSize: number;
    pageNo: number;
  }

  export default defineComponent({
    props: {
      characterId: {
        type: Number,
        required: true,
        default: 0, // 默认值
      },
      inventoryType: {
        type: Number,
        required: true,
        default: 1, // 默认值
      },
    },
    setup(props) {
      const { t } = useI18n();
      const loading = ref(false);
      const inventory = ref<any[]>([]);
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
          const condition: InventoryCondition = {
            notPage: true,
            inventoryType: props.inventoryType, // 直接使用 props.inventoryType
            characterId: props.characterId,
            page: 1,
            pageSize: 100,
            pageNo: 1,
          };

          const data = await getInventoryList(condition);

          if (Array.isArray(data.data)) {
            inventory.value = data.data;
          } else {
            inventory.value = [];
          }
        } catch (error) {
          inventory.value = [];
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
          const canvas = document.getElementById(
            'inventoryCanvas'
          ) as HTMLCanvasElement;
          if (!canvas) return;
          const ctx = canvas.getContext('2d');
          if (!ctx) return;

          const backgroundImg = new Image();
          backgroundImg.src = invImage;
          backgroundImg.onload = async () => {
            ctx.drawImage(backgroundImg, 0, 0, canvasWidth, canvasHeight);

            const imagePromises = inventory.value.map(async (item) => {
              const { position, quantity, itemId } = item;
              const index = (position - 1) % posArr.value.length;
              const pos = posArr.value[index];
              let itemImgSrc: string;
              if (itemId === 2430033) {
                itemImgSrc = beidouBook;
              } else {
                itemImgSrc = await getFromCacheOrDownload(itemId);
              }
              const itemImg = new Image();
              return new Promise<void>((resolve) => {
                itemImg.src = itemImgSrc;
                itemImg.onload = () => {
                  ctx.drawImage(itemImg, pos.x, pos.y, 30, 30);
                  if (props.inventoryType !== 1 && quantity > 1) {
                    ctx.fillStyle = 'white';
                    ctx.fillText(`${quantity}`, pos.x, pos.y + 25);
                    ctx.strokeStyle = 'black';
                    ctx.lineWidth = 3;
                    ctx.strokeText(`${quantity}`, pos.x, pos.y + 25);
                    ctx.fillStyle = 'white';
                    ctx.fillText(`${quantity}`, pos.x, pos.y + 25);
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
        const target = event.currentTarget as HTMLElement;
        if (!target) return;

        const rect = target.getBoundingClientRect();
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
          const offsetX = 15;
          const offsetY = 15;
          tooltip.value = {
            x: mouseX + offsetX,
            y: mouseY + offsetY,
            text: `${t('inventory.placeholder.itemId')}: ${
              foundItem.itemId
            }\n${t('inventory.placeholder.itemName')}: ${foundItem.itemName}`,
            show: true,
          };
          hideCursor.value = true;
        } else {
          tooltip.value.show = false;
          hideCursor.value = false;
        }
      };

      const handleMouseLeave = () => {
        tooltip.value.show = false;
        hideCursor.value = false;
      };

      watch(inventory, () => {
        drawInventory();
      });

      onMounted(() => {
        fetchInventoryData();
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
    white-space: pre-line;
    pointer-events: none; /* 防止提示框影响鼠标事件 */
  }

  .hide-cursor {
    cursor: none !important; /* 隐藏鼠标指针 */
  }
</style>

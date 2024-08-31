export interface InventoryTypeState {
  type: number;
  name: string;
}

export interface InventoryState {
  id?: number;
  characterId?: number;
  itemId?: number;
  itemType?: number;
  inventoryType?: number;
  position?: number;
  quantity?: number;
  owner?: string;
  petId?: number;
  flag?: number;
  expiration?: number;
  giftFrom?: string;
  online?: boolean;
  equipment?: boolean;
  inventoryEquipment?: any;
}

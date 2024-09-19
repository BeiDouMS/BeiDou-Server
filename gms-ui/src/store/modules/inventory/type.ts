export interface InventoryTypeState {
  inventoryType: number;
  name: string;
}

export interface InventoryEquipmentState {
  id: number;
  inventoryItemId: number;
  upgradeSlots: number;
  level: number;
  attStr: number;
  attDex: number;
  attInt: number;
  attLuk: number;
  hp: number;
  mp: number;
  patk: number;
  matk: number;
  pdef: number;
  mdef: number;
  acc: number;
  avoid: number;
  hands: number;
  speed: number;
  jump: number;
  locked: number;
  vicious: number;
  itemLevel: number;
  itemExp: number;
  ringId: number;
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
  inventoryEquipment: InventoryEquipmentState;
}

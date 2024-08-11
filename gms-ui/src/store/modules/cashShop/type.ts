export interface categoryState {
  id: number;
  name: string;
  subId: number;
  subName: string;
  onSale?: number;
  pageNo?: number;
  pageSize?: number;
  onlyTotal?: number;
  notPage?: number;
}

export interface cashShopState {
  categoryId?: number;
  categoryName?: string;
  subcategoryId?: number;
  subcategoryName?: string;
  sn: number;
  itemId: number;
  price?: number;
  defaultPrice?: number;
  period?: number;
  defaultPeriod?: number;
  priority?: number;
  defaultPriority?: number;
  count?: number;
  defaultCount?: number;
  onSale?: number;
  defaultOnSale?: number;
}

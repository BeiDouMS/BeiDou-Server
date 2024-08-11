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
  bonus?: number;
  defaultBonus?: number;
  maplePoint?: number;
  defaultMaplePoint?: number;
  meso?: number;
  defaultMeso?: number;
  forPremiumUser?: number;
  defaultForPremiumUser?: number;
  gender?: number;
  defaultGender?: number;
  clz?: number;
  defaultClz?: number;
  limit?: number;
  defaultLimit?: number;
  pbCash?: number;
  defaultPBCash?: number;
  pbPoint?: number;
  defaultPBPoint?: number;
  pbGift?: number;
  defaultPBGift?: number;
  packageSn?: number;
  defaultPackageSn?: number;
}

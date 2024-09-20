export interface GachaponPoolState {
  id?: number;
  name?: string;
  gachaponId?: number;
  weight?: number;
  isPublic?: boolean;
  prob?: number;
  startTime?: number;
  endTime?: number;
  notification?: boolean;
  realProb?: number;
  comment?: string;
}

export interface GachaponRewardState {
  id?: number;
  poolId?: number;
  itemId?: number;
  quantity?: number;
  createTime?: number;
  comment?: string;
}

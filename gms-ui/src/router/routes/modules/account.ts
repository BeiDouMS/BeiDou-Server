import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const ACCOUNT: AppRouteRecordRaw = {
  path: '/account',
  name: 'account',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.account',
    requiresAuth: true,
    icon: 'icon-user',
    order: 1,
  },
  children: [
    {
      path: 'list',
      name: 'AccountList',
      component: () => import('@/views/account/list/index.vue'),
      meta: {
        locale: 'menu.account.list',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: 'player',
      name: 'PlayerList',
      component: () => import('@/views/account/player/index.vue'),
      meta: {
        locale: 'menu.account.player',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
  ],
};

export default ACCOUNT;

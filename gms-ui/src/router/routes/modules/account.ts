import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const ACCOUNT: AppRouteRecordRaw = {
  path: '/account',
  name: 'account',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.account',
    requiresAuth: true,
    icon: 'icon-dashboard',
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
        roles: ['*'],
      },
    },
  ],
};

export default ACCOUNT;

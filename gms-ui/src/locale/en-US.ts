import config from '@/views/game/config/locale/en-US';
import drop from '@/views/game/drop/locale/en-US';
import cashShop from '@/views/game/cashShop/locale/en-US';
import npcShop from '@/views/game/npcShop/locale/en-US';
import gachapon from '@/views/game/gachapon/locale/en-US';
import commandInfo from '@/views/game/commandInfo/locale/en-US';
import workplace from '@/views/dashboard/workplace/locale/en-US';
import informationSearch from '@/views/dashboard/informationSearch/locale/en-US';
import account from '@/views/account/locale/en-US';
import login from '@/views/login/locale/en-US';
import inventory from '@/views/game/inventory/locale/en-US';
import base from './en-US/base';

export default {
  'menu.dashboard': 'Dashboard',
  'menu.dashboard.workplace': 'Workplace',
  'menu.dashboard.informationSearch': 'Information Search',
  'menu.game': 'Game',
  'menu.game.config': 'Config',
  'menu.game.npcShop': 'NPC Shop',
  'menu.game.cashShop': 'Cash Shop',
  'menu.game.drop': 'Mob Drop',
  'menu.game.drop.global': 'Global Drop',
  'menu.game.inventory': 'Inventory',
  'menu.game.gachapon': 'Gachapon',
  'menu.game.command': 'Command',
  'menu.game.file': 'File',
  'menu.account': 'Account',
  'menu.account.list': 'Account List',
  'menu.account.player': 'Player',
  'menu.arco': 'UI Doc',
  'menu.beiDou': 'About BeiDou',
  'message.success': 'Success',
  'message.switch.success': 'Switch to English',
  'message.login.success': 'Welcome',
  'message.logout.success': 'Logout success',
  'settings.language': 'Language',
  'settings.switch.toDark': 'Click to use dark mode',
  'settings.switch.toLight': 'Click to use light mode',
  'settings.screen.toFull': 'Click to switch to full screen mode',
  'settings.screen.toExit': 'Click to exit the full screen mode',
  'settings.userCenter': 'User Center',
  'settings.userSettings': 'User Settings',
  'settings.logout': 'Logout',
  'settings.version': 'Version',
  ...base,
  ...workplace,
  ...login,
  ...account,
  ...npcShop,
  ...cashShop,
  ...drop,
  ...gachapon,
  ...commandInfo,
  ...informationSearch,
  ...inventory,
  ...config,
};

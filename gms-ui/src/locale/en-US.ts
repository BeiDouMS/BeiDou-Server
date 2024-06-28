import workplace from '@/views/dashboard/workplace/locale/en-US';
import account from '@/views/account/locale/en-US';
import login from '@/views/login/locale/en-US';
import base from './en-US/base';

export default {
  'menu.dashboard': 'Dashboard',
  'menu.dashboard.workplace': 'Workplace',
  'menu.account': 'Account',
  'menu.account.list': 'Account List',
  'menu.arco': 'Arco Design',
  'menu.faq': 'FAQ',
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
  ...base,
  ...workplace,
  ...login,
  ...account,
};

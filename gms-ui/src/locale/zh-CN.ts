import workplace from '@/views/dashboard/workplace/locale/zh-CN';
import login from '@/views/login/locale/zh-CN';
import baseConfig from './zh-CN/base';

export default {
  // 左侧菜单
  'menu.dashboard': '仪表盘',
  'menu.dashboard.workplace': '工作台',
  'menu.arco': 'Arco Design',
  'menu.faq': '常见问题',
  // 提示信息
  'message.switch.success': '切换为中文',
  'message.login.success': '欢迎使用',
  'message.logout.success': '登出成功',
  // 设置
  'settings.language': '语言',
  'settings.switch.toDark': '点击切换为暗黑模式',
  'settings.switch.toLight': '点击切换为明亮模式',
  'settings.screen.toFull': '点击切换全屏模式',
  'settings.screen.toExit': '点击退出全屏模式',
  'settings.userCenter': '用户中心',
  'settings.userSettings': '用户设置',
  'settings.logout': '退出登录',
  ...baseConfig, // 基本配置（示范如何外部导入）
  ...workplace,
  ...login,
};

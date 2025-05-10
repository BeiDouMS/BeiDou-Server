import { useI18n } from 'vue-i18n';

/**
 * 判断一个变量是否为有效字符串，
 * 判断标准：
 * 1. 变量类型为 string (顺带排除了null和undefined的情况)
 * 2. 变量的值不为纯空格或者没有值
 */
export function isValidString(data: any) {
  // string 已经排除了 null 和 undefined的情况
  return typeof data === 'string' && data.trim() !== '';
}

/**
 * 将时间戳转换为中文或英文时间字符串
 * @param timestamp 时间戳
 * @returns 格式化的时间字符串
 */
export function timestampToChineseTime(timestamp: number | string) {
  const { locale } = useI18n();

  if (timestamp === -1) {
    return locale.value === 'en-US' ? 'Permanent' : '永久';
  }
  if (typeof timestamp === 'string') timestamp?.replace(' ', 'T');
  const date = new Date(timestamp);

  // 获取年、月、日、时、分、秒
  const year = date.getFullYear();
  const month = date.getMonth() + 1; // 月份从0开始，所以加1
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  // 根据语言环境返回不同的时间字符串
  if (locale.value === 'en-US') {
    return `${year}-${month.toString().padStart(2, '0')}-${day
      .toString()
      .padStart(2, '0')} ${hours.toString().padStart(2, '0')}:${minutes
      .toString()
      .padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }
  return `${year}年${month}月${day}日 ${hours}时${minutes}分${seconds}秒`;
}

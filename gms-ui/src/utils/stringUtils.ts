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

export function timestampToChineseTime(timestamp: number) {
  if (timestamp === -1) return '永久';
  // 创建一个 Date 对象，传入毫秒时间戳
  const date = new Date(timestamp);

  // 获取年、月、日、时、分、秒
  const year = date.getFullYear();
  const month = date.getMonth() + 1; // 月份从0开始，所以加1
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  // 格式化输出为中文时间字符串
  return `${year}年${month}月${day}日 ${hours}时${minutes}分${seconds}秒`;
}

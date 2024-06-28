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

export function nothing() {
  window.console.log();
}

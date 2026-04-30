var l10n;
l10n ??= {};

/**
 * 根据当前服务端语言获取翻译文本。
 * 须配合 `*.en-US.js` 以及 `*.zh-CN.js` 脚本使用。
 *
 * @param {string} key
 * @param {Record<string, string>} [params]
 * @return {string}
 */
function i18n(key, params) {
    try {
        let text = params === undefined ? l10n[key] : l10n[key](params);
        text = text.replace(/^[\r\n]+|[\r\n]+$/g, '');  // 忽略首尾换行，使翻译脚本更美观
        text = text.replace(/\r?\n/g, '\\n');  // 将不同平台的换行统一为冒险岛的转义符"\\n"
        return text;

    } catch (error) {
        print(`[script] i18n error: ${error}`);
        return `#ri18n error#k\\nkey: #b${key}#k\\nparams: ${JSON.stringify(params)}\\nerror: ${error}`;
    }
}
